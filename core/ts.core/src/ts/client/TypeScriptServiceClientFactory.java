package ts.client;

import java.io.File;

import ts.TypeScriptException;

public class TypeScriptServiceClientFactory implements ITypeScriptServiceClientFactory {

	private final File tsserverFile;
	private final File nodeFile;

	public TypeScriptServiceClientFactory(File tsserverFile, File nodeFile) {
		this.tsserverFile = tsserverFile;
		this.nodeFile = nodeFile;
	}

	@Override
	public ITypeScriptServiceClient create(File projectDir) throws TypeScriptException {
		return new TypeScriptServiceClient(projectDir, tsserverFile, nodeFile);
	}

}
