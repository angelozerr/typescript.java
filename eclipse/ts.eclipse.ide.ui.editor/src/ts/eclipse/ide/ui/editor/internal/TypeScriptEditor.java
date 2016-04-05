package ts.eclipse.ide.ui.editor.internal;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewerExtension;
import org.eclipse.jface.text.ITextViewerExtension5;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.source.ICharacterPairMatcher;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.ISourceViewerExtension2;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditor;
import org.eclipse.ui.texteditor.ChainedPreferenceStore;
import org.eclipse.ui.texteditor.SourceViewerDecorationSupport;
import org.eclipse.wst.jsdt.core.JavaScriptCore;
import org.eclipse.wst.jsdt.internal.ui.JavaScriptPlugin;
import org.eclipse.wst.jsdt.internal.ui.text.JavaPairMatcher;
import org.eclipse.wst.jsdt.internal.ui.text.PreferencesAdapter;
import org.eclipse.wst.jsdt.ui.PreferenceConstants;
import org.eclipse.wst.jsdt.ui.actions.IJavaEditorActionDefinitionIds;
import org.eclipse.wst.jsdt.ui.text.IJavaScriptPartitions;
import org.eclipse.wst.jsdt.ui.text.JavaScriptSourceViewerConfiguration;
import org.eclipse.wst.jsdt.ui.text.JavaScriptTextTools;

import ts.eclipse.ide.ui.editor.TypeScriptUIEditorPlugin;
import ts.eclipse.ide.ui.editor.internal.text.TypeScriptSourceViewerConfiguration;

public class TypeScriptEditor extends AbstractDecoratedTextEditor {

	/** Preference key for matching brackets */
	protected final static String MATCHING_BRACKETS = PreferenceConstants.EDITOR_MATCHING_BRACKETS;
	/** Preference key for matching brackets color */
	protected final static String MATCHING_BRACKETS_COLOR = PreferenceConstants.EDITOR_MATCHING_BRACKETS_COLOR;
	protected final static char[] BRACKETS = { '{', '}', '(', ')', '[', ']', '<', '>' };
	/** The editor's bracket matcher */
	protected JavaPairMatcher fBracketMatcher = new JavaPairMatcher(BRACKETS);

	/** Preference key for automatically closing strings */
	private final static String CLOSE_STRINGS= PreferenceConstants.EDITOR_CLOSE_STRINGS;
	/** Preference key for automatically closing brackets and parenthesis */
	private final static String CLOSE_BRACKETS= PreferenceConstants.EDITOR_CLOSE_BRACKETS;
	/** The bracket inserter. */
	private BracketInserter fBracketInserter = new BracketInserter(this);

	public TypeScriptEditor() {
		super.setDocumentProvider(TypeScriptUIEditorPlugin.getDefault().getTypeScriptDocumentProvider());
	}

	@Override
	public void createPartControl(Composite parent) {

		super.createPartControl(parent);

		IPreferenceStore preferenceStore = getPreferenceStore();
		boolean closeBrackets = preferenceStore.getBoolean(CLOSE_BRACKETS);
		boolean closeStrings = preferenceStore.getBoolean(CLOSE_STRINGS);
		boolean closeAngularBrackets = JavaScriptCore.VERSION_1_5
				.compareTo(preferenceStore.getString(JavaScriptCore.COMPILER_SOURCE)) <= 0;

		fBracketInserter.setCloseBracketsEnabled(closeBrackets);
		fBracketInserter.setCloseStringsEnabled(closeStrings);
		fBracketInserter.setCloseAngularBracketsEnabled(closeAngularBrackets);

		ISourceViewer sourceViewer = getSourceViewer();
		if (sourceViewer instanceof ITextViewerExtension)
			((ITextViewerExtension) sourceViewer).prependVerifyKeyListener(fBracketInserter);
	}

	@Override
	protected void initializeEditor() {
		IPreferenceStore store = createCombinedPreferenceStore(null);
		setPreferenceStore(store);
		setSourceViewerConfiguration(createJavaSourceViewerConfiguration());

		super.initializeEditor();
	}

