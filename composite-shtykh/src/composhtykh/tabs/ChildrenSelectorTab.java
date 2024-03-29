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
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ContainerCheckedTreeViewer;

import composhtykh.CompositeConfigurationDelegate;

public class ChildrenSelectorTab extends AbstractLaunchConfigurationTab {
	
	/*
	 * configurationsTree for interaction with user
	 */
	private CheckboxTreeViewer configurationsTree;

	/**
	 * Initializes configurationsTree CheckboxTreeViewer for interaction with user
	 */
	@Override
	public void createControl(Composite parent) {
		ILabelDecorator labelDecorator = PlatformUI.getWorkbench().
										getDecoratorManager().
										getLabelDecorator();
		IBaseLabelProvider labelProvider = new DecoratingLabelProvider(
										DebugUITools.newDebugModelPresentation(), 
										labelDecorator);
		String mode = getLaunchConfigurationDialog().getMode();
		ITreeContentProvider contentProvider = new LaunchConfigurationTreeContentProvider(mode, null);
		configurationsTree = new ContainerCheckedTreeViewer(parent);
		configurationsTree.setContentProvider(contentProvider);
		configurationsTree.setLabelProvider(labelProvider);
		configurationsTree.setInput(ResourcesPlugin.getWorkspace());
		configurationsTree.addCheckStateListener(new ICheckStateListener() {
			public void checkStateChanged(CheckStateChangedEvent event) {
				updateLaunchConfigurationDialog();
			}
		});
		setControl(configurationsTree.getTree());
	}
	
	/**
	 * Reflect configuration information into configurationsTree 
	 * 
	 * @param configuration launch configuration
	 */
	@Override
	public void initializeFrom(ILaunchConfiguration configuration) {
		Object[] children = CompositeConfigurationDelegate.childrenOf(configuration);
		configurationsTree.setCheckedElements(children);
	}
	
	/**
	 * Reflect information from configurationsTree into the given 
	 * launch configuration.
	 * 
	 * @param configuration launch configuration
	 */
	@Override
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		Object[] checkedElements = configurationsTree.getCheckedElements();
		Set<String> launchConfigurationsNames = toLaunchConfigurationsNames(checkedElements);
		CompositeConfigurationDelegate.adoptChildren(configuration, launchConfigurationsNames);
	}
	
	/*
	 * Returns names of all checked launch configurations on LaunchConfigurationTree
	 * @param checkedItems - items checked on LaunchConfigurationTree
	 * @return configurationNamesSet - the set of launch configurations names
	 * @throws CoreException 
	 */
	private static Set<String> toLaunchConfigurationsNames(Object[] checkedItems) {
		Set<String> configurationNamesSet = new HashSet<>();
		for (Object object : checkedItems) {
			if (object instanceof ILaunchConfiguration ) {
				configurationNamesSet.add(object.toString());
			}
		}
		return configurationNamesSet;
	}

	/**
	 * Returns whether launchConfig is valid to launch; 
	 * Sets actual [error / warning] messages for user 
	 */
	@Override
	public boolean isValid(ILaunchConfiguration launchConfig) {
		String configname = launchConfig.getName();
		if(isEmpty(launchConfig)){
			setMessage(null);
			setWarningMessage("Configuration " + configname + " is Empty!");
			setErrorMessage(null);
			return true; // Launching empty set of configurations isn't forbidden. Why not?
		} else if (hasLoops(launchConfig, new HashSet<String>())) {
			setErrorMessage("Configuration " + configname + " has loops!");
			setMessage(null);
			setWarningMessage(null);
			return false;
		} else {
			setMessage("Configuration " + configname + " is perfectly fine to launch!");
			setErrorMessage(null);
			setWarningMessage(null);
			return true;
		}
	}
	
	/*
	 * Returns whether launchConfig has no children to launch
	 * @param launchConfig - tested composite configuration
	 */
	private boolean isEmpty(ILaunchConfiguration launchConfig) {
		return CompositeConfigurationDelegate.childrenOf(launchConfig).length == 0;
	}
	
	/*
	 * Returns  whether launchConfig's launch will cause launchConfig's launch  
	 * @param launchConfig - tested ILaunchConfiguration
	 * @param ancestors - names of ILaunchConfigurations currently testing on previous layers of recursion
	 */
	private boolean hasLoops(ILaunchConfiguration launchConfig, Set<String> ancestors) {
		if (ancestors.contains(launchConfig.getName())) {
			return true;
		}
		ILaunchConfiguration[] children = CompositeConfigurationDelegate.childrenOf(launchConfig);
		if (children == null || children.length > 0) {
			Set<String> nextAncestors = new HashSet<>(ancestors);
			nextAncestors.add(launchConfig.getName());
			for (ILaunchConfiguration child : children) {
				if (hasLoops(child, nextAncestors)) {
					return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * Name of the tab will be a constant:
	 * "Children selection"
	 */
	@Override
	public String getName() {
		return "Children selection";
	}
	
	/**
	 * Nothing to do here - empty selection is fine for default
	 */
	@Override
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {}
}
