package ts.eclipse.jface.fieldassist;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.jface.fieldassist.IContentProposalProvider;

import ts.ICompletionEntry;
import ts.ICompletionInfo;
import ts.TSException;
import ts.doc.IJSDocument;
import ts.server.ITSClient;

public class TSContentProposalProvider implements IContentProposalProvider {

	private final IJSDocument doc;

	public TSContentProposalProvider(IJSDocument doc) {
		this.doc = doc;
	}

	@Override
	public IContentProposal[] getProposals(String contents, int position) {
		ITSClient client = doc.getClient();
		try {
			ICompletionInfo completion = client.getCompletionsAtLineOffset(doc.getName(), 0, position);
			List<IContentProposal> proposals = new ArrayList<IContentProposal>();
			ICompletionEntry[] entries = completion.getEntries();
			for (int i = 0; i < entries.length; i++) {
				final ICompletionEntry entry = entries[i];
				proposals.add(new IContentProposal() {
					
					@Override
					public String getLabel() {
						// TODO Auto-generated method stub
						return entry.getName();
					}
					
					@Override
					public String getDescription() {
						// TODO Auto-generated method stub
						return null;
					}
					
					@Override
					public int getCursorPosition() {
						// TODO Auto-generated method stub
						return 0;
					}
					
					@Override
					public String getContent() {
						// TODO Auto-generated method stub
						return entry.getName();
					}
				});
			}
			return proposals.toArray(new IContentProposal[0]);
		} catch (TSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

}
