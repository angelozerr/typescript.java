package ts.client.completions;

import ts.client.AbstractTypeScriptCollector;
import ts.client.ITypeScriptServiceClient;

public abstract class AbstractCompletionCollector extends AbstractTypeScriptCollector
		implements ITypeScriptCompletionCollector {

	private final String prefix;

	public AbstractCompletionCollector(String prefix) {
		this.prefix = prefix != null ? prefix : "";
	}

	@Override
	public void addCompletionEntry(String name, String kind, String kindModifiers, String sortText, String fileName,
			int line, int offset, ITypeScriptServiceClient client) {
		if (name.toUpperCase().startsWith(prefix.toUpperCase())) {
			doAddCompletionEntry(name, kind, kindModifiers, sortText, fileName, line, offset, client);
		}
	}

	public String getPrefix() {
		return prefix;
	}

	protected abstract void doAddCompletionEntry(String name, String kind, String kindModifiers, String sortText,
			String fileName, int line, int offset, ITypeScriptServiceClient client);
}
