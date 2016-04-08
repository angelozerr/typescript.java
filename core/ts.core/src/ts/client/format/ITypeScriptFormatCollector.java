package ts.client.format;

import ts.TypeScriptException;
import ts.client.ITypeScriptCollector;

public interface ITypeScriptFormatCollector extends ITypeScriptCollector {

	void format(int startLine, int startOffset, int endLine, int endOffset, String newText) throws TypeScriptException;
}
