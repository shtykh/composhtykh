package composhtykh.tabs;

import org.eclipse.debug.ui.AbstractLaunchConfigurationTabGroup;

import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.ILaunchConfigurationTab;

public class ShtykhTabGroup extends AbstractLaunchConfigurationTabGroup {

	@Override
	public void createTabs(ILaunchConfigurationDialog dialog, String mode) {
		setTabs(new ILaunchConfigurationTab[]{new ShtykhContentSelectorTab(mode, "Children selection")});
	}
}