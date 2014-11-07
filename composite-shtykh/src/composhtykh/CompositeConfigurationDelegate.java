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
		ILaunchConfiguration[] children = getChildren(configuration, mode);
		for (ILaunchConfiguration child : children) {
			child.launch(mode, monitor);
		}
	}
	/**
	 * 
	 * @param configuration
	 * @param mode
	 * @return ILaunchConfigurations are to launch if there are any. empty array otherwise
	 * @throws CoreException
	 */
	public static ILaunchConfiguration[] getChildren(ILaunchConfiguration configuration, 
			String mode) throws CoreException {
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
			throw new RuntimeException("Names of the folowing configurations had been changed and thus were lost : \n" + childrenNames.toString());			
		}
		return children.toArray(new ILaunchConfiguration[children.size()]);
	}
	
	public static void setChildren(ILaunchConfigurationWorkingCopy configuration, 
			String mode, Object[] checkedItems) throws CoreException {
		Set<String> childrenNames = toLaunchConfigurationsNames(checkedItems, mode);
		configuration.setAttribute(CHILDREN_KEY, childrenNames);
	}
	
	/**
	 * 
	 * @param checkedItems
	 * @return configurationNamesSet - the set of launchConfigurationNames
	 * @throws CoreException 
	 */
	private static Set<String> toLaunchConfigurationsNames(
			Object[] checkedItems, String mode) throws CoreException {
		Set<String> configurationNamesSet = new HashSet<>();
		for (Object object : checkedItems) {
			if (object instanceof ILaunchConfiguration ) { // TODO && ((ILaunchConfiguration)object).getModes().contains(mode)) {
				configurationNamesSet.add(object.toString());
			}
		}
		return configurationNamesSet;
	}
	

}
