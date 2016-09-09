package ts.core.tests;

import java.io.File;
import java.io.IOException;

import ts.TypeScriptException;
import ts.client.ITypeScriptServiceClient;
import ts.client.TypeScriptServiceClient;
import ts.client.completions.ICompletionEntry;
import ts.client.completions.ICompletionInfo;
import ts.client.definition.DefinitionsInfo;
import ts.client.navbar.ITypeScriptNavBarCollector;
import ts.client.navbar.NavigationBarItemRoot;
import ts.utils.FileUtils;

public class NavBarTest {

	public static void main(String[] args) throws InterruptedException, TypeScriptException, IOException {

		File projectDir = new File("./samples");
		// sample2.ts has the following content:
		// var s = "";s.
		File sampleFile = new File(projectDir, "vscode.d.ts");
		final String fileName = FileUtils.getPath(sampleFile);

		// Create TypeScript client
		final ITypeScriptServiceClient client = new TypeScriptServiceClient(projectDir,
				new File("../ts.repository/node_modules/typescript/bin/tsserver"), null);

		// Open "sample2.ts" in an editor
		Thread t1 = new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					client.openFile(fileName, null);
				} catch (TypeScriptException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});

		Thread t2 = new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					client.navbar(fileName, null, new ITypeScriptNavBarCollector() {

						@Override
						public void setNavBar(NavigationBarItemRoot root) {
							System.err.println(root);
						}
					});
				} catch (TypeScriptException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});

		t1.start();
		t2.start();
		// client.join();
		// client.dispose();

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
