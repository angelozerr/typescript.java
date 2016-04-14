package ts.client.occurrences;

import ts.TypeScriptException;
import ts.client.ITypeScriptCollector;

public interface ITypeScriptOccurrencesCollector extends ITypeScriptCollector {

	void addOccurrence(String file, int startLine, int startOffset, int endLine, int endOffset, boolean isWriteAccess)
			throws TypeScriptException;

}
