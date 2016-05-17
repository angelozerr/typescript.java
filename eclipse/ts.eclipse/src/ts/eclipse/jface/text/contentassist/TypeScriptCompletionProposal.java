/**
 *  Copyright (c) 2015-2016 Angelo ZERR.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */

package ts.eclipse.jface.text.contentassist;

import java.util.List;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.BadPositionCategoryException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.IPositionUpdater;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.contentassist.BoldStylerProvider;
//import org.eclipse.jface.text.contentassist.BoldStylerProvider;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ContextInformation;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension2;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension3;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension6;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension7;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.link.ILinkedModeListener;
import org.eclipse.jface.text.link.InclusivePositionUpdater;
import org.eclipse.jface.text.link.LinkedModeModel;
import org.eclipse.jface.text.link.LinkedModeUI;
import org.eclipse.jface.text.link.LinkedModeUI.ExitFlags;
import org.eclipse.jface.text.link.LinkedModeUI.IExitPolicy;
import org.eclipse.jface.text.link.LinkedPosition;
import org.eclipse.jface.text.link.LinkedPositionGroup;
import org.eclipse.jface.text.link.ProposalPosition;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.texteditor.link.EditorLinkedModeUI;

import ts.TypeScriptException;
import ts.TypeScriptKind;
import ts.client.ITypeScriptServiceClient;
import ts.client.completions.CompletionEntry;
import ts.client.completions.ICompletionEntryDetails;
import ts.client.completions.ICompletionEntryMatcher;
import ts.client.completions.SymbolDisplayPart;
import ts.eclipse.jface.images.TypeScriptImagesRegistry;
import ts.utils.StringUtils;
import ts.utils.TypeScriptHelper;

/**
 * {@link ICompletionProposal} implementation with TypeScript completion entry.
 */
