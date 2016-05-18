package ts.cmd.tslint;

import java.io.File;

import ts.cmd.AbstractCmd;

public class TypeScriptLint extends AbstractCmd<TSLintOptions> implements ITypeScriptLint {

	private static final String TSLINT_FILE_TYPE = "tslint";

	public TypeScriptLint(File tslintFile, File nodejsFile) {
		super(tslintFile, nodejsFile, TSLINT_FILE_TYPE);
	}

}
