package ts.client.definition;

import ts.TypeScriptException;
import ts.client.ITypeScriptCollector;

public interface ITypeScriptDefinitionCollector extends ITypeScriptCollector {

	void addDefinition(String file, int startLine, int startOffset, int endLine, int endOffset) throws TypeScriptException;
}
