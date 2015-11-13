package test;

import java.io.File;

import ts.CompletionInfo;
import ts.TSException;
import ts.server.ITSClient;
import ts.server.nodejs.NodeJSTSClient;

public class Main {

	public static void main(String[] args) throws InterruptedException, TSException {

		String fileName = "samples2.ts";
		ITSClient client = new NodeJSTSClient(new File("./samples"), new File("../../core/ts.repository/node_modules/typescript/bin/tsserver"), null);
		client.openFile(fileName);
		
		CompletionInfo completionInfo = client.getCompletionsAtLineOfsset(fileName, 1, 13);
		System.out.println(completionInfo);
		
		client.getNavigationBarItems(fileName);
		
		client.join();
		
	}
}
