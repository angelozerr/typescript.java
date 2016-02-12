package ts.client.completions;

import ts.client.ITypeScriptCollector;

public interface ITypeScriptCompletionEntryDetailsCollector extends ITypeScriptCollector {

	void setEntryDetails(String name, String kind, String kindModifiers);
	
	void addDisplayPart(String text, String kind);
	
	void addDocumentation(String text, String kind);

}
