package ts;

public class CompletionEntry implements ICompletionEntry {

	private final String name;
	private final String kind;
	private final String kindModifiers;
	private final String sortText;

	public CompletionEntry(String name, String kind, String kindModifiers, String sortText) {
		this.name = name;
		this.kind = kind;
		this.kindModifiers = kindModifiers;
		this.sortText = sortText;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getKind() {
		return kind;
	}

	@Override
	public String getKindModifiers() {
		return kindModifiers;
	}

	@Override
	public String getSortText() {
		return sortText;
	}

}
