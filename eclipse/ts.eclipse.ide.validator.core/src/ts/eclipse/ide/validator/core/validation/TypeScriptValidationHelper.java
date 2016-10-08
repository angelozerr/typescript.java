package ts.eclipse.ide.validator.core.validation;

import org.eclipse.wst.validation.internal.provisional.core.IReporter;
import org.eclipse.wst.validation.internal.provisional.core.IValidator;

import ts.eclipse.ide.core.resources.IIDETypeScriptFile;
import ts.eclipse.ide.core.resources.IIDETypeScriptProject;
import ts.eclipse.ide.validator.internal.core.Trace;
import ts.eclipse.ide.validator.internal.core.validation.TypeScriptReporterCollector;

public class TypeScriptValidationHelper {

	public static void validate(IIDETypeScriptFile tsFile, IReporter reporter, IValidator validator) {
		try {
			IIDETypeScriptProject tsProject = (IIDETypeScriptProject) tsFile.getProject();
			TypeScriptReporterCollector collector = new TypeScriptReporterCollector(tsFile, reporter, validator);
			tsProject.diagnostics(tsFile, collector);
			// tsProject.getTslint().lint((IFile) tsFile.getResource(), collector);
		} catch (Exception e) {
			Trace.trace(Trace.SEVERE, "Error while TypeScript validation.", e);
		}
	}

}
