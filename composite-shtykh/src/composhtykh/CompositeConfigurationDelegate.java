package composhtykh;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.model.ILaunchConfigurationDelegate;

public class CompositeConfigurationDelegate implements ILaunchConfigurationDelegate {
	
	/*
	 * The key for storing children ILaunchConfigurations in attributes map 
	 * I decided to store it in static field for avoiding confusion editing this code
	 */
	private static String CHILDREN_KEY = "CHILDREN";
	
	/**
	 * Launches all the children configurations
	 */
	@Override
	public void launch(ILaunchConfiguration configuration, String mode, 
			ILaunch launch, IProgressMonitor monitor) throws CoreException {
		for (ILaunchConfiguration child : childrenOf(configuration)) {
			child.launch(mode, monitor);
		}
	}
	
	/**
	 * Returns children launch configurations
	 * @param configuration
	 * @return ILaunchConfigurations are to launch if there are any, empty array otherwise
	 * @throws CoreException
	 */
	public static ILaunchConfiguration[] childrenOf(ILaunchConfiguration configuration)  {
		ILaunchConfiguration[] allConfigurations = null;
		Set<String> childrenNames = null;
		try {
			allConfigurations = DebugPlugin.getDefault().getLaunchManager().getLaunchConfigurations();
			childrenNames = new HashSet<String>(configuration.getAttribute(CHILDREN_KEY, new HashSet<String>()));
		} catch (CoreException e) {
			e.printStackTrace();
			return new ILaunchConfiguration[]{};
		}
		Set<ILaunchConfiguration> children = new HashSet<>();
		for(ILaunchConfiguration testedConfiguration : allConfigurations) {
			if (childrenNames.contains(testedConfiguration.toString())) {
				children.add(testedConfiguration);
				childrenNames.remove(testedConfiguration.toString());
			}
		}
		if (! childrenNames.isEmpty()) {
			String name = configuration.getName();
			name = name == null ? "" : name;
			throw new RuntimeException("Composite launch configuration " + name + " refers to the folowing configurations which had been changed and thus were lost: \n" 
						+ childrenNames.toString() + "\n" 
						+ "To fix that you may delete " + name + " or rename the configuretion listed above" );			
		}
		return children.toArray(new ILaunchConfiguration[children.size()]);
	}
	
	/**
	 * sets
	 * @param configuration
	 * @param childrenNames
	 * @throws CoreException
	 */
	public static void adoptChildren(ILaunchConfigurationWorkingCopy configuration, Set<String> childrenNames) {
		configuration.setAttribute(CHILDREN_KEY, childrenNames);
	}
	

}
