package ts.eclipse.ide.validator.internal.core.validation;

import org.eclipse.core.resources.IResource;
import org.eclipse.wst.validation.internal.operations.LocalizedMessage;
import org.eclipse.wst.validation.internal.provisional.core.IMessage;
import org.eclipse.wst.validation.internal.provisional.core.IReporter;
import org.eclipse.wst.validation.internal.provisional.core.IValidator;

import ts.TypeScriptException;
import ts.client.Location;
import ts.client.diagnostics.IDiagnostic.DiagnosticCategory;
import ts.cmd.ITypeScriptLinterHandler;
import ts.cmd.Severity;
import ts.cmd.tslint.TslintHelper;
import ts.eclipse.ide.core.resources.IIDETypeScriptFile;
import ts.eclipse.ide.validator.internal.core.Trace;
import ts.nodejs.INodejsProcess;
import ts.nodejs.INodejsProcessListener;
import ts.nodejs.NodejsProcessAdapter;

public class TypeScriptReporterCollector extends NodejsProcessAdapter
		implements ITypeScriptLinterHandler, INodejsProcessListener {

	private static final String CHAR_END = "charEnd";
	private static final String CHAR_START = "charStart";
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

	public void addDiagnostic(String event, String file, String text, int startLine, int startOffset, int endLine,
			int endOffset, DiagnosticCategory category, Integer code) {
		try {
			IIDETypeScriptFile tsFile = file != null ? (IIDETypeScriptFile) this.tsFile.getProject().getOpenedFile(file)
					: this.tsFile;
			IResource resource = tsFile.getResource();
			int start = tsFile.getPosition(startLine, startOffset);
			int end = tsFile.getPosition(endLine, endOffset);
			if (start == end) {
				if (start == 0) {
					end = 1;
				} else {
					start = start - 1;
				}
			}
			LocalizedMessage message = new LocalizedMessage(getSeverity(category), text, resource);
			message.setOffset(start);
			message.setLength(end - start);
			message.setAttribute(CHAR_START, start);
			message.setAttribute(CHAR_END, end);
			message.setLineNo(startLine - 1);
			if (code != null) {
				message.setAttribute("tsCode", code);
			}
			reporter.addMessage(validator, message);
		} catch (TypeScriptException e) {
			Trace.trace(Trace.SEVERE, "Error while reporting error", e);
		}
	}

	private int getSeverity(DiagnosticCategory category) {
		switch(category) {
		case Message:
			return IMessage.LOW_SEVERITY;
		case Warning:
			return IMessage.NORMAL_SEVERITY;
		default:
			return IMessage.HIGH_SEVERITY;
		}
	}

	@Override
	public void onMessage(INodejsProcess process, String response) {
		TslintHelper.processJsonMessage(response, this);
	}

	@Override
	public void addError(String file, Location startLoc, Location endLoc, Severity severity, String code,
			String failure) {
		IResource resource = tsFile.getResource();
		int start = startLoc.getPosition();
		int end = endLoc.getPosition();
		if (start == end) {
			if (start == 0) {
				end = 1;
			} else {
				start = start - 1;
			}
		}
		// TODO: severity
		// String severity = null;
		LocalizedMessage message = new LocalizedMessage(getSeverity(severity), failure, resource);
		message.setOffset(start);
		message.setLength(end - start);
		message.setAttribute(CHAR_START, start);
		message.setAttribute(CHAR_END, end);
		message.setLineNo(startLoc.getLine());
		reporter.addMessage(validator, message);

	}

	private int getSeverity(Severity severity) {
		return 0;
	}

}
