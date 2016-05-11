package ts.compiler;

import ts.client.Location;

public interface ITypeScriptCompilerMessageHandler {

	void addFile(String file);

	void refreshFiles();

	void addError(String file, Location startLoc, Location endLoc,
			TypeScriptCompilerSeverity typeScriptCompilerSeverity, String code, String message);
}
