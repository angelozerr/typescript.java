package ts.eclipse.jface.text.contentassist;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;

import ts.CompletionEntry;
import ts.eclipse.jface.images.TypeScriptImagesRegistry;

public class TypeScriptCompletionProposal extends CompletionEntry implements ICompletionProposal {

	private int cursorPosition;
	private int replacementOffset;
	private int replacementlength;

	public TypeScriptCompletionProposal(String name, String kind, String kindModifiers, String sortText, int position, String prefix) {
		super(name, kind, kindModifiers, sortText);
		this.cursorPosition = name.length();
		this.replacementOffset = position - prefix.length();
		this.replacementlength = prefix.length();
	}

	@Override
	public void apply(IDocument document) {
		CompletionProposal proposal = new CompletionProposal(getReplacementString(), getReplacementOffset(),
				getReplacementLength(), getCursorPosition(), getImage(), getDisplayString(), getContextInformation(),
				getAdditionalProposalInfo());
		proposal.apply(document);
	}

	protected String getReplacementString() {
		return getName();
	}
	
	protected int getReplacementLength() {
		return replacementlength;
	}

	protected int getReplacementOffset() {
		return replacementOffset;
	}

	protected int getCursorPosition() {
		return cursorPosition;
	}
	
	@Override
	public String getAdditionalProposalInfo() {
		return null;
	}

	@Override
	public IContextInformation getContextInformation() {
		return null;
	}

	@Override
	public String getDisplayString() {
		return getName();
	}

	@Override
	public Image getImage() {
		return TypeScriptImagesRegistry.getImage(this);
	}

	@Override
	public Point getSelection(IDocument document) {
		return null;
	}

}
