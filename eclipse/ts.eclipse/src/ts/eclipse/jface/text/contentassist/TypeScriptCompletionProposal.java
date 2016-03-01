package ts.eclipse.jface.text.contentassist;

import java.util.List;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ContextInformation;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension2;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension3;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;

import ts.TypeScriptException;
import ts.client.ITypeScriptServiceClient;
import ts.client.completions.CompletionEntry;
import ts.client.completions.ICompletionEntryDetails;
import ts.client.completions.SymbolDisplayPart;
import ts.eclipse.jface.images.TypeScriptImagesRegistry;
import ts.utils.StringUtils;
import ts.utils.TypeScriptHelper;

public class TypeScriptCompletionProposal extends CompletionEntry implements ICompletionProposal,
		ICompletionProposalExtension, ICompletionProposalExtension2, ICompletionProposalExtension3 {

	private int cursorPosition;
	private int replacementOffset;
	private int replacementlength;
	private IContextInformation contextInformation;
	private boolean fToggleEating;

	public TypeScriptCompletionProposal(String name, String kind, String kindModifiers, String sortText, int position,
			String prefix, String fileName, int line, int offset, ITypeScriptServiceClient client) {
		super(name, kind, kindModifiers, sortText, fileName, line, offset, client);
		this.cursorPosition = name.length();
		this.replacementOffset = position - prefix.length();
		setReplacementLength(prefix.length());
	}

	public void setReplacementLength(int replacementlength) {
		this.replacementlength = replacementlength;
	}

	@Override
	public void apply(IDocument document) {
		CompletionProposal proposal = new CompletionProposal(getReplacementString(), getReplacementOffset(),
				getReplacementLength(), getCursorPosition(), getImage(), getDisplayString(), getContextInformation(),
				getAdditionalProposalInfo());
		proposal.apply(document);
	}

	@Override
	public void apply(IDocument document, char trigger, int offset) {
		CompletionProposal proposal = new CompletionProposal(getReplacementString(), getReplacementOffset(),
				getReplacementLength(), getCursorPosition(), getImage(), getDisplayString(), getContextInformation(),
				getAdditionalProposalInfo());
		// we currently don't do anything special for which character
		// selected the proposal, and where the cursor offset is
		// but we might in the future...
		proposal.apply(document);
		// we want to ContextInformationPresenter.updatePresentation() here
	}

	public void apply(ITextViewer viewer, char trigger, int stateMask, int offset) {
		IDocument document = viewer.getDocument();
		// don't eat if not in preferences, XOR with modifier key 1 (Ctrl)
		// but: if there is a selection, replace it!
		Point selection = viewer.getSelectedRange();
		fToggleEating = (stateMask & SWT.MOD1) != 0;
		int newLength = selection.x + selection.y - getReplacementOffset();
		if ((insertCompletion() ^ fToggleEating) && newLength >= 0) {
			setReplacementLength(newLength);
		}
		apply(document, trigger, offset);
		fToggleEating = false;
	}

	private boolean insertCompletion() {
		return true;
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
		try {
			ICompletionEntryDetails details = getEntryDetails();
			if (details == null) {
				return null;
			}
			String html = TypeScriptHelper.html(details.getDocumentation());
			if (StringUtils.isEmpty(html)) {
				html = TypeScriptHelper.html(details.getDisplayParts());
			}
			return html;
		} catch (TypeScriptException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public IContextInformation getContextInformation() {
		if (contextInformation != null) {
			return contextInformation;
		}
		try {
			ICompletionEntryDetails details = getEntryDetails();
			List<SymbolDisplayPart> parts = details.getDisplayParts();
			if (parts.size() > 0) {
				contextInformation = new ContextInformation("", TypeScriptHelper.html(parts));
			}
			return contextInformation;
		} catch (TypeScriptException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public int getContextInformationPosition() {
		// https://bugs.eclipse.org/bugs/show_bug.cgi?id=110355
		// return getCursorPosition();
		if (getContextInformation() == null)
			return getReplacementOffset() - 1;
		return getReplacementOffset() + getCursorPosition();
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
		CompletionProposal proposal = new CompletionProposal(getReplacementString(), getReplacementOffset(),
				getReplacementLength(), getCursorPosition(), getImage(), getDisplayString(), getContextInformation(),
				getAdditionalProposalInfo());
		return proposal.getSelection(document);
	}

	@Override
	public char[] getTriggerCharacters() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IInformationControlCreator getInformationControlCreator() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getPrefixCompletionStart(IDocument document, int completionOffset) {
		return replacementOffset;
	}

	@Override
	public CharSequence getPrefixCompletionText(IDocument document, int completionOffset) {
		return null;
	}

	@Override
	public void selected(ITextViewer viewer, boolean smartToggle) {

	}

	@Override
	public void unselected(ITextViewer viewer) {

	}

	@Override
	public boolean validate(IDocument document, int offset, DocumentEvent event) {
		if (offset < replacementOffset)
			return false;
		boolean validated = startsWith(document, offset, getReplacementString());
		// if (fUpdateLengthOnValidate && event != null) {
		// replacementLength += event.fText.length() - event.fLength; // adjust
		// the
		// replacement
		// length
		// by
		// the
		// event's
		// text
		// replacement
		// }
		return validated;
	}

	// code is borrowed from JavaCompletionProposal
	protected boolean startsWith(IDocument document, int offset, String word) {

		int replacementOffset = getReplacementOffset();
		int wordLength = word == null ? 0 : word.length();
		if (offset > replacementOffset + wordLength)
			return false;

		try {
			int length = offset - replacementOffset;
			String start = document.get(replacementOffset, length);

			return (word != null && word.substring(0, length).equalsIgnoreCase(start));
			/*
			 * || (fAlternateMatch != null && length <= fAlternateMatch.length()
			 * && fAlternateMatch.substring(0, length).equalsIgnoreCase(start)
			 */
			// );
		} catch (BadLocationException x) {
		}

		return false;
	}

	@Override
	public boolean isValidFor(IDocument document, int offset) {
		return validate(document, offset, null);
	}
}
