package ts.eclipse.jface.text.contentassist;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.contentassist.ICompletionProposal;

import ts.client.ITypeScriptServiceClient;
import ts.client.completions.AbstractCompletionCollector;

public class CompletionProposalCollector extends AbstractCompletionCollector {

	private final List<ICompletionProposal> proposals;
	private final int position;

	public CompletionProposalCollector(int position, String prefix) {
		super(prefix);
		this.position = position;
		this.proposals = new ArrayList<ICompletionProposal>();
	}

	@Override
	protected void doAddCompletionEntry(String name, String kind, String kindModifiers, String sortText,
			String fileName, int line, int offset, ITypeScriptServiceClient client) {
		String prefix = getPrefix();
		ICompletionProposal proposal = new TypeScriptCompletionProposal(name, kind, kindModifiers, sortText, position,
				prefix, fileName, line, offset, client);
		proposals.add(proposal);
	}

	public List<ICompletionProposal> getProposals() {
		return proposals;
	}
}
