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

	private String name;
	private ITreeContentProvider contentProvider;
	private CheckboxTreeViewer treeViever;
	private String mode;

	public ShtykhContentSelectorTab(
			String mode,
			String name) {
				this.mode = mode;
				this.name = name;
	}

	@Override
	public void createControl(Composite parent) {
		IBaseLabelProvider labelProvider = new DecoratingLabelProvider(DebugUITools.newDebugModelPresentation(), PlatformUI.getWorkbench().getDecoratorManager().getLabelDecorator());
		contentProvider = new LaunchConfigurationTreeContentProvider(getLaunchConfigurationDialog().getMode(), null);
		treeViever = new ContainerCheckedTreeViewer(parent);
		treeViever.setContentProvider(contentProvider);
		treeViever.setLabelProvider(labelProvider);
		treeViever.setComparator(new WorkbenchViewerComparator());
		treeViever.setInput(ResourcesPlugin.getWorkspace().getRoot());

		treeViever.addCheckStateListener(new ICheckStateListener() {
			public void checkStateChanged(CheckStateChangedEvent event) {
				updateLaunchConfigurationDialog();
			}
		});
		setControl(treeViever.getTree());
	}
	
	@Override
	public void initializeFrom(ILaunchConfiguration configuration) {
		try {
			treeViever.setCheckedElements(CompositeConfigurationDelegate.getChildren(configuration, mode));
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		try {
			CompositeConfigurationDelegate.setChildren(configuration, mode, treeViever.getCheckedElements());
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}

	
	@Override
	public boolean isValid(ILaunchConfiguration launchConfig) {
		if (hasLoops(launchConfig, new HashSet<ILaunchConfiguration>())) {
			setErrorMessage("Configuration " + launchConfig.getName() + " has loops!");
			return false;
		} else {
			setMessage("Configuration " + launchConfig.getName() + " is perfectly fine to launch!");
			return true;
		}
	}

	private boolean hasLoops(ILaunchConfiguration launchConfig, Set<ILaunchConfiguration> ancestors) {
		if (ancestors.contains(launchConfig)) {
			return true;
		}
		ILaunchConfiguration[] children = null;
		try {
			children = CompositeConfigurationDelegate.getChildren(launchConfig, mode);
		} catch (CoreException e) {
			e.printStackTrace();
		}
		Set<ILaunchConfiguration> nextAncestors = new HashSet<>(ancestors);
		nextAncestors.add(launchConfig);
		for (ILaunchConfiguration child : children) {
			if (hasLoops(child, nextAncestors)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
		//nothing to do by default
	}
}
