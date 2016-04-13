package ts.eclipse.ide.internal.core;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Path;

import ts.eclipse.ide.core.TypeScriptCorePlugin;
import ts.eclipse.ide.internal.core.resources.IDEResourcesManager;
import ts.utils.FileUtils;

public class TypeScriptNatureTester extends PropertyTester {

	private static final String IS_TYPESCRIPT_PROJECT_PROPERTY = "isTypeScriptProject";
	private static final String HAS_TYPESCRIPT_BUILDER_PROPERTY = "hasTypeScriptBuilder";
	private static final String IS_COMPILED_TYPESCRIPT_RESOURCE = "isCompiledTypeScriptResource";

	public TypeScriptNatureTester() {
		// Default constructor is required for property tester
	}

	/**
	 * Tests if the receiver object is a project is a TypeScript project
	 * 
	 * @return true if the receiver object is a Project that has a nature that
	 *         is treated as TypeScript nature, otherwise false is returned
	 */
	@Override
	public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {

		if (IS_TYPESCRIPT_PROJECT_PROPERTY.equals(property)) {
			return testIsTypeScriptProject(receiver);
		} else if (HAS_TYPESCRIPT_BUILDER_PROPERTY.equals(property)) {
			return testHasTypeScriptBuilder(receiver);
		} else if (IS_COMPILED_TYPESCRIPT_RESOURCE.equals(property)) {
			return testIsCompiledTypeScriptResource(receiver);

		}
		return false;
	}

	private boolean testIsTypeScriptProject(Object receiver) {
		if (receiver instanceof IAdaptable) {
			IProject project = (IProject) ((IAdaptable) receiver).getAdapter(IProject.class);
			if (project != null) {
				return TypeScriptCorePlugin.hasTypeScriptNature(project);
			}
		}
		return false;
	}

	private boolean testHasTypeScriptBuilder(Object receiver) {
		if (receiver instanceof IAdaptable) {
			IProject project = (IProject) ((IAdaptable) receiver).getAdapter(IProject.class);
			if (project != null) {
				return TypeScriptCorePlugin.hasTypeScriptBuilder(project);
			}
		}
		return false;
	}

	private boolean testIsCompiledTypeScriptResource(Object receiver) {
		if (receiver instanceof IAdaptable) {
			IFile file = (IFile) ((IAdaptable) receiver).getAdapter(IFile.class);
			if (file != null) {
				if (IDEResourcesManager.getInstance().isJsFile(file)
						|| IDEResourcesManager.getInstance().isSourceMapFile(file)) {
					// check if there is a linked TypeScript file
					// TODO: improve that by using tsconfig.json config (out
					// folder)
					return file.getParent().exists(new Path(
							FileUtils.getFileNameWithoutExtension(file.getName()) + "." + FileUtils.TS_EXTENSION));
				}
			}
		}
		return false;
	}
}
