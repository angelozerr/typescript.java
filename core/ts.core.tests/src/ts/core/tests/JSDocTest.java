package ts.core.tests;

import java.io.File;
import java.util.concurrent.TimeUnit;

import ts.client.ITypeScriptServiceClient;
import ts.client.LoggingInterceptor;
import ts.client.TypeScriptServiceClient;
import ts.client.jsdoc.TextInsertion;
import ts.utils.FileUtils;

public class JSDocTest {

	public static void main(String[] args) throws Exception {
		File projectDir = new File("./samples");
		// sample.ts has the following content:
		// var s = "";s.
		File sampleFile = new File(projectDir, "sample.ts");
		String fileName = FileUtils.getPath(sampleFile);

		// Create TypeScript client
		ITypeScriptServiceClient client = new TypeScriptServiceClient(projectDir,
				new File("../ts.repository/node_modules/typescript/bin/tsserver"), null);

		client.addInterceptor(LoggingInterceptor.getInstance());

		client.openFile(fileName, "function f(a) {return 1;}");
		TextInsertion insertion = client.docCommentTemplate(fileName, 1, 1).get(5000, TimeUnit.MILLISECONDS);
		display(insertion);
		
		client.closeFile(fileName);
		client.openFile(fileName, "var b;\nfunction f(a) {return 1;}");
		insertion = client.docCommentTemplate(fileName, 1, 8).get(5000, TimeUnit.MILLISECONDS);
		display(insertion);
		
		client.closeFile(fileName);
		client.openFile(fileName, "function f(a) {return 1;}");
		insertion = client.docCommentTemplate(fileName, 1, 1).get(5000, TimeUnit.MILLISECONDS);
		display(insertion);
		
		client.dispose();
	}

	private static void display(TextInsertion insertion) {
		System.err.println("newText:" + insertion.getNewText() + ", caretOffset:" + insertion.getCaretOffset());
		
	}
}
