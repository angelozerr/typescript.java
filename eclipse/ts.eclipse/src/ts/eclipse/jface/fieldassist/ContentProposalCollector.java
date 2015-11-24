package ts.eclipse.jface.fieldassist;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.fieldassist.ContentProposal;
import org.eclipse.jface.fieldassist.IContentProposal;

import ts.server.completions.AbstractCompletionCollector;

public class ContentProposalCollector extends AbstractCompletionCollector {

	public static final IContentProposal[] EMPTY_PROPOSAL = new IContentProposal[0];
	private final List<IContentProposal> proposals;

	public ContentProposalCollector(String prefix) {
		super(prefix);
		this.proposals = new ArrayList<IContentProposal>();
	}

	@Override
	protected void doAddCompletionEntry(String name, String kind, String kindModifiers, String sortText) {
		String content = name.substring(getPrefix().length(), name.length());
		int cursorPosition = content.length();
		ContentProposal proposal = new ContentProposal(content, name, "", cursorPosition);
		proposals.add(proposal);
	}

	public List<IContentProposal> getProposals() {
		return proposals;
	}
}
