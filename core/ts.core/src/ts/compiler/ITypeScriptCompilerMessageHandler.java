package ts.compiler;

public interface ITypeScriptCompilerMessageHandler {

	void addFile(String file);

	void refreshFiles();
}
