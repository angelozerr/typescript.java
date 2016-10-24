package ts.eclipse.ide.internal.ui.commands;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;

import ts.eclipse.ide.core.resources.IIDETypeScriptProject;
import ts.eclipse.ide.core.resources.buildpath.ITsconfigBuildPath;
import ts.eclipse.ide.core.resources.buildpath.ITypeScriptBuildPath;
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
				IFile tsconfigFile = TypeScriptResourceUtil.getBuildPathContainer(receiver);
				IIDETypeScriptProject tsProject = null;
				if (tsconfigFile != null) {
					// The selected container contains a tsconfig.json file in
					// the root
					try {
						tsProject = TypeScriptResourceUtil.getTypeScriptProject(tsconfigFile.getProject());
						ITypeScriptBuildPath buildPath = getBuildPath(tsProject, buildPaths);
						if (add) {
							// Add the container to the build path
							buildPath.addEntry(tsconfigFile);
						} else {
							// Remove the existing container from the build path
							buildPath.removeEntry(tsconfigFile);
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
							getTsconfigFiles(tsProject.getTypeScriptBuildPath().getTsconfigBuildPaths()));
					if (dialog.open() == Window.OK) {
						List<IResource> resources = dialog.getCheckedElements();
						if (resources != null) {
							ITypeScriptBuildPath buildPath = getBuildPath(tsProject, buildPaths);
							buildPath.clear();
							for (IResource resource : resources) {
								buildPath.addEntry((IFile) resource);
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

	private List<IFile> getTsconfigFiles(ITsconfigBuildPath[] rootContainers) {
		List<IFile> files = new ArrayList<IFile>();
		for (int i = 0; i < rootContainers.length; i++) {
			files.add(rootContainers[i].getTsconfigFile());
		}
		return files;
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
