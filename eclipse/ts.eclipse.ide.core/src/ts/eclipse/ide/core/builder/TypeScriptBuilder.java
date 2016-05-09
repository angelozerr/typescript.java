package ts.eclipse.ide.core.builder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import ts.TypeScriptException;
import ts.eclipse.ide.core.resources.IIDETypeScriptProject;
import ts.eclipse.ide.core.resources.buildpath.ITypeScriptBuildPath;
import ts.eclipse.ide.core.resources.buildpath.ITypeScriptRootContainer;
import ts.eclipse.ide.core.resources.jsconfig.IDETsconfigJson;
import ts.eclipse.ide.core.utils.TypeScriptResourceUtil;
import ts.eclipse.ide.core.utils.WorkbenchResourceUtil;
import ts.eclipse.ide.internal.core.Trace;

/**
 * Builder to transpiles TypeScript files into JavaScript files and source map
 * if needed.
 *
 */
public class TypeScriptBuilder extends IncrementalProjectBuilder {

	public static final String ID = "ts.eclipse.ide.core.typeScriptBuilder";

	@Override
	protected IProject[] build(int kind, Map<String, String> args, final IProgressMonitor monitor)
			throws CoreException {
		IProject project = this.getProject();
		if (!TypeScriptResourceUtil.isTypeScriptProject(project)) {
			return null;
		}

		IIDETypeScriptProject tsProject = TypeScriptResourceUtil.getTypeScriptProject(project);
		// incremental or full build
		switch (kind) {
		case IncrementalProjectBuilder.AUTO_BUILD:
		case IncrementalProjectBuilder.INCREMENTAL_BUILD:
			this.incrementalBuild(tsProject, monitor);
			break;
		case IncrementalProjectBuilder.FULL_BUILD:
			this.fullBuild(tsProject, monitor);
			break;
		}
		return null;
	}

	private void fullBuild(IIDETypeScriptProject tsProject, IProgressMonitor monitor) throws CoreException {
		ITypeScriptBuildPath buildPath = tsProject.getTypeScriptBuildPath();
		ITypeScriptRootContainer[] containers = buildPath.getRootContainers();
		for (int i = 0; i < containers.length; i++) {
			IContainer container = containers[i].getContainer();
			try {
				IDETsconfigJson tsconfig = TypeScriptResourceUtil.findTsconfig(container);
				if (tsconfig == null || tsconfig.isCompileOnSave()) {
					tsProject.getCompiler().compile(tsconfig);
				}
			} catch (TypeScriptException e) {
				Trace.trace(Trace.SEVERE, "Error while tsc compilation", e);
			}
		}
	}

	private void incrementalBuild(IIDETypeScriptProject tsProject, IProgressMonitor monitor) throws CoreException {
		final ITypeScriptBuildPath buildPath = tsProject.getTypeScriptBuildPath();
		final Map<IContainer, List<IFile>> deltaFiles = new HashMap<IContainer, List<IFile>>();
		IResourceDelta delta = getDelta(tsProject.getProject());
		delta.accept(new IResourceDeltaVisitor() {

			@Override
			public boolean visit(IResourceDelta delta) throws CoreException {
				IResource resource = delta.getResource();
				if (resource == null) {
					return false;
				}
				switch (resource.getType()) {
				case IResource.ROOT:
					return true;
				case IResource.PROJECT:
					return TypeScriptResourceUtil.isTypeScriptProject((IProject) resource);
				case IResource.FOLDER:
					return buildPath.isInScope(resource);
				case IResource.FILE:
					int kind = delta.getKind();
					switch (kind) {
					case IResourceDelta.ADDED:
					case IResourceDelta.CHANGED:
						if (TypeScriptResourceUtil.isTsOrTsxFile(resource)) {
							ITypeScriptRootContainer tsContainer = buildPath.findRootContainer(resource);
							if (tsContainer != null) {
								List<IFile> deltas = deltaFiles.get(tsContainer.getContainer());
								if (deltas == null) {
									deltas = new ArrayList<IFile>();
									deltaFiles.put(tsContainer.getContainer(), deltas);
								}
								deltas.add((IFile) resource);
							}
						}
					}
					return false;
				}
				return false;
			}
		});

		for (Entry<IContainer, List<IFile>> entries : deltaFiles.entrySet()) {
			IContainer container = entries.getKey();
			try {
				IDETsconfigJson tsconfig = TypeScriptResourceUtil.findTsconfig(container);
				if (tsconfig == null || tsconfig.isCompileOnSave()) {
					List<IFile> deltas = entries.getValue();
					List<String> filenames = new ArrayList<String>();
					for (IFile file : deltas) {
						filenames.add(WorkbenchResourceUtil.getRelativePath(file, container).toString());
					}
					tsProject.getCompiler().compile(tsconfig, filenames);					
				}
			} catch (TypeScriptException e) {
				Trace.trace(Trace.SEVERE, "Error while tsc compilation", e);
			}
		}
	}

}
