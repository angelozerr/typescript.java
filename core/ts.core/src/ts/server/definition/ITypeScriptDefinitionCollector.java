package ts.server.definition;

import ts.TSException;
import ts.server.ITypeScriptCollector;

public interface ITypeScriptDefinitionCollector extends ITypeScriptCollector {

	void addDefinition(String file, int startLine, int startOffset, int endLine, int endOffset) throws TSException;
}
