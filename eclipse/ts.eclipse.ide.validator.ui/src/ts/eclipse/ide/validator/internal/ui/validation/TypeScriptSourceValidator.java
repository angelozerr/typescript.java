package ts.eclipse.ide.validator.internal.ui.validation;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.wst.sse.ui.internal.reconcile.validator.ISourceValidator;
import org.eclipse.wst.validation.internal.core.ValidationException;
import org.eclipse.wst.validation.internal.provisional.core.IReporter;
import org.eclipse.wst.validation.internal.provisional.core.IValidationContext;
import org.eclipse.wst.validation.internal.provisional.core.IValidator;

import ts.eclipse.ide.core.TypeScriptCorePlugin;
import ts.eclipse.ide.core.resources.IIDETypeScriptFile;
import ts.eclipse.ide.core.resources.IIDETypeScriptProject;
import ts.eclipse.ide.ui.utils.EditorUtils;
import ts.eclipse.ide.validator.core.validation.TypeScriptValidationHelper;
import ts.eclipse.ide.validator.internal.ui.Trace;

/**
 * WTP TypeScript Validator "as-you-type" to validate TypeScript when user type
 * content inside JSDT JavaScript editor.
 *
 */
public class TypeScriptSourceValidator implements IValidator, ISourceValidator {

	private IDocument document;

	@Override
	public void validate(IValidationContext helper, IReporter reporter) throws ValidationException {

		if (helper == null || document == null) {
			return;
		}

		if ((reporter != null) && (reporter.isCancelled() == true)) {
			throw new OperationCanceledException();
		}

		// we cannot use helper#getURI() to retrieve the IFile which is
		// validating, because
		// this helper is filled by using IStructuredModel (see
		// ReconcileStepForValidator#getFile())
		// and JSDT JavaScript Editor doesn't manage IStructuredModel
		IFile file = EditorUtils.getFile(document);
		if (file == null || !TypeScriptCorePlugin.hasTypeScriptNature(file.getProject())) {
			return;
		}

		try {
			IIDETypeScriptProject tsProject = TypeScriptCorePlugin.getTypeScriptProject(file.getProject());
			IIDETypeScriptFile tsFile = tsProject.getOpenedFile(file, document);
			TypeScriptValidationHelper.validate(tsFile, tsProject, reporter, this);
		} catch (Exception e) {
			Trace.trace(Trace.SEVERE, "Error while TypeScript validation.", e);
		}
	}

	@Override
	public void cleanup(IReporter reporter) {
		// don't need to implement
	}

	@Override
	public void connect(IDocument document) {
		this.document = document;
	}

	@Override
	public void disconnect(IDocument document) {
		this.document = null;
	}

	@Override
	public void validate(IRegion dirtyRegion, IValidationContext helper, IReporter reporter) {
		// Never called, because TypeScriptSourceValidator is declared as
		// "total" (and
		// not "partial") in the plugin.xml
		// "org.eclipse.wst.sse.ui.sourcevalidation" extension point.
	}

}
