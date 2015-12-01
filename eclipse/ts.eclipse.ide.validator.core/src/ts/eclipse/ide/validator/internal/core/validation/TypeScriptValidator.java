package ts.eclipse.ide.validator.internal.core.validation;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.wst.validation.AbstractValidator;
import org.eclipse.wst.validation.internal.core.ValidationException;
import org.eclipse.wst.validation.internal.provisional.core.IReporter;
import org.eclipse.wst.validation.internal.provisional.core.IValidationContext;
import org.eclipse.wst.validation.internal.provisional.core.IValidatorJob;

/**
 * WTP TypeScript Validator V2 to validate TypeScript. This validator can be
 * called when project is Build or Validate at hand (with Validate context
 * menu).
 */
public class TypeScriptValidator extends AbstractValidator implements IValidatorJob {

	@Override
	public void cleanup(IReporter reporter) {
		// do nothing
	}

	@Override
	public void validate(IValidationContext context, IReporter reporter) throws ValidationException {
		// It seems that it is never called?
	}

	@Override
	public ISchedulingRule getSchedulingRule(IValidationContext context) {
		return null;
	}

	@Override
	public IStatus validateInJob(IValidationContext helper, IReporter reporter) throws ValidationException {
		IStatus status = Status.OK_STATUS;
		validate(helper, reporter);
		return status;
	}

}
