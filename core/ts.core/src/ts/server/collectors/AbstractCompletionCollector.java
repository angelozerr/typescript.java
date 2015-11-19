package ts.server.collectors;

public abstract class AbstractCompletionCollector extends AbstractTypeScriptCollector implements ICompletionCollector {

	private final String prefix;

	public AbstractCompletionCollector(String prefix) {
		this.prefix = prefix != null ? prefix : "";
	}

	@Override
	public final void addCompletionEntry(String name, String kind, String kindModifiers, String sortText) {
		if (name.toUpperCase().startsWith(prefix.toUpperCase())) {
			doAddCompletionEntry(name, kind, kindModifiers, sortText);
		}
	}

	public String getPrefix() {
		return prefix;
	}

	protected abstract void doAddCompletionEntry(String name, String kind, String kindModifiers, String sortText);
}
