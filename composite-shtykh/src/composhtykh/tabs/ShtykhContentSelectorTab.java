package composhtykh.tabs;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.internal.ui.launchConfigurations.LaunchConfigurationTreeContentProvider;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.DecoratingLabelProvider;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ContainerCheckedTreeViewer;
import org.eclipse.ui.model.WorkbenchViewerComparator;

import composhtykh.CompositeConfigurationDelegate;

public class ShtykhContentSelectorTab extends AbstractLaunchConfigurationTab {

	private ITreeContentProvider contentProvider;
	private CheckboxTreeViewer configurationsTree;
	
	public ShtykhContentSelectorTab() {
	}

	@Override
	public void createControl(Composite parent) {
		IBaseLabelProvider labelProvider = new DecoratingLabelProvider(DebugUITools.newDebugModelPresentation(), PlatformUI.getWorkbench().getDecoratorManager().getLabelDecorator());
		contentProvider = new LaunchConfigurationTreeContentProvider(getLaunchConfigurationDialog().getMode(), null);
		configurationsTree = new ContainerCheckedTreeViewer(parent);
		configurationsTree.setContentProvider(contentProvider);
		configurationsTree.setLabelProvider(labelProvider);
		configurationsTree.setInput(ResourcesPlugin.getWorkspace().getRoot());

		configurationsTree.addCheckStateListener(new ICheckStateListener() {
			public void checkStateChanged(CheckStateChangedEvent event) {
				updateLaunchConfigurationDialog();
			}
		});
		setControl(configurationsTree.getTree());
	}
	
	@Override
	public void initializeFrom(ILaunchConfiguration configuration) {
		try {
			configurationsTree.setCheckedElements(CompositeConfigurationDelegate.getChildren(configuration));
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		try {
			CompositeConfigurationDelegate.setChildren(configuration, toLaunchConfigurationsNames(configurationsTree.getCheckedElements()));
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}

	
	@Override
	public boolean isValid(ILaunchConfiguration launchConfig) {
		if(isEmpty(launchConfig)){
			setMessage(null);
			setWarningMessage("Configuration " + launchConfig.getName() + " is Empty!");
			setErrorMessage(null);
			return true;
		} else if (hasLoops(launchConfig, new HashSet<String>())) {
			setMessage(null);
			setErrorMessage("Configuration " + launchConfig.getName() + " has loops!");
			setWarningMessage(null);
			return false;
		} else {
			setMessage("Configuration " + launchConfig.getName() + " is perfectly fine to launch!");
			setErrorMessage(null);
			setWarningMessage(null);
			return true;
		}
	}

	private boolean isEmpty(ILaunchConfiguration launchConfig) {
		boolean result = false;
		try {
			result = CompositeConfigurationDelegate.getChildren(launchConfig).length == 0;
		} catch (CoreException e) {
			e.printStackTrace();
		}
		return result;
	}

	private boolean hasLoops(ILaunchConfiguration launchConfig, Set<String> ancestors) {
		if (ancestors.contains(launchConfig.getName())) {
			return true;
		}
		ILaunchConfiguration[] children = null;
		try {
			children = CompositeConfigurationDelegate.getChildren(launchConfig);
		} catch (CoreException e) {
			e.printStackTrace();
		}
		Set<String> nextAncestors = new HashSet<>(ancestors);
		nextAncestors.add(launchConfig.getName());
		for (ILaunchConfiguration child : children) {
			if (hasLoops(child, nextAncestors)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public String getName() {
		return "Children selection";
	}

	@Override
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
		// nothing to do here - empty selection is fine for default
	}
	
	/**
	 * 
	 * @param checkedItems - items checked on LaunchConfigurationTree
	 * @return configurationNamesSet - the set of launch configurations names
	 * @throws CoreException 
	 */
	private static Set<String> toLaunchConfigurationsNames(Object[] checkedItems) throws CoreException {
		Set<String> configurationNamesSet = new HashSet<>();
		for (Object object : checkedItems) {
			if (object instanceof ILaunchConfiguration ) {
				configurationNamesSet.add(object.toString());
			}
		}
		return configurationNamesSet;
	}
}
