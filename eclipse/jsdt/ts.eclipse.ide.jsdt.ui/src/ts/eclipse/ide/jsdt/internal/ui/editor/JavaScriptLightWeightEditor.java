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
package ts.eclipse.ide.jsdt.internal.ui.editor;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DefaultInformationControl;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewerExtension;
import org.eclipse.jface.text.ITextViewerExtension5;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.reconciler.IReconciler;
import org.eclipse.jface.text.source.ICharacterPairMatcher;
import org.eclipse.jface.text.source.IOverviewRuler;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.ISourceViewerExtension2;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.jface.text.source.projection.ProjectionSupport;
import org.eclipse.jface.text.source.projection.ProjectionViewer;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditor;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditorPreferenceConstants;
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

import ts.eclipse.ide.jsdt.internal.ui.JSDTTypeScriptUIPlugin;
import ts.eclipse.ide.jsdt.internal.ui.actions.GotoMatchingBracketAction;

/**
 * TypeScript editor.
 *
 */
public class JavaScriptLightWeightEditor extends AbstractDecoratedTextEditor {

	/** Preference key for matching brackets */
	protected final static String MATCHING_BRACKETS = PreferenceConstants.EDITOR_MATCHING_BRACKETS;
	/** Preference key for matching brackets color */
	protected final static String MATCHING_BRACKETS_COLOR = PreferenceConstants.EDITOR_MATCHING_BRACKETS_COLOR;
	protected final static char[] BRACKETS = { '{', '}', '(', ')', '[', ']', '<', '>' };
	/** The editor's bracket matcher */
	protected JavaPairMatcher fBracketMatcher = new JavaPairMatcher(BRACKETS);

	/** Preference key for automatically closing strings */
	private final static String CLOSE_STRINGS = PreferenceConstants.EDITOR_CLOSE_STRINGS;
	/** Preference key for automatically closing brackets and parenthesis */
	private final static String CLOSE_BRACKETS = PreferenceConstants.EDITOR_CLOSE_BRACKETS;
	/** The bracket inserter. */
	private BracketInserter fBracketInserter = new BracketInserter(this);

	/**
	 * This editor's projection support
	 * 
	 */
	private ProjectionSupport fProjectionSupport;

	public JavaScriptLightWeightEditor() {
		super.setDocumentProvider(JSDTTypeScriptUIPlugin.getDefault().getTypeScriptDocumentProvider());
	}

	@Override
	public void createPartControl(Composite parent) {

		super.createPartControl(parent);

		// do not even install projection support until folding is actually
		// enabled
		if (isFoldingEnabled()) {
			installProjectionSupport();
		}

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
		setSourceViewerConfiguration(createTypeScriptSourceViewerConfiguration());
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
		TypeScriptSourceViewer TypeScriptSourceViewer = null;
		if (sourceViewer instanceof TypeScriptSourceViewer)
			TypeScriptSourceViewer = (TypeScriptSourceViewer) sourceViewer;

		IPreferenceStore store = getPreferenceStore();
		// if (TypeScriptSourceViewer != null && isFoldingEnabled() &&(store ==
		// null || !store.getBoolean(PreferenceConstants.EDITOR_SHOW_SEGMENTS)))
		// TypeScriptSourceViewer.prepareDelayedProjection();

		super.doSetInput(input);

		if (TypeScriptSourceViewer != null && TypeScriptSourceViewer.getReconciler() == null) {
			IReconciler reconciler = getSourceViewerConfiguration().getReconciler(TypeScriptSourceViewer);
			if (reconciler != null) {
				reconciler.install(TypeScriptSourceViewer);
				TypeScriptSourceViewer.setReconciler(reconciler);
			}
		}

		// if (fEncodingSupport != null)
		// fEncodingSupport.reset();

		// setOutlinePageInput(fOutlinePage, input);
		//
		// if (isShowingOverrideIndicators())
		// installOverrideIndicator(false);
	}

