package ts.client.completions;

import ts.client.ITypeScriptCollector;

public interface ITypeScriptCompletionCollector extends ITypeScriptCollector {

	void addCompletionEntry(String name, String kind, String kindModifiers, String sortText);

}
