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
import ts.compiler.CompilerOptions;
import ts.eclipse.ide.core.resources.IIDETypeScriptProject;
import ts.eclipse.ide.core.resources.buildpath.ITypeScriptBuildPath;
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
		List<IContainer> containers = buildPath.getContainers();
		for (IContainer container : containers) {
			try {
				IDETsconfigJson tsconfig = TypeScriptResourceUtil.findTsconfig(container);
				if (tsconfig == null || tsconfig.isCompileOnSave()) {
					IDETypeScriptCompilerReporter reporter = new IDETypeScriptCompilerReporter(container);
					CompilerOptions options = createCompilerOptions(tsconfig);
					tsProject.getCompiler().compile(container.getLocation().toFile(), options, null, reporter);
					for (IFile tsFile : reporter.getFilesToRefresh()) {
						try {
							TypeScriptResourceUtil.refreshAndCollectEmittedFiles(tsFile, tsconfig, true, null);
						} catch (CoreException e) {
							Trace.trace(Trace.SEVERE, "Error while tsc compilation when ts file is refreshed", e);
						}
					}
				}
			} catch (TypeScriptException e) {
				Trace.trace(Trace.SEVERE, "Error while tsc compilation", e);
			}
		}
	}

	private CompilerOptions createCompilerOptions(IDETsconfigJson tsconfig) {
		CompilerOptions options = tsconfig != null && tsconfig.getCompilerOptions() != null
				? new CompilerOptions(tsconfig.getCompilerOptions()) : new CompilerOptions();
		options.setListFiles(true);
		return options;
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
							IContainer container = buildPath.getContainer(resource);
							if (container != null) {
								List<IFile> deltas = deltaFiles.get(container);
								if (deltas == null) {
									deltas = new ArrayList<IFile>();
									deltaFiles.put(container, deltas);
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
					IDETypeScriptCompilerReporter reporter = new IDETypeScriptCompilerReporter(container);
					List<IFile> deltas = entries.getValue();
					List<String> filenames = new ArrayList<String>();
					for (IFile file : deltas) {
						filenames.add(WorkbenchResourceUtil.getRelativePath(file, container).toString());
					}
					CompilerOptions options = createCompilerOptions(tsconfig);
					tsProject.getCompiler().compile(container.getLocation().toFile(), options, filenames, reporter);
					for (IFile tsFile : reporter.getFilesToRefresh()) {
						try {
							TypeScriptResourceUtil.refreshAndCollectEmittedFiles(tsFile, tsconfig, true, null);
						} catch (CoreException e) {
							Trace.trace(Trace.SEVERE, "Error while tsc compilation when ts file is refreshed", e);
						}
					}
				}
			} catch (TypeScriptException e) {
				Trace.trace(Trace.SEVERE, "Error while tsc compilation", e);
			}
		}
	}

}
