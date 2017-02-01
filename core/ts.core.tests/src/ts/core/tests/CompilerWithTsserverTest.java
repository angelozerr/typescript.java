package ts.core.tests;

import java.io.File;
import java.util.List;

import ts.client.ITypeScriptServiceClient;
import ts.client.LoggingInterceptor;
import ts.client.TypeScriptServiceClient;
import ts.client.compileonsave.CompileOnSaveAffectedFileListSingleProject;
import ts.utils.FileUtils;

public class CompilerWithTsserverTest {

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

		client.openFile(fileName, null);

		List<CompileOnSaveAffectedFileListSingleProject> projects = client.compileOnSaveAffectedFileList(fileName)
				.get();
		for (CompileOnSaveAffectedFileListSingleProject project : projects) {
			for (String file : project.getFileNames()) {
				Boolean result = client.compileOnSaveEmitFile(file, true).get();
				System.err.println(result);
			}
		}
		client.closeFile(fileName);

		client.dispose();
	}
}
