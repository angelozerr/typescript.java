package ts.eclipse.ide.validator.internal.core.validation;

import org.eclipse.core.resources.IResource;
import org.eclipse.wst.validation.internal.operations.LocalizedMessage;
import org.eclipse.wst.validation.internal.provisional.core.IMessage;
import org.eclipse.wst.validation.internal.provisional.core.IReporter;
import org.eclipse.wst.validation.internal.provisional.core.IValidator;

import ts.TSException;
import ts.eclipse.ide.core.resources.IIDETypeScriptFile;
import ts.eclipse.ide.validator.internal.core.Trace;
import ts.resources.ITypeScriptFile;
import ts.server.geterr.ITypeScriptGeterrCollector;

public class TypeScriptReporterCollector implements ITypeScriptGeterrCollector {

	private static final String CHAR_END = "charEnd";
	private static final String CHAR_START = "charStart";
	private static final String WARNING_SEVERITY = "warning";
	private final IIDETypeScriptFile tsFile;
	// private final ITypeScriptProject tsProject;
	private final IReporter reporter;
	private final IValidator validator;

	public TypeScriptReporterCollector(IIDETypeScriptFile tsFile, IReporter reporter, IValidator validator) {
		// this.tsProject = tsProject;
		this.tsFile = tsFile;
		this.reporter = reporter;
		this.validator = validator;
	}

	@Override
	public void addDiagnostic(String event, String file, String text, int startLine, int startOffset, int endLine,
			int endOffset) {
		try {
			IResource resource = tsFile.getResource();
			int start = tsFile.getPosition(startLine, startOffset);
			int end = tsFile.getPosition(endLine, endOffset);
			if (start == end) {
				if (start == 0) {
					end = 1;
				} else {
					start = start -1;
				}
			}
			// TODO: severity
			String severity = null;
			LocalizedMessage message = new LocalizedMessage(getSeverity(severity), text, resource);
			message.setOffset(start);
			message.setLength(end - start);
			message.setAttribute(CHAR_START, start);
			message.setAttribute(CHAR_END, end);
			message.setLineNo(startLine - 1);
			reporter.addMessage(validator, message);
		} catch (TSException e) {
			Trace.trace(Trace.SEVERE, "Error while reporting error", e);
		}
	}

	private int getSeverity(String severity) {
		return WARNING_SEVERITY.equals(severity) ? IMessage.NORMAL_SEVERITY : IMessage.HIGH_SEVERITY;
	}

}
