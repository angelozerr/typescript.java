package ts.client.completions;

import java.util.ArrayList;
import java.util.List;

public class CompletionInfo extends AbstractCompletionCollector implements ICompletionInfo {

	private final List<ICompletionEntry> entries;

	public CompletionInfo(String prefix) {
		super(prefix);
		this.entries = new ArrayList<ICompletionEntry>();
	}

	@Override
	protected void doAddCompletionEntry(String name, String kind, String kindModifiers, String sortText) {
		entries.add(new CompletionEntry(name, kind, kindModifiers, sortText));
	}

	@Override
	public boolean isMemberCompletion() {
		return false;
	}

	@Override
	public boolean isNewIdentifierLocation() {
		return false;
	}

	@Override
	public ICompletionEntry[] getEntries() {
		return entries.toArray(ICompletionEntry.EMPTY_ENTRIES);
	}

}