	@Override
	protected void doSetInput(IEditorInput input) throws CoreException {
		ISourceViewer sourceViewer = getSourceViewer();
		if (!(sourceViewer instanceof ISourceViewerExtension2)) {
			setPreferenceStore(createCombinedPreferenceStore(input));
			internalDoSetInput(input);
			return;
		}

		// uninstall & unregister preference store listener
		getSourceViewerDecorationSupport(sourceViewer).uninstall();
		((ISourceViewerExtension2) sourceViewer).unconfigure();

		setPreferenceStore(createCombinedPreferenceStore(input));

		// install & register preference store listener
		sourceViewer.configure(getSourceViewerConfiguration());
		getSourceViewerDecorationSupport(sourceViewer).install(getPreferenceStore());

		internalDoSetInput(input);
	}

	private void internalDoSetInput(IEditorInput input) throws CoreException {
		ISourceViewer sourceViewer = getSourceViewer();
		// TSourceViewer javaSourceViewer = null;
		// if (sourceViewer instanceof JavaSourceViewer) {
		// javaSourceViewer = (JavaSourceViewer) sourceViewer;
		// }

		super.doSetInput(input);
	}

	/**
	 * Returns a new Java source viewer configuration.
	 * 
	 * @return a new <code>JavaScriptSourceViewerConfiguration</code>
	 * 
	 */
	protected JavaScriptSourceViewerConfiguration createJavaSourceViewerConfiguration() {
		JavaScriptTextTools textTools = JavaScriptPlugin.getDefault().getJavaTextTools();
		return new TypeScriptSourceViewerConfiguration(textTools.getColorManager(), getPreferenceStore(), this,
				IJavaScriptPartitions.JAVA_PARTITIONING);
	}

	/**
	 * Creates and returns the preference store for this Java editor with the
	 * given input.
	 *
	 * @param input
	 *            The editor input for which to create the preference store
	 * @return the preference store for this editor
	 *
	 * 
	 */
	private IPreferenceStore createCombinedPreferenceStore(IEditorInput input) {
		List stores = new ArrayList();

		// IJavaScriptProject project= EditorUtility.getJavaProject(input);
		// if (project != null) {
		// stores.add(new EclipsePreferencesAdapter(new
		// ProjectScope(project.getProject()), JavaScriptCore.PLUGIN_ID));
		// }

		stores.add(JavaScriptPlugin.getDefault().getPreferenceStore());
		stores.add(new PreferencesAdapter(JavaScriptCore.getPlugin().getPluginPreferences()));
		stores.add(EditorsUI.getPreferenceStore());

		return new ChainedPreferenceStore((IPreferenceStore[]) stores.toArray(new IPreferenceStore[stores.size()]));
	}

	@Override
	protected void createActions() {
		super.createActions();

		Action action = new GotoMatchingBracketAction(this);
		action.setActionDefinitionId(IJavaEditorActionDefinitionIds.GOTO_MATCHING_BRACKET);
		setAction(GotoMatchingBracketAction.GOTO_MATCHING_BRACKET, action);
	}

	@Override
	protected void configureSourceViewerDecorationSupport(SourceViewerDecorationSupport support) {
		fBracketMatcher.setSourceVersion(getPreferenceStore().getString(JavaScriptCore.COMPILER_SOURCE));
		support.setCharacterPairMatcher(fBracketMatcher);
		support.setMatchingCharacterPainterPreferenceKeys(MATCHING_BRACKETS, MATCHING_BRACKETS_COLOR);
		super.configureSourceViewerDecorationSupport(support);
	}

