package ts.eclipse.ide.core.builder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import ts.TypeScriptException;
import ts.client.CommandNames;
import ts.eclipse.ide.core.resources.IIDETypeScriptProject;
import ts.eclipse.ide.core.resources.buildpath.ITsconfigBuildPath;
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
			// TODO: how it works? Start several tsc for each TypeScript Root
			// container?
			// this.fullBuild(tsProject, monitor);
			break;
		}
		return null;
	}

	private void fullBuild(IIDETypeScriptProject tsProject, IProgressMonitor monitor) throws CoreException {
		ITypeScriptBuildPath buildPath = tsProject.getTypeScriptBuildPath();
		ITsconfigBuildPath[] tsContainers = buildPath.getTsconfigBuildPaths();
		for (int i = 0; i < tsContainers.length; i++) {
			ITsconfigBuildPath tsContainer = tsContainers[i];
			try {
				IDETsconfigJson tsconfig = tsContainer.getTsconfig();
				if (tsconfig == null || tsconfig.isCompileOnSave()) {
					tsProject.getCompiler().compile(tsconfig, null);
				}
			} catch (TypeScriptException e) {
				Trace.trace(Trace.SEVERE, "Error while tsc compilation", e);
			}
		}
	}

	private void incrementalBuild(IIDETypeScriptProject tsProject, IProgressMonitor monitor) throws CoreException {

		final ITypeScriptBuildPath buildPath = tsProject.getTypeScriptBuildPath();
		final Map<ITsconfigBuildPath, List<IFile>> tsFilesToCompile = new HashMap<ITsconfigBuildPath, List<IFile>>();
		final Map<ITsconfigBuildPath, List<IFile>> tsFilesToDelete = new HashMap<ITsconfigBuildPath, List<IFile>>();
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
							addTsFile(buildPath, tsFilesToCompile, resource);
						}
						break;
					case IResourceDelta.REMOVED:
						if (TypeScriptResourceUtil.isTsOrTsxFile(resource)) {
							addTsFile(buildPath, tsFilesToDelete, resource);
						}
						break;
					}
					return false;
				}
				return false;
			}

			private void addTsFile(final ITypeScriptBuildPath buildPath,
					final Map<ITsconfigBuildPath, List<IFile>> tsFiles, IResource resource) {
				ITsconfigBuildPath tsContainer = buildPath.findTsconfigBuildPath(resource);
				if (tsContainer != null) {
					List<IFile> deltas = tsFiles.get(tsContainer);
					if (deltas == null) {
						deltas = new ArrayList<IFile>();
						tsFiles.put(tsContainer, deltas);
					}
					deltas.add((IFile) resource);
				}
			}
		});

		// Compile ts files *.ts
		if (tsProject.canSupport(CommandNames.compileOnSaveEmitFile)) {
			// TypeScript >=2.0.5: compile is done with tsserver
			compileWithTsserver(tsProject, tsFilesToCompile);
		} else {
			// TypeScript < 2.0.5: compile is done with tsc which is not very
			// performant.
			compileWithTsc(tsProject, tsFilesToCompile);
		}

		// Delete emitted files *.js, *.js.map
		for (Entry<ITsconfigBuildPath, List<IFile>> entries : tsFilesToDelete.entrySet()) {
			ITsconfigBuildPath tsContainer = entries.getKey();
			List<IFile> tsFiles = entries.getValue();
			IDETsconfigJson tsconfig = tsContainer.getTsconfig();
			for (IFile tsFile : tsFiles) {
				TypeScriptResourceUtil.deleteEmittedFiles(tsFile, tsconfig);
			}

		}
	}

	/**
	 * Compile the given ts files with tsserver by consumming
	 * "compileOnSaveEmitFile" command.
	 * 
	 * @param tsProject
	 * @param tsFilesToCompile
	 * @throws CoreException
	 */
	private void compileWithTsserver(IIDETypeScriptProject tsProject,
			final Map<ITsconfigBuildPath, List<IFile>> tsFilesToCompile) {
		for (Entry<ITsconfigBuildPath, List<IFile>> entries : tsFilesToCompile.entrySet()) {
			try {
				ITsconfigBuildPath tsContainer = entries.getKey();
				List<IFile> tsFiles = entries.getValue();
				IDETsconfigJson tsconfig = tsContainer.getTsconfig();
				if (tsconfig.isCompileOnSave()) {
					// tsconfig.json defines compileOnSave
					// loop for each ts files to compile
					for (IFile tsFile : tsFiles) {
						try {
							// compile the current ts file with
							// "compileOnSaveEmitFile"
							tsProject.getClient().compileOnSaveEmitFile(WorkbenchResourceUtil.getFileName(tsFile),
									null);
							// TODO: Refresh UI
							TypeScriptResourceUtil.refreshAndCollectEmittedFiles(tsFile, tsconfig, true, null);
							//tsconfig.getTsconfigFile().getParent().refreshLocal(IResource.DEPTH_INFINITE, null);
						} catch (TypeScriptException e) {
							Trace.trace(Trace.SEVERE, "Error while tsserver compilation", e);
						}
					}
				}
			} catch (Exception e) {
				Trace.trace(Trace.SEVERE, "Error while tsserver compilation", e);
			}
		}
	}

	/**
	 * Compile the given ts files with tsc.
	 * 
	 * @param tsProject
	 * @param tsFilesToCompile
	 * @throws CoreException
	 */
	private void compileWithTsc(IIDETypeScriptProject tsProject,
			final Map<ITsconfigBuildPath, List<IFile>> tsFilesToCompile) throws CoreException {
		for (Entry<ITsconfigBuildPath, List<IFile>> entries : tsFilesToCompile.entrySet()) {
			ITsconfigBuildPath tsContainer = entries.getKey();
			List<IFile> tsFiles = entries.getValue();
			try {
				IDETsconfigJson tsconfig = tsContainer.getTsconfig();
				// compile ts files
				tsProject.getCompiler().compile(tsconfig, tsFiles);
				// validate ts files with tslint
				tsProject.getTslint().lint(tsconfig, tsFiles, tsProject.getProjectSettings());
			} catch (TypeScriptException e) {
				Trace.trace(Trace.SEVERE, "Error while tsc compilation", e);
			}
		}
	}
}