public class TypeScriptCompletionProposal extends CompletionEntry
		implements ICompletionProposal, ICompletionProposalExtension, ICompletionProposalExtension2,
		ICompletionProposalExtension3, ICompletionProposalExtension6, ICompletionProposalExtension7 {

	public static final String TAB = "\t";
	public static final String SPACE = " ";

	private static final String RPAREN = ")";
	private static final String LPAREN = "(";
	private static final String COMMA = ",";

	private int cursorPosition;
	private int replacementOffset;
	private int replacementlength;
	private boolean contextInformationComputed;
	private IContextInformation contextInformation;
	private boolean fToggleEating;
	private StyledString fDisplayString;

	private IRegion fSelectedRegion; // initialized by apply()
	private IPositionUpdater fUpdater;

	private String fReplacementString;

	private Arguments arguments;
	private ITextViewer fTextViewer;

	public TypeScriptCompletionProposal(String name, String kind, String kindModifiers, String sortText, int position,
			String prefix, String fileName, int line, int offset, ICompletionEntryMatcher matcher,
			ITypeScriptServiceClient client) {
		super(name, kind, kindModifiers, sortText, fileName, line, offset, matcher, client);
		fReplacementString = name;
		this.cursorPosition = name.length();
		this.replacementOffset = position - prefix.length();
		setReplacementLength(prefix.length());
		this.contextInformationComputed = false;
		this.fDisplayString = new StyledString(getName());
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
		// compute replacement string
		String replacement = computeReplacementString(document, offset);
		setReplacementString(replacement);

		updateReplacementLengthForString(document, offset, replacement);

		// apply the replacement.
		CompletionProposal proposal = new CompletionProposal(getReplacementString(), getReplacementOffset(),
				getReplacementLength(), getCursorPosition(), getImage(), getDisplayString(), getContextInformation(),
				getAdditionalProposalInfo());
		// we currently don't do anything special for which character
		// selected the proposal, and where the cursor offset is
		// but we might in the future...
		proposal.apply(document);
		int baseOffset = getReplacementOffset();

		if (arguments != null && !arguments.isEmpty() && getTextViewer() != null) {
			try {// adjust offset of the whole arguments
				arguments.setBaseOffset(baseOffset);
				// guess parameters if "guess-types" tern plugin is checked.
				// guessParameters(offset);
				// Create group.
				Arg arg = null;
				LinkedModeModel model = new LinkedModeModel();
				for (int i = 0; i != arguments.size(); i++) {
					arg = arguments.get(i);
					LinkedPositionGroup group = new LinkedPositionGroup();
					if (arg.getProposals() == null) {
						group.addPosition(new LinkedPosition(document, arg.getOffset(), arg.getLength(),
								LinkedPositionGroup.NO_STOP));
					} else {
						ensurePositionCategoryInstalled(document, model);
						document.addPosition(getCategory(), arg);
						group.addPosition(new ProposalPosition(document, arg.getOffset(), arg.getLength(),
								LinkedPositionGroup.NO_STOP, arg.getProposals()));
					}
					model.addGroup(group);
				}

				model.forceInstall();
				/*
				 * JavaEditor editor = getJavaEditor(); if (editor != null) {
				 * model.addLinkingListener(new EditorHighlightingSynchronizer(
				 * editor)); }
				 */

				LinkedModeUI ui = new EditorLinkedModeUI(model, getTextViewer());
				ui.setExitPosition(getTextViewer(), baseOffset + replacement.length(), 0, Integer.MAX_VALUE);
				ui.setExitPolicy(new ExitPolicy(')', document));
				ui.setDoContextInfo(true);
				ui.setCyclingMode(LinkedModeUI.CYCLE_WHEN_NO_PARENT);
				ui.enter();

				fSelectedRegion = ui.getSelectedRegion();

			} catch (BadLocationException e) {
				ensurePositionCategoryRemoved(document);
				// JavaScriptPlugin.log(e);
				// openErrorDialog(e);
			} catch (BadPositionCategoryException e) {
				ensurePositionCategoryRemoved(document);
				// JavaScriptPlugin.log(e);
				// openErrorDialog(e);
			}

		} else {
			int newOffset = baseOffset + replacement.length();
			fSelectedRegion = new Region(newOffset, 0);
		}
	}

	private String computeReplacementString(IDocument document, int offset) {

		try {
			ICompletionEntryDetails details = super.getEntryDetails();
			TypeScriptKind tsKind = details != null ? TypeScriptKind.getKind(details.getKind()) : null;
			if (tsKind != null && (TypeScriptKind.CONSTRUCTOR == tsKind || TypeScriptKind.FUNCTION == tsKind
					|| TypeScriptKind.METHOD == tsKind)) {
				// It's a function
				// compute replacement string
				// setReplacementString(replacement);

				String indentation = getIndentation(document, offset);
				arguments = new Arguments();

				StringBuilder replacement = new StringBuilder(super.getName());
				replacement.append(LPAREN);
				setCursorPosition(replacement.length());
				computeReplacementString(details.getDisplayParts(), replacement, arguments, indentation, 1, true);
				replacement.append(RPAREN);
				return replacement.toString();
			}

		} catch (TypeScriptException e) {
		}
		return getReplacementString();
	}

	/**
	 * Compute replacement string for the given function.
	 * 
	 * @param parameters
	 * @param replacement
	 * @param arguments
	 * @param indentation
	 * @param nbIndentations
	 * @param initialFunction
	 */
	private void computeReplacementString(List<SymbolDisplayPart> parameters, StringBuilder replacement,
			Arguments arguments, String indentation, int nbIndentations, boolean initialFunction) {
		int count = parameters.size();
		SymbolDisplayPart parameter = null;
		String paramName = null;
		boolean hasParam = false;
		for (int i = 0; i != count; i++) {
			parameter = parameters.get(i);
			if (!parameter.getKind().equals("parameterName")) {
				continue;
			}
			if (hasParam) {
				// if (prefs.beforeComma)
				// buffer.append(SPACE);
				replacement.append(COMMA);
				// if (prefs.afterComma)
				replacement.append(SPACE);
			}

			int offset = replacement.length();
			paramName = parameter.getText();
			// to select focus for parameter
			replacement.append(paramName);
			arguments.addArg(offset, paramName.length());
			hasParam = true;
		}
	}

	/**
	 * Returns the indentation characters from the given line.
	 * 
	 * @param document
	 * @param offset
	 * @return the indentation characters from the given line.
	 */
	private String getIndentation(IDocument document, int offset) {
		try {
			IRegion lineRegion = document.getLineInformationOfOffset(offset);
			String lineText = document.get(lineRegion.getOffset(), lineRegion.getLength());
			StringBuilder indentation = new StringBuilder();
			char[] chars = lineText.toCharArray();
			char c;
			for (int i = 0; i < chars.length; i++) {
				c = chars[i];
				if (c == ' ' || c == '\t') {
					indentation.append(c);
				} else {
					break;
				}
			}
			return indentation.toString();

		} catch (BadLocationException e1) {
		}
		return "";
	}

	/**
	 * Compute new replacement length for string replacement.
	 * 
	 * @param document
	 * @param offset
	 * @param replacement
	 */
	protected void updateReplacementLengthForString(IDocument document, int offset, String replacement) {
		boolean isString = replacement.startsWith("\"") || replacement.startsWith("'");
		if (isString) {
			int length = document.getLength();
			int pos = offset;
			char c;
			while (pos < length) {
				try {
					c = document.getChar(pos);
					switch (c) {
					case '\r':
					case '\n':
					case '\t':
					case ' ':
						return;
					case '"':
					case '\'':
						setReplacementLength(getReplacementLength() + pos - offset + 1);
						return;
					}
					++pos;
				} catch (BadLocationException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public void apply(ITextViewer viewer, char trigger, int stateMask, int offset) {
		IDocument document = viewer.getDocument();
		if (fTextViewer == null) {
			fTextViewer = viewer;
		}
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
		return fReplacementString;
	}

	/**
	 * @param replacementString
	 *            The fReplacementString to set.
	 */
	public void setReplacementString(String replacementString) {
		fReplacementString = replacementString;
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

	public void setCursorPosition(int cursorPosition) {
		this.cursorPosition = cursorPosition;
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
		if (contextInformationComputed) {
			return contextInformation;
		}
		this.contextInformation = createContextInformation();
		contextInformationComputed = true;
		return contextInformation;
	}

	private IContextInformation createContextInformation() {
		try {
			ICompletionEntryDetails details = getEntryDetails();
			if (details != null) {
				List<SymbolDisplayPart> parts = details.getDisplayParts();
				if (parts != null && parts.size() > 0) {
					return new ContextInformation("", TypeScriptHelper.html(parts));
				}
			}
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
		if (fDisplayString != null) {
			return fDisplayString.getString();
		}
		return ""; //$NON-NLS-1$
	}

	@Override
	public Image getImage() {
		return TypeScriptImagesRegistry.getImage(this);
	}

	@Override
	public Point getSelection(IDocument document) {
		// CompletionProposal proposal = new
		// CompletionProposal(getReplacementString(), getReplacementOffset(),
		// getReplacementLength(), getCursorPosition(), getImage(),
		// getDisplayString(), getContextInformation(),
		// getAdditionalProposalInfo());
		// return proposal.getSelection(document);
		if (fSelectedRegion == null) {
			return new Point(getReplacementOffset(), 0);
		}
		return new Point(fSelectedRegion.getOffset(), fSelectedRegion.getLength());
	}

	public ITextViewer getTextViewer() {
		return fTextViewer;
	}

	@Override
	public char[] getTriggerCharacters() {
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
		if (offset < replacementOffset) {
			return false;
		}

		int replacementOffset = getReplacementOffset();
		String word = getReplacementString();
		int wordLength = word == null ? 0 : word.length();
		if (offset > replacementOffset + wordLength) {
			return false;
		}

		try {
			int length = offset - replacementOffset;
			String start = document.get(replacementOffset, length);
			return super.updatePrefix(start);
			// if (word == null) {
			// return false;
			// }
			// int[] bestSequence = getMatcher().bestSubsequence(word, start);
			// if (bestSequence != null && bestSequence.length > 0) {
			// super.updatePrefix(start);
			// return true;
			// }
		} catch (BadLocationException x) {
		}

		return false;

		/*
		 * 
		 * 
		 * boolean validated = isMatchWord(document, offset,
		 * getReplacementString()); if (validated) { StyledString
		 * styledDisplayString = new StyledString();
		 * styledDisplayString.append(getName()); String pattern = if (pattern
		 * != null && pattern.length() > 0) { String displayString =
		 * styledDisplayString.getString(); int[] bestSequence =
		 * getMatcher().bestSubsequence(displayString, pattern); int
		 * highlightAdjustment = 0; for (int index : bestSequence) {
		 * styledDisplayString.setStyle(index + highlightAdjustment, 1, null); }
		 * } }
		 * 
		 * // if (fUpdateLengthOnValidate && event != null) { //
		 * replacementLength += event.fText.length() - event.fLength; // adjust
		 * // the // replacement // length // by // the // event's // text //
		 * replacement // } return validated;
		 */
	}

	protected boolean isMatchWord(IDocument document, int offset, String word) {
		int replacementOffset = getReplacementOffset();
		int wordLength = word == null ? 0 : word.length();
		if (offset > replacementOffset + wordLength)
			return false;

		try {
			int length = offset - replacementOffset;
			String start = document.get(replacementOffset, length);
			if (word == null) {
				return false;
			}
			int[] bestSequence = getMatcher().bestSubsequence(word, start);
			return bestSequence != null && bestSequence.length > 0;
		} catch (BadLocationException x) {
		}

		return false;
	}

	@Override
	public boolean isValidFor(IDocument document, int offset) {
		return false; // validate(document, offset, null);
	}

	@Override
	public StyledString getStyledDisplayString() {
		return fDisplayString;
	}

	@Override
	public StyledString getStyledDisplayString(IDocument document, int offset, BoldStylerProvider boldStylerProvider) {
		StyledString styledDisplayString = new StyledString();
		styledDisplayString.append(getStyledDisplayString());

		String pattern = getPatternToEmphasizeMatch(document, offset);
		if (pattern != null && pattern.length() > 0) {
			String displayString = styledDisplayString.getString();
			int[] bestSequence = getMatcher().bestSubsequence(displayString, pattern);
			int highlightAdjustment = 0;
			for (int index : bestSequence) {
				styledDisplayString.setStyle(index + highlightAdjustment, 1, boldStylerProvider.getBoldStyler());
			}
		}
		return styledDisplayString;
	}

	/**
	 * Computes the token at the given <code>offset</code> in
	 * <code>document</code> to emphasize the ranges matching this token in
	 * proposal's display string.
	 * 
	 * @param document
	 *            the document where content assist is invoked
	 * @param offset
	 *            the offset in the document at current caret location
	 * @return the token at the given <code>offset</code> in
	 *         <code>document</code> to be used for emphasizing matching ranges
	 *         in proposal's display string
	 * @since 3.12
	 */
	protected String getPatternToEmphasizeMatch(IDocument document, int offset) {
		int start = getPrefixCompletionStart(document, offset);
		int patternLength = offset - start;
		String pattern = null;
		try {
			pattern = document.get(start, patternLength);
		} catch (BadLocationException e) {
			// return null
		}
		return pattern;
	}

	private void ensurePositionCategoryInstalled(final IDocument document, LinkedModeModel model) {
		if (!document.containsPositionCategory(getCategory())) {
			document.addPositionCategory(getCategory());
			fUpdater = new InclusivePositionUpdater(getCategory());
			document.addPositionUpdater(fUpdater);

			model.addLinkingListener(new ILinkedModeListener() {

				/*
				 * @see
				 * org.eclipse.jface.text.link.ILinkedModeListener#left(org.
				 * eclipse.jface.text.link.LinkedModeModel, int)
				 */
				public void left(LinkedModeModel environment, int flags) {
					ensurePositionCategoryRemoved(document);
				}

				public void suspend(LinkedModeModel environment) {
				}

				public void resume(LinkedModeModel environment, int flags) {
				}
			});
		}
	}

	private void ensurePositionCategoryRemoved(IDocument document) {
		if (document.containsPositionCategory(getCategory())) {
			try {
				document.removePositionCategory(getCategory());
			} catch (BadPositionCategoryException e) {
				// ignore
			}
			document.removePositionUpdater(fUpdater);
		}
	}

	protected static final class ExitPolicy implements IExitPolicy {

		final char fExitCharacter;
		private final IDocument fDocument;

		public ExitPolicy(char exitCharacter, IDocument document) {
			fExitCharacter = exitCharacter;
			fDocument = document;
		}

		public ExitFlags doExit(LinkedModeModel environment, VerifyEvent event, int offset, int length) {

			if (event.character == fExitCharacter) {
				if (environment.anyPositionContains(offset))
					return new ExitFlags(ILinkedModeListener.UPDATE_CARET, false);
				else
					return new ExitFlags(ILinkedModeListener.UPDATE_CARET, true);
			}

			switch (event.character) {
			case ';':
				return new ExitFlags(ILinkedModeListener.NONE, true);
			case SWT.CR:
				// when entering an anonymous class as a parameter, we don't
				// want
				// to jump after the parenthesis when return is pressed
				if (offset > 0) {
					try {
						if (fDocument.getChar(offset - 1) == '{')
							return new ExitFlags(ILinkedModeListener.EXIT_ALL, true);
					} catch (BadLocationException e) {
					}
				}
				return null;
			default:
				return null;
			}
		}

	}

	private String getCategory() {
		return "TypeScriptCompletionProposal_" + toString(); //$NON-NLS-1$
	}
}
