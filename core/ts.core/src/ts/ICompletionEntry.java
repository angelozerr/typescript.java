package ts;

public interface ICompletionEntry {

	ICompletionEntry[] EMPTY_ENTRIES = new ICompletionEntry[0];

	String getName();

	String getKind();

	String getKindModifiers();

	String getSortText();
}
