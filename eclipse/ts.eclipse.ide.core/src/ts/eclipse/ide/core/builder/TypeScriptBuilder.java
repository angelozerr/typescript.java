package ts.eclipse.ide.core.builder;

import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import ts.TypeScriptException;
import ts.eclipse.ide.core.resources.IIDETypeScriptProject;
import ts.eclipse.ide.core.utils.TypeScriptResourceUtil;

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
		if (TypeScriptResourceUtil.hasTypeScriptNature(project)) {
			IIDETypeScriptProject tsProject = TypeScriptResourceUtil.getTypeScriptProject(project);
			try {
				tsProject.getCompiler().compile(null);
			} catch (TypeScriptException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return null;
	}

}
