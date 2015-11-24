package ts.server.completions;

import ts.server.ITypeScriptCollector;

public interface ITypeScriptCompletionCollector extends ITypeScriptCollector {

	void addCompletionEntry(String name, String kind, String kindModifiers, String sortText);

}