	/**
	 * Jumps to the matching bracket.
	 */
	public void gotoMatchingBracket() {

		ISourceViewer sourceViewer = getSourceViewer();
		IDocument document = sourceViewer.getDocument();
		if (document == null)
			return;

		IRegion selection = getSignedSelection(sourceViewer);

		int selectionLength = Math.abs(selection.getLength());
		if (selectionLength > 1) {
			setStatusLineErrorMessage(TypeScriptUIMessages.GotoMatchingBracket_error_invalidSelection);
			sourceViewer.getTextWidget().getDisplay().beep();
			return;
		}

		// #26314
		int sourceCaretOffset = selection.getOffset() + selection.getLength();
		if (isSurroundedByBrackets(document, sourceCaretOffset))
			sourceCaretOffset -= selection.getLength();

		IRegion region = fBracketMatcher.match(document, sourceCaretOffset);
		if (region == null) {
			setStatusLineErrorMessage(TypeScriptUIMessages.GotoMatchingBracket_error_noMatchingBracket);
			sourceViewer.getTextWidget().getDisplay().beep();
			return;
		}

		int offset = region.getOffset();
		int length = region.getLength();

		if (length < 1)
			return;

		int anchor = fBracketMatcher.getAnchor();
		// http://dev.eclipse.org/bugs/show_bug.cgi?id=34195
		int targetOffset = (ICharacterPairMatcher.RIGHT == anchor) ? offset + 1 : offset + length;

		boolean visible = false;
		if (sourceViewer instanceof ITextViewerExtension5) {
			ITextViewerExtension5 extension = (ITextViewerExtension5) sourceViewer;
			visible = (extension.modelOffset2WidgetOffset(targetOffset) > -1);
		} else {
			IRegion visibleRegion = sourceViewer.getVisibleRegion();
			// http://dev.eclipse.org/bugs/show_bug.cgi?id=34195
			visible = (targetOffset >= visibleRegion.getOffset()
					&& targetOffset <= visibleRegion.getOffset() + visibleRegion.getLength());
		}

		if (!visible) {
			setStatusLineErrorMessage(TypeScriptUIMessages.GotoMatchingBracket_error_bracketOutsideSelectedElement);
			sourceViewer.getTextWidget().getDisplay().beep();
			return;
		}

		if (selection.getLength() < 0)
			targetOffset -= selection.getLength();

		sourceViewer.setSelectedRange(targetOffset, selection.getLength());
		sourceViewer.revealRange(targetOffset, selection.getLength());
	}

	/**
	 * Returns the signed current selection. The length will be negative if the
	 * resulting selection is right-to-left (RtoL).
	 * <p>
	 * The selection offset is model based.
	 * </p>
	 *
	 * @param sourceViewer
	 *            the source viewer
	 * @return a region denoting the current signed selection, for a resulting
	 *         RtoL selections length is < 0
	 */
	protected IRegion getSignedSelection(ISourceViewer sourceViewer) {
		StyledText text = sourceViewer.getTextWidget();
		Point selection = text.getSelectionRange();

		if (text.getCaretOffset() == selection.x) {
			selection.x = selection.x + selection.y;
			selection.y = -selection.y;
		}

		selection.x = widgetOffset2ModelOffset(sourceViewer, selection.x);

		return new Region(selection.x, selection.y);
	}

	private static boolean isBracket(char character) {
		for (int i = 0; i != BRACKETS.length; ++i)
			if (character == BRACKETS[i])
				return true;
		return false;
	}

	private static boolean isSurroundedByBrackets(IDocument document, int offset) {
		if (offset == 0 || offset == document.getLength())
			return false;

		try {
			return isBracket(document.getChar(offset - 1)) && isBracket(document.getChar(offset));

		} catch (BadLocationException e) {
			return false;
		}
	}

	@Override
	public void dispose() {
		if (fBracketMatcher != null) {
			fBracketMatcher.dispose();
			fBracketMatcher = null;
		}

		ISourceViewer sourceViewer = getSourceViewer();
		if (sourceViewer instanceof ITextViewerExtension) {
			((ITextViewerExtension) sourceViewer).removeVerifyKeyListener(fBracketInserter);
		}

		super.dispose();
	}

	ISourceViewer getViewser() {
		return getSourceViewer();
	}

}
