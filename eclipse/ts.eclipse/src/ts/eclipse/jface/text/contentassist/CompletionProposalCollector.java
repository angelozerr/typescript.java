package ts.eclipse.jface.text.contentassist;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposal;

import ts.server.completions.AbstractCompletionCollector;

public class CompletionProposalCollector extends AbstractCompletionCollector {

	private final List<ICompletionProposal> proposals;
	private final int position;

	public CompletionProposalCollector(int position, String prefix) {
		super(prefix);
		this.position = position;
		this.proposals = new ArrayList<ICompletionProposal>();
	}

	@Override
	protected void doAddCompletionEntry(String name, String kind, String kindModifiers, String sortText) {
		String prefix = getPrefix();
		int cursorPosition = name.length();
		int replacementOffset = position - prefix.length();
		int replacementlength = prefix.length();
		CompletionProposal proposal = new CompletionProposal(name, replacementOffset, replacementlength, cursorPosition,
				null, null, null, null);
		proposals.add(proposal);
	}

	public List<ICompletionProposal> getProposals() {
		return proposals;
	}
}
