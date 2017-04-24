package ts.eclipse.ide.validator.core.validation;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.wst.validation.internal.provisional.core.IReporter;
import org.eclipse.wst.validation.internal.provisional.core.IValidator;

import ts.client.CommandNames;
import ts.client.diagnostics.DiagnosticEvent;
import ts.client.diagnostics.DiagnosticEventBody;
import ts.client.diagnostics.IDiagnostic;
import ts.eclipse.ide.core.resources.IIDETypeScriptFile;
import ts.eclipse.ide.core.resources.IIDETypeScriptProject;
import ts.eclipse.ide.validator.internal.core.Trace;
import ts.eclipse.ide.validator.internal.core.validation.TypeScriptReporterCollector;
import ts.utils.VersionHelper;

public class TypeScriptValidationHelper {

	public static void validate(IIDETypeScriptFile tsFile, IReporter reporter, IValidator validator) {
		try {
			IIDETypeScriptProject tsProject = (IIDETypeScriptProject) tsFile.getProject();
			TypeScriptReporterCollector collector = new TypeScriptReporterCollector(tsFile, reporter, validator);
			if (tsProject.canSupport(CommandNames.SemanticDiagnosticsSync)) {
				boolean includeLinePosition = !(VersionHelper
						.canSupport(tsProject.getProjectSettings().getTypeScriptVersion(), "2.3.1"));
				addDiagnostics(tsFile.semanticDiagnosticsSync(includeLinePosition), collector);
				addDiagnostics(tsFile.syntacticDiagnosticsSync(includeLinePosition), collector);
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
		for (IDiagnostic d : event.getDiagnostics()) {
			collector.addDiagnostic(null, null, d.getFullText(), d.getStartLocation().getLine(),
					d.getStartLocation().getOffset(), d.getEndLocation().getLine(), d.getEndLocation().getOffset(),
					d.getCategory(), d.getCode());
		}
	}

}
