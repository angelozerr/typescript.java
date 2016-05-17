package ts.eclipse.ide.jsdt.internal.ui.editor.contentassist;

import ts.client.ITypeScriptServiceClient;
import ts.client.completions.ICompletionEntry;
import ts.client.completions.ICompletionEntryMatcher;
import ts.eclipse.jface.text.contentassist.CompletionProposalCollector;

public class JSDTCompletionProposalCollector extends CompletionProposalCollector {

	public JSDTCompletionProposalCollector(int position, String prefix, ICompletionEntryMatcher matcher) {
		super(position, prefix, matcher);
	}

	@Override
	protected ICompletionEntry createEntry(String name, String kind, String kindModifiers, String sortText,
			String fileName, int line, int offset, ITypeScriptServiceClient client) {
		return new JSDTTypeScriptCompletionProposal(name, kind, kindModifiers, sortText, getPosition(), getPrefix(),
				fileName, line, offset, getMatcher(), client);
	}

}
