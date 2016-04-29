package ts.eclipse.ide.internal.ui.commands;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;

import ts.eclipse.ide.core.resources.IIDETypeScriptProject;
import ts.eclipse.ide.core.resources.buildpath.ITypeScriptBuildPath;
import ts.eclipse.ide.core.utils.TypeScriptResourceUtil;
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
				if (container != null) {
					try {
						IIDETypeScriptProject tsProject = TypeScriptResourceUtil
								.getTypeScriptProject(container.getProject());
						ITypeScriptBuildPath buildPath = getBuildPath(tsProject, buildPaths);
						if (add) {
							buildPath.addEntry(container);
						} else {
							buildPath.removeEntry(container);
						}
					} catch (CoreException e) {

					}
				} else if (add) {
					// search containers
				}
			}
			// save build paths
			for (ITypeScriptBuildPath buildPath : buildPaths.values()) {
				buildPath.save();
			}
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
