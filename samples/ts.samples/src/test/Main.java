package test;

import java.io.File;

import ts.CompletionInfo;
import ts.TSException;
import ts.server.ITSClient;
import ts.server.nodejs.NodeJSTSClient;

public class Main {

	public static void main(String[] args) throws InterruptedException, TSException {

		String fileName = "sample2.ts";
		ITSClient client = new NodeJSTSClient(new File("./samples"), new File("../../core/ts.repository/node_modules/typescript/bin/tsserver"), null);
		client.openFile(fileName);
		
		CompletionInfo completionInfo = client.getCompletionsAtLineOffset(fileName, 0, 14);
		System.out.println(completionInfo);
		
		completionInfo = client.getCompletionsAtLineOffset(fileName, 0, 14);
		System.out.println(completionInfo);
		
		completionInfo = client.getCompletionsAtLineOffset("sample.ts", 0, 14);
		System.out.println(completionInfo);
		
		completionInfo = client.getCompletionsAtLineOffset("angular2.d.ts", 0, 14);
		System.out.println(completionInfo);
		
		client.getNavigationBarItems(fileName);
		
		client.join();
		
	}
}
