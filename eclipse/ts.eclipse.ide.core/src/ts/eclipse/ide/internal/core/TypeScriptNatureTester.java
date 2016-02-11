package ts.eclipse.ide.internal.core;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IAdaptable;

import ts.eclipse.ide.core.TypeScriptCorePlugin;

public class TypeScriptNatureTester extends PropertyTester {

	private static final String IS_TYPESCRIPT_PROJECT_PROPERTY = "isTypeScriptProject";
	private static final String HAS_TYPESCRIPT_BUILDER_PROPERTY ="hasTypeScriptBuilder";

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
}
