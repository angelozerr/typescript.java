package ts.compiler;

import ts.TypeScriptException;

public interface ITypeScriptCompiler {

	void compile(String filename) throws TypeScriptException;

	void dispose();
}
