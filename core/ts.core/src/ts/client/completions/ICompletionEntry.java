package ts.client.completions;

import ts.client.IKindProvider;

public interface ICompletionEntry extends IKindProvider{

	ICompletionEntry[] EMPTY_ENTRIES = new ICompletionEntry[0];

	String getName();

	String getSortText();
}
