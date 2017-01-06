package ts.eclipse.ide.validator.core.validation;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.wst.validation.internal.provisional.core.IReporter;
import org.eclipse.wst.validation.internal.provisional.core.IValidator;

import ts.client.CommandNames;
import ts.client.diagnostics.Diagnostic;
import ts.client.diagnostics.DiagnosticEvent;
import ts.client.diagnostics.DiagnosticEventBody;
import ts.eclipse.ide.core.resources.IIDETypeScriptFile;
import ts.eclipse.ide.core.resources.IIDETypeScriptProject;
import ts.eclipse.ide.validator.internal.core.Trace;
import ts.eclipse.ide.validator.internal.core.validation.TypeScriptReporterCollector;

public class TypeScriptValidationHelper {

	public static void validate(IIDETypeScriptFile tsFile, IReporter reporter, IValidator validator) {
		try {
			IIDETypeScriptProject tsProject = (IIDETypeScriptProject) tsFile.getProject();
			TypeScriptReporterCollector collector = new TypeScriptReporterCollector(tsFile, reporter, validator);
			if (tsProject.canSupport(CommandNames.SemanticDiagnosticsSync)) {
				addDiagnostics(tsFile.semanticDiagnosticsSync(false), collector);
				addDiagnostics(tsFile.syntacticDiagnosticsSync(false), collector);
			} else {
				List<DiagnosticEvent> events = tsFile.geterr().get(5000, TimeUnit.MILLISECONDS);
				for (DiagnosticEvent event : events) {
					addDiagnostics(event.getBody(), collector);
				}
			}
		} catch (Throwable e) {
			Trace.trace(Trace.SEVERE, "Error while TypeScript validation.", e);
		}
	}

	private static void addDiagnostics(CompletableFuture<DiagnosticEventBody> promise,
			TypeScriptReporterCollector collector) throws Exception {
		DiagnosticEventBody event = promise.get(5000, TimeUnit.MILLISECONDS);
		addDiagnostics(event, collector);
	}

	public static void addDiagnostics(DiagnosticEventBody event, TypeScriptReporterCollector collector) {
		for (Diagnostic d : event.getDiagnostics()) {
			collector.addDiagnostic(null, null, d.getText(), d.getStart().getLine(), d.getStart().getOffset(),
					d.getEnd().getLine(), d.getEnd().getOffset(), d.getCategory(), d.getCode());
		}
	}

}
