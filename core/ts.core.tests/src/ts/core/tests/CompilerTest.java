package ts.core.tests;

import java.io.File;

import ts.TypeScriptException;
import ts.cmd.tsc.CompilerOptions;
import ts.cmd.tsc.TypeScriptCompiler;
import ts.nodejs.INodejsProcess;
import ts.nodejs.NodejsProcessAdapter;

public class CompilerTest {

	public static void main(String[] args) throws TypeScriptException, InterruptedException {
		File tscFile = new File("../ts.repository/node_modules/typescript/bin/tsc");
		TypeScriptCompiler compiler = new TypeScriptCompiler(tscFile, null);
		
		File projectDir = new File("./samples");
		CompilerOptions options = new CompilerOptions();
		options.setListFiles(true);
		compiler.execute(projectDir, options, null, new NodejsProcessAdapter() {
			@Override
			public void onMessage(INodejsProcess process, String response) {
				System.err.println(response);
			}
		});
		
		//Thread.sleep(5000);
	}
}
