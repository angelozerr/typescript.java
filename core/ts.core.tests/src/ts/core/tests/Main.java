package ts.core.tests;

import java.io.File;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import ts.TypeScriptException;
import ts.client.FileSpan;
import ts.client.ITypeScriptServiceClient;
import ts.client.Location;
import ts.client.LoggingInterceptor;
import ts.client.TypeScriptServiceClient;
import ts.client.completions.CompletionEntry;
import ts.client.diagnostics.DiagnosticEvent;
import ts.client.projectinfo.ProjectInfo;
import ts.client.quickinfo.QuickInfo;
import ts.utils.FileUtils;

public class Main {

	public static void main(String[] args) throws TypeScriptException, InterruptedException, ExecutionException {
		File projectDir = new File("./samples");
		// sample.ts has the following content:
		// var s = "";s.
		File sampleFile = new File(projectDir, "sample.ts");
		String fileName = FileUtils.getPath(sampleFile);

		// Create TypeScript client
		ITypeScriptServiceClient client = new TypeScriptServiceClient(projectDir,
				new File("../ts.repository/node_modules/typescript/bin/tsserver"), null);

		client.addInterceptor(LoggingInterceptor.getInstance());
		
		// Open "sample.ts" in an editor
		client.openFile(fileName, null);

		// compile on save
		client.compileOnSaveEmitFile(fileName, true);

		
		// Completions with line/offset
		CompletableFuture<List<CompletionEntry>> completionPromise = client.completions(fileName, 1, 14);
		List<CompletionEntry> entries = completionPromise.get();
		displayCompletions(entries);

		// Completions with position (only since TypeScript 2.0)
		// completionPromise = client.completions(fileName, 14);
		// entries = completionPromise.get();
		// displayCompletions(entries);

		// QuickInfo
		CompletableFuture<QuickInfo> quickInfoPromise = client.quickInfo(fileName, 1, 5);
		QuickInfo quickInfo = quickInfoPromise.get();
		displayQuickInfo(quickInfo);

		// Definition
		CompletableFuture<List<FileSpan>> definitionPromise = client.definition(fileName, 1, 5);
		List<FileSpan> definition = definitionPromise.get();
		displayDefinition(definition);

		// Update the editor content to set s as number
		// var s = 1;s.
		// client.changeFile(fileName, 9, 11, "1"); // position change, doesn't
		// work with TypeScript 2.1.4
		client.changeFile(fileName, 1, 9, 1, 11, "1");

		// Do completion after the last dot of "s" variable which is a Number
		// (toExponential, ....)
		completionPromise = client.completions(fileName, 1, 14);
		entries = completionPromise.get();
		displayCompletions(entries);

		// geterr
		List<DiagnosticEvent> events = client.geterr(new String[] { fileName }, 0).get();
		displayDiagnostics(events);

		// projectInfo
		ProjectInfo projectInfo = client.projectInfo(fileName, null, true).get();
		displayProjectInfo(projectInfo);
		
		client.syntacticDiagnosticsSync(fileName, true);
		//
		// client.geterrForProjectRequest(file, delay, projectInfo)

		// Close "sample.ts"
		client.closeFile(fileName);


		// synchronized (client) {t
		// try {
		// client.wait(2000);
		// } catch (InterruptedException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		// }
		client.dispose();

	}

	public static void displayCompletions(List<CompletionEntry> entries) {
		for (CompletionEntry entry : entries) {
			System.err.println(entry.getName());
		}
	}

	private static void displayQuickInfo(QuickInfo quickInfo) {
		System.err.println("DisplayString: " + quickInfo.getDisplayString() + ", start: "
				+ toString(quickInfo.getStart()) + ", end: " + toString(quickInfo.getEnd()));
	}

	private static void displayDefinition(List<FileSpan> spans) {
		for (FileSpan span : spans) {
			System.err.println("file: " + span.getFile() + ", start: " + toString(span.getStart()) + ", end: "
					+ toString(span.getEnd()));
		}
	}

	private static void displayDiagnostics(List<DiagnosticEvent> events) {
		for (DiagnosticEvent event : events) {
			System.err.println(event.getBody().getFile());
		}
	}

	private static void displayProjectInfo(ProjectInfo projectInfo) {
		System.err.println(projectInfo.getConfigFileName());
	}

	private static String toString(Location loc) {
		return "{" + loc.getLine() + ", " + loc.getOffset() + "}";
	}

}
