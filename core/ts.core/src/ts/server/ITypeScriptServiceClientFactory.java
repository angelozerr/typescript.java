package ts.server;

import java.io.File;

import ts.TypeScriptException;

public interface ITypeScriptServiceClientFactory {

	ITypeScriptServiceClient create(File projectDir) throws TypeScriptException;
}
