package ts.core.tests;

import java.io.File;

import ts.cmd.tslint.ITypeScriptLint;
import ts.cmd.tslint.TypeScriptLint;

public class TSLintTest {

	public TSLintTest() {
		File tslintFile = new File("../ts.repository/node_modules/tslint/bin/tslint");
		ITypeScriptLint lint = new TypeScriptLint(tslintFile, null);
		
		File projectDir = new File("./samples");
		//lint.lint(projectDir);
	}

}