	/**
	 * Returns a new Java source viewer configuration.
	 * 
	 * @return a new <code>JavaScriptSourceViewerConfiguration</code>
	 * 
	 */
	protected JavaScriptSourceViewerConfiguration createTypeScriptSourceViewerConfiguration() {
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
		if (fProjectionSupport != null) {
			fProjectionSupport.dispose();
			fProjectionSupport = null;
		}

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

	protected final ISourceViewer createSourceViewer(Composite parent, IVerticalRuler verticalRuler, int styles) {

		IPreferenceStore store = getPreferenceStore();
		ISourceViewer viewer = createTypeScriptSourceViewer(parent, verticalRuler, getOverviewRuler(),
				isOverviewRulerVisible(), styles, store);

		// JavaUIHelp.setHelp(this, viewer.getTextWidget(),
		// IJavaHelpContextIds.JAVA_EDITOR);

		TypeScriptSourceViewer TypeScriptSourceViewer = null;
		if (viewer instanceof TypeScriptSourceViewer)
			TypeScriptSourceViewer = (TypeScriptSourceViewer) viewer;

		/*
		 * This is a performance optimization to reduce the computation of the
		 * text presentation triggered by {@link #setVisibleDocument(IDocument)}
		 */
		if (TypeScriptSourceViewer != null && isFoldingEnabled()
				&& (store == null || !store.getBoolean(PreferenceConstants.EDITOR_SHOW_SEGMENTS))) {
			// TypeScriptSourceViewer.prepareDelayedProjection();
		}

		// // do not even install projection support until folding is actually
		// // enabled
		// if (isFoldingEnabled()) {
		// installProjectionSupport(TypeScriptSourceViewer);
		// }

		// fProjectionModelUpdater =
		// JavaScriptPlugin.getDefault().getFoldingStructureProviderRegistry()
		// .getCurrentFoldingProvider();
		// if (fProjectionModelUpdater != null) {
		// fProjectionModelUpdater.install(this, projectionViewer);
		// }
		// ensure source viewer decoration support has been created and
		// configured
		getSourceViewerDecorationSupport(viewer);

		return viewer;
	}

	public final ISourceViewer getViewer() {
		return getSourceViewer();
	}

	protected ISourceViewer createTypeScriptSourceViewer(Composite parent, IVerticalRuler verticalRuler,
			IOverviewRuler overviewRuler, boolean isOverviewRulerVisible, int styles, IPreferenceStore store) {
		return new TypeScriptSourceViewer(parent, verticalRuler, getOverviewRuler(), isOverviewRulerVisible(), styles,
				store);
	}

	@Override
	protected void handlePreferenceStoreChanged(PropertyChangeEvent event) {

		String property = event.getProperty();

		if (AbstractDecoratedTextEditorPreferenceConstants.EDITOR_TAB_WIDTH.equals(property)) {
			/*
			 * Ignore tab setting since we rely on the formatter preferences. We
			 * do this outside the try-finally block to avoid that
			 * EDITOR_TAB_WIDTH is handled by the sub-class
			 * (AbstractDecoratedTextEditor).
			 */
			return;
		}

		try {
			ISourceViewer sourceViewer = getSourceViewer();
			if (sourceViewer == null)
				return;

			if (JavaScriptCore.COMPILER_SOURCE.equals(property)) {
				if (event.getNewValue() instanceof String)
					fBracketMatcher.setSourceVersion((String) event.getNewValue());
				// fall through as others are interested in source change as
				// well.
			}

			((JavaScriptSourceViewerConfiguration) getSourceViewerConfiguration()).handlePropertyChangeEvent(event);

			if (PreferenceConstants.EDITOR_FOLDING_PROVIDER.equals(property)) {
				if (sourceViewer instanceof ProjectionViewer) {
					ProjectionViewer pv = (ProjectionViewer) sourceViewer;
					// install projection support if it has not even been
					// installed yet
					if (isFoldingEnabled() && (fProjectionSupport == null)) {
						installProjectionSupport();
					}
					if (pv.isProjectionMode() != isFoldingEnabled()) {
						if (pv.canDoOperation(ProjectionViewer.TOGGLE)) {
							pv.doOperation(ProjectionViewer.TOGGLE);
						}
					}
				}
				return;
			}
		} finally {
			super.handlePreferenceStoreChanged(event);
		}
	}

	/**
	 * Install everything necessary to get document folding working and enable
	 * document folding
	 * 
	 * @param sourceViewer
	 */
	private void installProjectionSupport() {

		ProjectionViewer projectionViewer = (ProjectionViewer) getSourceViewer();
		fProjectionSupport = new ProjectionSupport(projectionViewer, getAnnotationAccess(), getSharedColors());
		fProjectionSupport.addSummarizableAnnotationType("org.eclipse.ui.workbench.texteditor.error"); //$NON-NLS-1$
		fProjectionSupport.addSummarizableAnnotationType("org.eclipse.ui.workbench.texteditor.warning"); //$NON-NLS-1$
		fProjectionSupport.setHoverControlCreator(new IInformationControlCreator() {
			public IInformationControl createInformationControl(Shell parent) {
				return new DefaultInformationControl(parent);
			}
		});
		fProjectionSupport.install();

		if (isFoldingEnabled()) {
			projectionViewer.doOperation(ProjectionViewer.TOGGLE);
		}
	}

	/**
	 * Return whether document folding should be enabled according to the
	 * preference store settings.
	 * 
	 * @return <code>true</code> if document folding should be enabled
	 */
	private boolean isFoldingEnabled() {
		return JavaScriptPlugin.getDefault().getPreferenceStore()
				.getBoolean(PreferenceConstants.EDITOR_FOLDING_ENABLED);
	}
}
