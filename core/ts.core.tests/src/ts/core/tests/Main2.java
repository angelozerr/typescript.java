package ts.core.tests;

import java.io.File;
import java.io.IOException;

import ts.TypeScriptException;
import ts.client.completions.ICompletionEntry;
import ts.client.completions.ICompletionInfo;
import ts.client.definition.DefinitionsInfo;
import ts.client.diagnostics.ITypeScriptGeterrCollector;
import ts.utils.FileUtils;

public class Main2 {

	public static void main(String[] args) throws InterruptedException, TypeScriptException, IOException {

		File projectDir = new File("./samples");
		// sample2.ts has the following content:
		// var s = "";s.
		File sampleFile = new File(projectDir, "sample.ts");
		String fileName = FileUtils.getPath(sampleFile);

		
		MockTypeScriptProject tsProject = new MockTypeScriptProject(projectDir);
		
		
		validate(sampleFile, tsProject, false);
		
		
		
//		// Create TypeScript client
//		ITypeScriptServiceClient client = new NodeJSTypeScriptServiceClient(projectDir,
//				new File("../ts.repository/node_modules/typescript/bin/tsserver"), null);
//
//		// Open "sample2.ts" in an editor
//		client.openFile(fileName, null);
//
//		// Do completion after the last dot of "s" variable which is a String
//		// (charAt, ....)
//		CompletionInfo completionInfo = new CompletionInfo(null);
//		client.completions(fileName, 1, 14, null, completionInfo);
//		display(completionInfo);
//
//		client.geterr(new String[]{fileName}, 0, new ITypeScriptGeterrCollector() {
//			
//			@Override
//			public void addDiagnostic(String event, String file, String text, int startLine, int startOffset, int endLine,
//					int endOffset) {
//				System.err.println(event);
//			}
//		});
//		
//		client.join();
//		client.dispose();

	}

	private static void validate(File sampleFile, MockTypeScriptProject tsProject, boolean normalize) throws TypeScriptException {
		MockTypeScriptFile tsFile = tsProject.openFile(sampleFile, normalize);
		tsFile.getProject().geterr(tsFile, 0, new ITypeScriptGeterrCollector() {
			
			@Override
			public void addDiagnostic(String event, String file, String text, int startLine, int startOffset, int endLine,
					int endOffset) {
				System.err.println(event);
			}
		});
	}

	private static void display(DefinitionsInfo definitionInfo) {
		// TODO Auto-generated method stub

	}

	private static void display(ICompletionInfo completionInfo) {
		System.out.println("getCompletionsAtLineOffset:");
		ICompletionEntry[] entries = completionInfo.getEntries();
		for (ICompletionEntry entry : entries) {
			System.out.println(entry.getName());
		}
	}
}
