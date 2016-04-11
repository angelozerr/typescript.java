package ts.client.references;

import ts.TypeScriptException;
import ts.client.ITypeScriptCollector;

public interface ITypeScriptReferencesCollector extends ITypeScriptCollector {

	void ref(String file, int startLine, int startOffset, int endLine, int endOffset, String lineText)
			throws TypeScriptException;

}
