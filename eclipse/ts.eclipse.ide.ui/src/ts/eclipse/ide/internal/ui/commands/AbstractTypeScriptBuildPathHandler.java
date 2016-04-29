package ts.eclipse.ide.internal.ui.commands;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;

import ts.eclipse.ide.core.resources.IIDETypeScriptProject;
import ts.eclipse.ide.core.resources.buildpath.ITypeScriptBuildPath;
import ts.eclipse.ide.core.resources.buildpath.ITypeScriptRootContainer;
import ts.eclipse.ide.core.utils.TypeScriptResourceUtil;
import ts.eclipse.ide.internal.ui.dialogs.DiscoverBuildPathDialog;
import ts.resources.ITypeScriptProject;

public abstract class AbstractTypeScriptBuildPathHandler extends AbstractHandler {

	private final boolean add;

	public AbstractTypeScriptBuildPathHandler(boolean add) {
		this.add = add;
	}

	@Override
	public final Object execute(ExecutionEvent event) throws ExecutionException {
		ISelection selection = HandlerUtil.getCurrentSelection(event);
		if (selection != null && selection instanceof IStructuredSelection) {
			Map<ITypeScriptProject, ITypeScriptBuildPath> buildPaths = new HashMap<ITypeScriptProject, ITypeScriptBuildPath>();
			for (Object receiver : ((IStructuredSelection) selection).toList()) {
				IContainer container = TypeScriptResourceUtil.getBuildPathContainer(receiver);
				IIDETypeScriptProject tsProject = null;
				if (container != null) {
					// The selected container contains a tsconfig.json file in
					// the root
					try {
						tsProject = TypeScriptResourceUtil.getTypeScriptProject(container.getProject());
						ITypeScriptBuildPath buildPath = getBuildPath(tsProject, buildPaths);
						if (add) {
							// Add the container to the build path
							buildPath.addEntry(container);
						} else {
							// Remove the existing container from the build path
							buildPath.removeEntry(container);
						}
					} catch (CoreException e) {

					}
				} else if (add) {
					// The selected container doesn't contain a tsconfig.json
					// file in the root
					// Open a dialog to select list of folders (which contains a
					// tsconfig.json) which must be added to the TypeScript
					// build path.
					IContainer selectedContainer = getSelectedContainer(receiver);
					try {
						tsProject = TypeScriptResourceUtil.getTypeScriptProject(selectedContainer.getProject());
					} catch (CoreException e) {
						return null;
					}
					// search containers
					Shell parentShell = HandlerUtil.getActiveShell(event);
					DiscoverBuildPathDialog dialog = new DiscoverBuildPathDialog(parentShell, selectedContainer);
					dialog.setInitialElementSelections(
							getContainers(tsProject.getTypeScriptBuildPath().getRootContainers()));
					if (dialog.open() == Window.OK) {
						List<IResource> resources = dialog.getCheckedElements();
						if (resources != null) {
							ITypeScriptBuildPath buildPath = getBuildPath(tsProject, buildPaths);
							buildPath.clear();
							for (IResource resource : resources) {
								buildPath.addEntry(resource);
							}
						}
					}

				}
			}
			// save build paths
			for (ITypeScriptBuildPath buildPath : buildPaths.values()) {
				buildPath.save();
			}
		}
		return null;
	}

	private List<IResource> getContainers(ITypeScriptRootContainer[] rootContainers) {
		List<IResource> containers = new ArrayList<IResource>();
		for (int i = 0; i < rootContainers.length; i++) {
			containers.add(rootContainers[i].getContainer());
		}
		return containers;
	}

	private IContainer getSelectedContainer(Object receiver) {
		if (receiver instanceof IAdaptable) {
			return (IContainer) ((IAdaptable) receiver).getAdapter(IContainer.class);
		}
		return null;
	}

	private ITypeScriptBuildPath getBuildPath(IIDETypeScriptProject tsProject,
			Map<ITypeScriptProject, ITypeScriptBuildPath> buildPaths) {
		ITypeScriptBuildPath buildPath = buildPaths.get(tsProject);
		if (buildPath != null) {
			return buildPath;
		}
		buildPath = tsProject.getTypeScriptBuildPath().copy();
		buildPaths.put(tsProject, buildPath);
		return buildPath;
	}

}
