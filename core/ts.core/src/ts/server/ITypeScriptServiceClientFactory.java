package ts.server;

import java.io.File;

import ts.TSException;

public interface ITypeScriptServiceClientFactory {

	ITypeScriptServiceClient create(File projectDir) throws TSException;
}
