package ts.eclipse.jface.text.contentassist;

import java.util.ArrayList;

import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposal;

import ts.ICompletionCollector;

public class CompletionProposalCollector extends ArrayList<ICompletionProposal>implements ICompletionCollector {

	public static final IContentProposal[] EMPTY_PROPOSAL = new IContentProposal[0];

	private final int position;
	
	public CompletionProposalCollector(int position) {
		this.position = position;
	}
	@Override
	public void addCompletionEntry(String name, String kind, String kindModifiers, String sortText) {
		int cursorPosition = name.length();
		CompletionProposal proposal = new CompletionProposal(name, position, 0, cursorPosition, null, null, null, null);
		super.add(proposal);
	}

}
