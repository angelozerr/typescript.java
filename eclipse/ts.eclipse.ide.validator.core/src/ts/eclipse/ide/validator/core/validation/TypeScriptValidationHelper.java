package ts.eclipse.ide.validator.core.validation;

import org.eclipse.wst.validation.internal.provisional.core.IReporter;
import org.eclipse.wst.validation.internal.provisional.core.IValidator;

import ts.eclipse.ide.core.resources.IIDETypeScriptFile;
import ts.eclipse.ide.validator.internal.core.Trace;
import ts.eclipse.ide.validator.internal.core.validation.TypeScriptReporterCollector;
import ts.resources.ITypeScriptProject;
import ts.server.geterr.ITypeScriptGeterrCollector;

public class TypeScriptValidationHelper {

	public static void validate(IIDETypeScriptFile tsFile, ITypeScriptProject tsProject, IReporter reporter,
			IValidator validator) {
		int delay = 0;
		try {
			ITypeScriptGeterrCollector collector = new TypeScriptReporterCollector(tsFile, reporter, validator);
			tsProject.geterr(tsFile, delay, collector);
		} catch (Exception e) {
			Trace.trace(Trace.SEVERE, "Error while TypeScript validation.", e);
		}
	}

}
