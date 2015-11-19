package ts.eclipse.jface.fieldassist;

import java.util.ArrayList;

import org.eclipse.jface.fieldassist.ContentProposal;
import org.eclipse.jface.fieldassist.IContentProposal;

import ts.ICompletionCollector;

public class ContentProposalCollector extends ArrayList<IContentProposal>implements ICompletionCollector {

	public static final IContentProposal[] EMPTY_PROPOSAL = new IContentProposal[0];
	
	@Override
	public void addCompletionEntry(String name, String kind, String kindModifiers, String sortText) {
		int cursorPosition = name.length();
		ContentProposal proposal = new ContentProposal(name, name, "", cursorPosition);
		super.add(proposal);
	}
}
