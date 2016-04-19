package ts.eclipse.ide.core.builder;

import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import ts.TypeScriptException;
import ts.eclipse.ide.core.resources.IIDETypeScriptProject;
import ts.eclipse.ide.core.resources.buildpath.ITypeScriptBuildPath;
import ts.eclipse.ide.core.resources.jsconfig.IDETsconfigJson;
import ts.eclipse.ide.core.utils.TypeScriptResourceUtil;
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
		if (!TypeScriptResourceUtil.hasTypeScriptNature(project)) {
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
					tsProject.getCompiler().compile(container.getLocation().toFile(), reporter);
					for (IFile tsFile : reporter.getFilesToRefresh()) {
						try {
							TypeScriptResourceUtil.refreshAndCollectCompiledFiles(tsFile, tsconfig, true, null);
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

	private void incrementalBuild(IIDETypeScriptProject tsProject, IProgressMonitor monitor) {
		// TODO Auto-generated method stub

	}

}
