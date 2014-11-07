package composhtykh;

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
	
	private static String CHILDREN_KEY = "CHILDREN";
	
	@Override
	public void launch(ILaunchConfiguration configuration, String mode,
			ILaunch launch, IProgressMonitor monitor) throws CoreException {
		ILaunchConfiguration[] children = getChildren(configuration);
		for (ILaunchConfiguration child : children) {
			child.launch(mode, monitor);
		}
	}
	
	/**
	 * Returns children launch configurations
	 * @param configuration
	 * @return ILaunchConfigurations are to launch if there are any, empty array otherwise
	 * @throws CoreException
	 */
	public static ILaunchConfiguration[] getChildren(ILaunchConfiguration configuration) throws CoreException {
		ILaunchConfiguration[] allConfigurations = DebugPlugin.getDefault().getLaunchManager().getLaunchConfigurations();
		Set<String> childrenNames = new HashSet<String>(configuration.getAttribute(CHILDREN_KEY, new HashSet<String>()));
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
	public static void setChildren(ILaunchConfigurationWorkingCopy configuration, Set<String> childrenNames) throws CoreException {
		configuration.setAttribute(CHILDREN_KEY, childrenNames);
	}
	

}
