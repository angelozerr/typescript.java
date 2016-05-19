package ts.core.tests;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import ts.TypeScriptException;
import ts.cmd.tslint.ITypeScriptLint;
import ts.cmd.tslint.TSLintOptions;
import ts.cmd.tslint.TslintFormat;
import ts.cmd.tslint.TypeScriptLint;
import ts.nodejs.TraceNodejsProcess;

public class TslintTest {

	public static void main(String[] args) throws TypeScriptException {

		// sample.ts(1,14): error TS1003: Identifier expected.

		File tslintFile = new File("../ts.repository/node_modules/tslint/bin/tslint");
		ITypeScriptLint lint = new TypeScriptLint(tslintFile, null, null);

		File projectDir = new File("./samples");

		TSLintOptions options = new TSLintOptions();
		options.setFormat(TslintFormat.json);
		List<String> filenames = new ArrayList<String>();
		filenames.add("sample.ts");
		lint.execute(projectDir, options, filenames, TraceNodejsProcess.INSTANCE);
	}

}
