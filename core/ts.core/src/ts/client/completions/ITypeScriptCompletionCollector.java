package ts.client.completions;

import ts.client.ITypeScriptCollector;
import ts.client.ITypeScriptServiceClient;

public interface ITypeScriptCompletionCollector extends ITypeScriptCollector {

	void addCompletionEntry(String name, String kind, String kindModifiers, String sortText,
			String fileName, int line, int offset, ITypeScriptServiceClient client);

}
