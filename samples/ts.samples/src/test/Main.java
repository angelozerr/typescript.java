package test;

import java.io.File;

import ts.ICompletionEntry;
import ts.ICompletionInfo;
import ts.TSException;
import ts.server.ITypeScriptServiceClient;
import ts.server.nodejs.NodeJSTSClient;

public class Main {

	public static void main(String[] args) throws InterruptedException, TSException {

		// sample2.ts has the following content: 
		// var s = "";s.
		String fileName = "sample2.ts";
		
		// Create TypeScript client
		ITypeScriptServiceClient client = new NodeJSTSClient(new File("./samples"), new File("../../core/ts.repository/node_modules/typescript/bin/tsserver"), null);
		
		// Open "sample2.ts" in an editor
		client.openFile(fileName);
		
		// Do completion after the last dot of "s" variable which is a String (charAt, ....)
		ICompletionInfo completionInfo = client.getCompletionsAtLineOffset(fileName, 1, 14, null);
		display(completionInfo);

		// Update the editor content to set s as number
		client.updateFile(fileName, "var s = 1;s.");
				
		// Do completion after the last dot of "s" variable which is a Number (toExponential, ....)
		completionInfo = client.getCompletionsAtLineOffset(fileName, 0, 14, null);
		display(completionInfo);
		
		completionInfo = client.getCompletionsAtLineOffset("angular2.d.ts", 0, 14, null);
		System.out.println(completionInfo);
		
		client.getNavigationBarItems(fileName);
		
		client.join();
		client.dispose();
		
	}

	private static void display(ICompletionInfo completionInfo) {
		System.out.println("getCompletionsAtLineOffset:");
		ICompletionEntry[] entries = completionInfo.getEntries();
		for (ICompletionEntry entry : entries) {
			System.out.println(entry.getName());	
		}
	}
}
