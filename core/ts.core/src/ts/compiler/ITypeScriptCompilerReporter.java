package ts.compiler;

public interface ITypeScriptCompilerReporter {

	void startCompilation();

	void endCompilation();

	void addError(String error);

	void addFile(String message);

}
