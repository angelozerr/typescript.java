/**
 *  Copyright (c) 2015-2016 Angelo ZERR.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 *  Lorenzo Dalla Vecchia <lorenzo.dallavecchia@webratio.com> - problem tick in title image
 */
package ts.eclipse.ide.jsdt.internal.ui.editor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DefaultLineTracker;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.ISelectionValidator;
import org.eclipse.jface.text.ISynchronizable;
import org.eclipse.jface.text.ITextInputListener;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.ITextViewerExtension5;
import org.eclipse.jface.text.ITextViewerExtension7;
import org.eclipse.jface.text.IWidgetTokenKeeper;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.TabsToSpacesConverter;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.link.LinkedModeModel;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.IAnnotationModelExtension;
import org.eclipse.jface.text.source.IChangeRulerColumn;
import org.eclipse.jface.text.source.IOverviewRuler;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.jface.text.source.IVerticalRulerColumn;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.jface.text.source.patch.LineNumberChangeRulerColumnPatch;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IWindowListener;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionContext;
import org.eclipse.ui.actions.ActionGroup;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditorActionConstants;
import org.eclipse.ui.texteditor.TextOperationAction;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;
import org.eclipse.wst.jsdt.internal.ui.IJavaHelpContextIds;
import org.eclipse.wst.jsdt.internal.ui.actions.AddBlockCommentAction;
import org.eclipse.wst.jsdt.internal.ui.actions.RemoveBlockCommentAction;
import org.eclipse.wst.jsdt.internal.ui.javaeditor.ICompilationUnitDocumentProvider;
import org.eclipse.wst.jsdt.internal.ui.javaeditor.ToggleCommentAction;
import org.eclipse.wst.jsdt.internal.ui.text.PreferencesAdapter;
import org.eclipse.wst.jsdt.ui.PreferenceConstants;

import ts.client.Location;
import ts.client.navbar.NavigationBarItem;
import ts.client.occurrences.OccurrencesResponseItem;
import ts.eclipse.ide.core.TypeScriptCorePlugin;
import ts.eclipse.ide.core.preferences.TypeScriptCorePreferenceConstants;
import ts.eclipse.ide.core.resources.IIDETypeScriptProject;
import ts.eclipse.ide.core.utils.DocumentUtils;
import ts.eclipse.ide.core.utils.TypeScriptResourceUtil;
import ts.eclipse.ide.jsdt.internal.ui.JSDTTypeScriptUIMessages;
import ts.eclipse.ide.jsdt.internal.ui.Trace;
import ts.eclipse.ide.jsdt.internal.ui.actions.CompositeActionGroup;
import ts.eclipse.ide.jsdt.internal.ui.actions.IndentAction;
import ts.eclipse.ide.jsdt.internal.ui.actions.RefactorActionGroup;
import ts.eclipse.ide.jsdt.internal.ui.actions.TypeScriptSearchActionGroup;
import ts.eclipse.ide.jsdt.ui.IContextMenuConstants;
import ts.eclipse.ide.jsdt.ui.actions.ITypeScriptEditorActionDefinitionIds;
import ts.eclipse.ide.ui.TypeScriptUIPlugin;
import ts.eclipse.ide.ui.outline.IEditorOutlineFeatures;
import ts.eclipse.ide.ui.outline.TypeScriptContentOutlinePage;
import ts.eclipse.ide.ui.utils.EditorUtils;
import ts.resources.ITypeScriptFile;

/**
 * TypeScript editor.
 *
 */
public class TypeScriptEditor extends JavaScriptLightWeightEditor implements IEditorOutlineFeatures {

	private static final boolean CODE_ASSIST_DEBUG = "true" //$NON-NLS-1$
			.equalsIgnoreCase(Platform.getDebugOption("ts.eclipse.ide.jsdt.ui/debug/ResultCollector")); //$NON-NLS-1$

	public class AdaptedSourceViewer extends TypeScriptSourceViewer {

		public AdaptedSourceViewer(Composite parent, IVerticalRuler verticalRuler, IOverviewRuler overviewRuler,
				boolean showAnnotationsOverview, int styles, IPreferenceStore store) {
			super(parent, verticalRuler, overviewRuler, showAnnotationsOverview, styles, store);
		}

		public IContentAssistant getContentAssistant() {
			return fContentAssistant;
		}

		/*
		 * @see ITextOperationTarget#doOperation(int)
		 */
		public void doOperation(int operation) {

			if (getTextWidget() == null)
				return;

			switch (operation) {
			case CONTENTASSIST_PROPOSALS:
				long time = CODE_ASSIST_DEBUG ? System.currentTimeMillis() : 0;
				String msg = fContentAssistant.showPossibleCompletions();
				if (CODE_ASSIST_DEBUG) {
					long delta = System.currentTimeMillis() - time;
					System.err.println("Code Assist (total): " + delta); //$NON-NLS-1$
				}
				setStatusLineErrorMessage(msg);
				return;
			case QUICK_ASSIST:
				/*
				 * XXX: We can get rid of this once the SourceViewer has a way
				 * to update the status line
				 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=133787
				 */
				msg = fQuickAssistAssistant.showPossibleQuickAssists();
				setStatusLineErrorMessage(msg);
				return;
			}

			super.doOperation(operation);
		}

		/*
		 * @see IWidgetTokenOwner#requestWidgetToken(IWidgetTokenKeeper)
		 */
		public boolean requestWidgetToken(IWidgetTokenKeeper requester) {
			if (PlatformUI.getWorkbench().getHelpSystem().isContextHelpDisplayed())
				return false;
			return super.requestWidgetToken(requester);
		}

		/*
		 * @see
		 * IWidgetTokenOwnerExtension#requestWidgetToken(IWidgetTokenKeeper,
		 * int)
		 * 
		 */
		public boolean requestWidgetToken(IWidgetTokenKeeper requester, int priority) {
			if (PlatformUI.getWorkbench().getHelpSystem().isContextHelpDisplayed())
				return false;
			return super.requestWidgetToken(requester, priority);
		}

		/*
		 * @see
		 * org.eclipse.jface.text.source.SourceViewer#createFormattingContext()
		 * 
		 */
		// public IFormattingContext createFormattingContext() {
		// IFormattingContext context = new CommentFormattingContext();
		//
		// Map preferences;
		// ITypeScriptFile inputJavaElement = getTypeScriptFile();
		// ITypeScriptProject javaProject = inputJavaElement != null ?
		// inputJavaElement.getProject() : null;
		//// if (javaProject == null)
		//// preferences = new HashMap(JavaScriptCore.getOptions());
		//// else
		//// preferences = new HashMap(javaProject.getOptions(true));
		//
		// //context.setProperty(FormattingContextProperties.CONTEXT_PREFERENCES,
		// preferences);
		//
		// return context;
		// }
	}

	protected CompositeActionGroup fActionGroups;
	private CompositeActionGroup fContextMenuGroup;

	private OccurrencesCollector occurrencesCollector;
	private OccurrencesFinderJob fOccurrencesFinderJob;
	/** The occurrences finder job canceler */
	private OccurrencesFinderJobCanceler fOccurrencesFinderJobCanceler;
	/**
	 * Holds the current occurrence annotations.
	 * 
	 */
	private Annotation[] fOccurrenceAnnotations = null;
	/**
	 * Tells whether all occurrences of the element at the current caret
	 * location are automatically marked in this editor.
	 * 
	 */
	private boolean fMarkOccurrenceAnnotations;
	/**
	 * The selection used when forcing occurrence marking through code.
	 * 
	 */
	private ISelection fForcedMarkOccurrencesSelection;
	/**
	 * The internal shell activation listener for updating occurrences.
	 * 
	 */
	private ActivationListener fActivationListener = new ActivationListener();

	/**
	 * Updates the Java outline page selection and this editor's range
	 * indicator.
	 *
	 * 
	 */
	private class EditorSelectionChangedListener extends AbstractSelectionChangedListener {

		/*
		 * @see
		 * org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged(
		 * org.eclipse.jface.viewers.SelectionChangedEvent)
		 */
		public void selectionChanged(SelectionChangedEvent event) {
			// TypeScriptEditor.this.selectionChanged();

			ISelection selection = event.getSelection();
			if (selection instanceof ITextSelection) {
				// Update occurrences
				ITextSelection textSelection = (ITextSelection) selection;
				updateOccurrenceAnnotations(textSelection);

				TypeScriptContentOutlinePage outlinePage = getOutlinePage();
				if (outlinePage != null && outlinePage.isLinkingEnabled()) {
					fOutlineSelectionChangedListener.uninstall(outlinePage);
					outlinePage.setSelection(selection);
					fOutlineSelectionChangedListener.install(outlinePage);
				}
			}
		}
	}

	/**
	 * Updates the selection in the editor's widget with the selection of the
	 * outline page.
	 */
	class OutlineSelectionChangedListener extends AbstractSelectionChangedListener {
		public void selectionChanged(SelectionChangedEvent event) {
			doSelectionChanged(event);
		}
	}

	/** The selection changed listener */
	protected AbstractSelectionChangedListener fOutlineSelectionChangedListener = new OutlineSelectionChangedListener();

	/**
	 * Outline page
	 */
	private TypeScriptContentOutlinePage contentOutlinePage;

	private final ProblemTickUpdater problemTickUpdater;
	// private CodeLensContribution contribution;

	public TypeScriptEditor() {
		super();
		this.problemTickUpdater = new ProblemTickUpdater(this);
	}

	protected ActionGroup getActionGroup() {
		return fActionGroups;
	}

	@Override
	protected void createActions() {
		super.createActions();

		RefactorActionGroup refactorActionGroup = new RefactorActionGroup(this, ITextEditorActionConstants.GROUP_EDIT);
		ActionGroup searchActionGroup = new TypeScriptSearchActionGroup(this);
		fActionGroups = new CompositeActionGroup(new ActionGroup[] { refactorActionGroup, searchActionGroup });
		fContextMenuGroup = new CompositeActionGroup(new ActionGroup[] { refactorActionGroup, searchActionGroup });

		// Format Action
		IAction action = new TextOperationAction(JSDTTypeScriptUIMessages.getResourceBundle(), "Format.", this, //$NON-NLS-1$
				ISourceViewer.FORMAT);
		action.setActionDefinitionId(ITypeScriptEditorActionDefinitionIds.FORMAT);
		setAction("Format", action); //$NON-NLS-1$
		markAsStateDependentAction("Format", true); //$NON-NLS-1$
		markAsSelectionDependentAction("Format", true); //$NON-NLS-1$
		// PlatformUI.getWorkbench().getHelpSystem().setHelp(action,
		// IJavaHelpContextIds.FORMAT_ACTION);

		action = new ToggleCommentAction(JSDTTypeScriptUIMessages.getResourceBundle(), "ToggleComment.", this); //$NON-NLS-1$
		action.setActionDefinitionId(ITypeScriptEditorActionDefinitionIds.TOGGLE_COMMENT);
		setAction("ToggleComment", action); //$NON-NLS-1$
		markAsStateDependentAction("ToggleComment", true); //$NON-NLS-1$
		// PlatformUI.getWorkbench().getHelpSystem().setHelp(action,
		// IJavaHelpContextIds.TOGGLE_COMMENT_ACTION);
		configureToggleCommentAction();

		action = new AddBlockCommentAction(JSDTTypeScriptUIMessages.getResourceBundle(), "AddBlockComment.", this); //$NON-NLS-1$
		action.setActionDefinitionId(ITypeScriptEditorActionDefinitionIds.ADD_BLOCK_COMMENT);
		setAction("AddBlockComment", action); //$NON-NLS-1$
		markAsStateDependentAction("AddBlockComment", true); //$NON-NLS-1$
		markAsSelectionDependentAction("AddBlockComment", true); //$NON-NLS-1$
		// PlatformUI.getWorkbench().getHelpSystem().setHelp(action,
		// IJavaHelpContextIds.ADD_BLOCK_COMMENT_ACTION);

		action = new RemoveBlockCommentAction(JSDTTypeScriptUIMessages.getResourceBundle(), "RemoveBlockComment.", //$NON-NLS-1$
				this);
		action.setActionDefinitionId(ITypeScriptEditorActionDefinitionIds.REMOVE_BLOCK_COMMENT);
		setAction("RemoveBlockComment", action); //$NON-NLS-1$
		markAsStateDependentAction("RemoveBlockComment", true); //$NON-NLS-1$
		markAsSelectionDependentAction("RemoveBlockComment", true); //$NON-NLS-1$
		// PlatformUI.getWorkbench().getHelpSystem().setHelp(action,
		// IJavaHelpContextIds.REMOVE_BLOCK_COMMENT_ACTION);

		action = new IndentAction(JSDTTypeScriptUIMessages.getResourceBundle(), "Indent.", this, false); //$NON-NLS-1$
		action.setActionDefinitionId(ITypeScriptEditorActionDefinitionIds.INDENT);
		setAction("Indent", action); //$NON-NLS-1$
		markAsStateDependentAction("Indent", true); //$NON-NLS-1$
		markAsSelectionDependentAction("Indent", true); //$NON-NLS-1$
		PlatformUI.getWorkbench().getHelpSystem().setHelp(action, IJavaHelpContextIds.INDENT_ACTION);

		action = new IndentAction(JSDTTypeScriptUIMessages.getResourceBundle(), "Indent.", this, true); //$NON-NLS-1$
		setAction("IndentOnTab", action); //$NON-NLS-1$
		markAsStateDependentAction("IndentOnTab", true); //$NON-NLS-1$
		markAsSelectionDependentAction("IndentOnTab", true); //$NON-NLS-1$

		if (getPreferenceStore().getBoolean(PreferenceConstants.EDITOR_SMART_TAB)) {
			// don't replace Shift Right - have to make sure their enablement is
			// mutually exclusive
			// removeActionActivationCode(ITextEditorActionConstants.SHIFT_RIGHT);
			setActionActivationCode("IndentOnTab", '\t', -1, SWT.NONE); //$NON-NLS-1$
		}

		action = new TextOperationAction(JSDTTypeScriptUIMessages.getResourceBundle(), "ShowOutline.", this, //$NON-NLS-1$
				TypeScriptSourceViewer.SHOW_OUTLINE, true);
		action.setActionDefinitionId(ITypeScriptEditorActionDefinitionIds.SHOW_OUTLINE);
		setAction(ITypeScriptEditorActionDefinitionIds.SHOW_OUTLINE, action);
		// PlatformUI.getWorkbench().getHelpSystem().setHelp(action,
		// IJavaHelpContextIds.SHOW_OUTLINE_ACTION);

		action = new TextOperationAction(JSDTTypeScriptUIMessages.getResourceBundle(), "OpenImplementation.", this, //$NON-NLS-1$
				TypeScriptSourceViewer.OPEN_IMPLEMENTATION, true);
		action.setActionDefinitionId(ITypeScriptEditorActionDefinitionIds.OPEN_IMPLEMENTATION);
		setAction(ITypeScriptEditorActionDefinitionIds.OPEN_IMPLEMENTATION, action);

	}

	@Override
	protected void initializeKeyBindingScopes() {
		setKeyBindingScopes(new String[] { "ts.eclipse.ide.jsdt.ui.typeScriptViewScope" }); //$NON-NLS-1$
	}

	@Override
	public void editorContextMenuAboutToShow(IMenuManager menu) {

		super.editorContextMenuAboutToShow(menu);
		menu.insertAfter(IContextMenuConstants.GROUP_OPEN, new GroupMarker(IContextMenuConstants.GROUP_SHOW));

		ActionContext context = new ActionContext(getSelectionProvider().getSelection());
		fContextMenuGroup.setContext(context);
		fContextMenuGroup.fillContextMenu(menu);
		fContextMenuGroup.setContext(null);

		// Quick views
		IAction action = getAction(ITypeScriptEditorActionDefinitionIds.SHOW_OUTLINE);
		menu.appendToGroup(IContextMenuConstants.GROUP_OPEN, action);
		action = getAction(ITypeScriptEditorActionDefinitionIds.OPEN_IMPLEMENTATION);
		menu.appendToGroup(IContextMenuConstants.GROUP_OPEN, action);

	}

	@Override
	public void dispose() {
		super.dispose();

		problemTickUpdater.dispose();

		if (fActionGroups != null) {
			fActionGroups.dispose();
			fActionGroups = null;
		}

		if (editorSelectionChangedListener != null) {
			editorSelectionChangedListener.uninstall(getSelectionProvider());
			editorSelectionChangedListener = null;
		}
		uninstallOccurrencesFinder();

		if (fActivationListener != null) {
			PlatformUI.getWorkbench().removeWindowListener(fActivationListener);
			fActivationListener = null;
		}

		// if (contribution != null) {
		// contribution.dispose();
		// }
	}

	void updateTitleImage(Image titleImage) {
		setTitleImage(titleImage);
	}

	@Override
	protected void initializeEditor() {
		super.initializeEditor();
		IPreferenceStore store = getPreferenceStore();
		fMarkOccurrenceAnnotations = store.getBoolean(PreferenceConstants.EDITOR_MARK_OCCURRENCES);
	}

	@Override
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);

		if (isMarkingOccurrences()) {
			installOccurrencesFinder(false);
		}
		PlatformUI.getWorkbench().addWindowListener(fActivationListener);
		editorSelectionChangedListener = new EditorSelectionChangedListener();
		editorSelectionChangedListener.install(getSelectionProvider());

		// if (isActivateCodeLenses()) {
		// installCodeLenses();
		// }
	}

	// protected boolean isActivateCodeLenses() {
	// IPreferenceStore store = getPreferenceStore();
	// return store != null &&
	// store.getBoolean(TypeScriptUIPreferenceConstants.EDITOR_ACTIVATE_CODELENS);
	// }

	// private void installCodeLenses() {
	// try {
	// ITextViewer textViewer = getSourceViewer();
	// contribution = new CodeLensContribution(textViewer);
	// contribution.addTarget(CODELENS_TARGET);
	// //contribution.start();
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	// }
	//
	// public CodeLensContribution getCodeLensContribution() {
	// return contribution;
	// };

	/*
	 * @see AbstractTextEditor#handlePreferenceStoreChanged(PropertyChangeEvent)
	 */
	protected void handlePreferenceStoreChanged(PropertyChangeEvent event) {
		String property = event.getProperty();
		try {

			ISourceViewer sourceViewer = getSourceViewer();
			if (sourceViewer == null)
				return;

			/*
			 * if (AbstractDecoratedTextEditorPreferenceConstants.
			 * EDITOR_SPACES_FOR_TABS.equals(property) ||
			 * TypeScriptCorePreferenceConstants.EDITOR_OPTIONS_INDENT_SIZE.
			 * equals(property) ||
			 * AbstractDecoratedTextEditorPreferenceConstants.EDITOR_TAB_WIDTH.
			 * equals(property)) {
			 */
			if (TypeScriptCorePreferenceConstants.EDITOR_OPTIONS_CONVERT_TABS_TO_SPACES.equals(property)) {
				if (isTabsToSpacesConversionEnabled())
					installTabsToSpacesConverter();
				else
					uninstallTabsToSpacesConverter();
				updateTabs(sourceViewer);
				return;
			}

			if (TypeScriptCorePreferenceConstants.EDITOR_OPTIONS_TAB_SIZE.equals(property)
					|| TypeScriptCorePreferenceConstants.EDITOR_OPTIONS_INDENT_SIZE.equals(property)) {
				updateTabs(sourceViewer);
				return;
			}

			if (PreferenceConstants.EDITOR_SMART_TAB.equals(property)) {
				if (getPreferenceStore().getBoolean(PreferenceConstants.EDITOR_SMART_TAB)) {
					setActionActivationCode("IndentOnTab", '\t', -1, SWT.NONE); //$NON-NLS-1$
				} else {
					removeActionActivationCode("IndentOnTab"); //$NON-NLS-1$
				}
			}

			boolean newBooleanValue = false;
			Object newValue = event.getNewValue();
			if (newValue != null)
				newBooleanValue = Boolean.valueOf(newValue.toString()).booleanValue();

			if (PreferenceConstants.EDITOR_MARK_OCCURRENCES.equals(property)) {
				if (newBooleanValue != fMarkOccurrenceAnnotations) {
					fMarkOccurrenceAnnotations = newBooleanValue;
					if (!fMarkOccurrenceAnnotations)
						uninstallOccurrencesFinder();
					else
						installOccurrencesFinder(true);
				}
				return;
			}
		} finally {
			super.handlePreferenceStoreChanged(event);
		}
	}

	private void updateTabs(ISourceViewer sourceViewer) {
		StyledText textWidget = sourceViewer.getTextWidget();
		int tabWidth = getSourceViewerConfiguration().getTabWidth(sourceViewer);
		if (textWidget.getTabs() != tabWidth)
			textWidget.setTabs(tabWidth);
	}

	@Override
	protected void addPreferenceStores(List stores, IEditorInput input) {
		IResource file = input != null ? EditorUtils.getResource(input) : null;
		if (file != null) {
			stores.add(
					new EclipsePreferencesAdapter(new ProjectScope(file.getProject()), TypeScriptCorePlugin.PLUGIN_ID));
			stores.add(
					new EclipsePreferencesAdapter(new ProjectScope(file.getProject()), TypeScriptUIPlugin.PLUGIN_ID));
		}
		stores.add(TypeScriptUIPlugin.getDefault().getPreferenceStore());
		stores.add(new PreferencesAdapter(TypeScriptCorePlugin.getDefault().getPluginPreferences()));
		super.addPreferenceStores(stores, input);
	}

	// ---------------------- Occurrences

	private EditorSelectionChangedListener editorSelectionChangedListener;
	private CompletableFuture<List<OccurrencesResponseItem>> occurrencesFuture;

	/**
	 * Internal activation listener.
	 * 
	 */
	private class ActivationListener implements IWindowListener {

		/*
		 * @see org.eclipse.ui.IWindowListener#windowActivated(org.eclipse.ui.
		 * IWorkbenchWindow)
		 * 
		 */
		public void windowActivated(IWorkbenchWindow window) {
			if (window == getEditorSite().getWorkbenchWindow() && fMarkOccurrenceAnnotations && isActivePart()) {
				fForcedMarkOccurrencesSelection = getSelectionProvider().getSelection();
				updateOccurrenceAnnotations((ITextSelection) fForcedMarkOccurrencesSelection);
			}
		}

		/*
		 * @see org.eclipse.ui.IWindowListener#windowDeactivated(org.eclipse.ui.
		 * IWorkbenchWindow)
		 * 
		 */
		public void windowDeactivated(IWorkbenchWindow window) {
			if (window == getEditorSite().getWorkbenchWindow() && fMarkOccurrenceAnnotations && isActivePart())
				removeOccurrenceAnnotations();
		}

		/*
		 * @see org.eclipse.ui.IWindowListener#windowClosed(org.eclipse.ui.
		 * IWorkbenchWindow)
		 * 
		 */
		public void windowClosed(IWorkbenchWindow window) {
		}

		/*
		 * @see org.eclipse.ui.IWindowListener#windowOpened(org.eclipse.ui.
		 * IWorkbenchWindow)
		 * 
		 */
		public void windowOpened(IWorkbenchWindow window) {
		}
	}

	/**
	 * Finds and marks occurrence annotations.
	 *
	 * 
	 */
	class OccurrencesFinderJob extends Job {

		private IDocument fDocument;
		private ISelection fSelection;
		private ISelectionValidator fPostSelectionValidator;
		private boolean fCanceled = false;
		private IProgressMonitor fProgressMonitor;
		private Position[] fPositions;

		public OccurrencesFinderJob(IDocument document, Position[] positions, ISelection selection) {
			super(JSDTTypeScriptUIMessages.TypeScriptEditor_markOccurrences_job_name);
			fDocument = document;
			fSelection = selection;
			fPositions = positions;

			if (getSelectionProvider() instanceof ISelectionValidator)
				fPostSelectionValidator = (ISelectionValidator) getSelectionProvider();
		}

		// cannot use cancel() because it is declared final
		void doCancel() {
			fCanceled = true;
			cancel();
		}

		private boolean isCanceled() {
			return fCanceled || fProgressMonitor.isCanceled() || fPostSelectionValidator != null
					&& !(fPostSelectionValidator.isValid(fSelection) || fForcedMarkOccurrencesSelection == fSelection)
					|| LinkedModeModel.hasInstalledModel(fDocument);
		}

		/*
		 * @see Job#run(org.eclipse.core.runtime.IProgressMonitor)
		 */
		public IStatus run(IProgressMonitor progressMonitor) {
			fProgressMonitor = progressMonitor;

			if (isCanceled()) {
				if (LinkedModeModel.hasInstalledModel(fDocument)) {
					// Template completion applied, remove occurrences
					removeOccurrenceAnnotations();
				}
				return Status.CANCEL_STATUS;
			}
			ITextViewer textViewer = getViewer();
			if (textViewer == null)
				return Status.CANCEL_STATUS;

			IDocument document = textViewer.getDocument();
			if (document == null)
				return Status.CANCEL_STATUS;

			IAnnotationModel annotationModel = getAnnotationModel();
			if (annotationModel == null)
				return Status.CANCEL_STATUS;

			// Add occurrence annotations
			int length = fPositions.length;
			Map annotationMap = new HashMap(length);
			for (int i = 0; i < length; i++) {

				if (isCanceled())
					return Status.CANCEL_STATUS;

				String message;
				Position position = fPositions[i];

				// Create & add annotation
				try {
					message = document.get(position.offset, position.length);
				} catch (BadLocationException ex) {
					// Skip this match
					continue;
				}
				annotationMap.put(new Annotation("org.eclipse.wst.jsdt.ui.occurrences", false, message), //$NON-NLS-1$
						position);
			}

			if (isCanceled())
				return Status.CANCEL_STATUS;

			synchronized (getLockObject(annotationModel)) {
				if (annotationModel instanceof IAnnotationModelExtension) {
					((IAnnotationModelExtension) annotationModel).replaceAnnotations(fOccurrenceAnnotations,
							annotationMap);
				} else {
					removeOccurrenceAnnotations();
					Iterator iter = annotationMap.entrySet().iterator();
					while (iter.hasNext()) {
						Map.Entry mapEntry = (Map.Entry) iter.next();
						annotationModel.addAnnotation((Annotation) mapEntry.getKey(), (Position) mapEntry.getValue());
					}
				}
				fOccurrenceAnnotations = (Annotation[]) annotationMap.keySet()
						.toArray(new Annotation[annotationMap.keySet().size()]);
			}

			return Status.OK_STATUS;
		}

		private IAnnotationModel getAnnotationModel() {
			IDocumentProvider documentProvider = getDocumentProvider();
			if (documentProvider == null) {
				return null;
			}
			return documentProvider.getAnnotationModel(getEditorInput());
		}
	}

	/**
	 * Cancels the occurrences finder job upon document changes.
	 *
	 * 
	 */
	class OccurrencesFinderJobCanceler implements IDocumentListener, ITextInputListener {

		public void install() {
			ISourceViewer sourceViewer = getSourceViewer();
			if (sourceViewer == null)
				return;

			StyledText text = sourceViewer.getTextWidget();
			if (text == null || text.isDisposed())
				return;

			sourceViewer.addTextInputListener(this);

			IDocument document = sourceViewer.getDocument();
			if (document != null)
				document.addDocumentListener(this);
		}

		public void uninstall() {
			ISourceViewer sourceViewer = getSourceViewer();
			if (sourceViewer != null)
				sourceViewer.removeTextInputListener(this);

			IDocumentProvider documentProvider = getDocumentProvider();
			if (documentProvider != null) {
				IDocument document = documentProvider.getDocument(getEditorInput());
				if (document != null)
					document.removeDocumentListener(this);
			}
		}

		/*
		 * @see
		 * org.eclipse.jface.text.IDocumentListener#documentAboutToBeChanged(org
		 * .eclipse.jface.text.DocumentEvent)
		 */
		public void documentAboutToBeChanged(DocumentEvent event) {
			if (fOccurrencesFinderJob != null)
				fOccurrencesFinderJob.doCancel();
		}

		/*
		 * @see
		 * org.eclipse.jface.text.IDocumentListener#documentChanged(org.eclipse.
		 * jface.text.DocumentEvent)
		 */
		public void documentChanged(DocumentEvent event) {
		}

		/*
		 * @see org.eclipse.jface.text.ITextInputListener#
		 * inputDocumentAboutToBeChanged(org.eclipse.jface.text.IDocument,
		 * org.eclipse.jface.text.IDocument)
		 */
		public void inputDocumentAboutToBeChanged(IDocument oldInput, IDocument newInput) {
			if (oldInput == null)
				return;

			oldInput.removeDocumentListener(this);
		}

		/*
		 * @see
		 * org.eclipse.jface.text.ITextInputListener#inputDocumentChanged(org.
		 * eclipse.jface.text.IDocument, org.eclipse.jface.text.IDocument)
		 */
		public void inputDocumentChanged(IDocument oldInput, IDocument newInput) {
			if (newInput == null)
				return;
			newInput.addDocumentListener(this);
		}
	}

	class OccurrencesCollector {

		private IDocument document;
		private List<Position> positions;
		private ITextSelection selection;

		public OccurrencesCollector() {
			this.positions = new ArrayList<Position>();
		}

		public void setDocument(IDocument document) {
			this.document = document;
		}

		// @Override
		public void startCollect() {
			this.positions.clear();
		}

		// @Override
		public void endCollect() {
			fOccurrencesFinderJob = new OccurrencesFinderJob(document, positions.toArray(new Position[0]), selection);
			fOccurrencesFinderJob.run(new NullProgressMonitor());
		}

		public void addOccurrence(OccurrencesResponseItem occurrence) {
			try {
				int start = DocumentUtils.getPosition(document, occurrence.getStart());
				int end = DocumentUtils.getPosition(document, occurrence.getEnd());
				int offset = start;
				int length = end - start;
				positions.add(new Position(offset, length));
			} catch (Exception e) {
				// Trace.trace(Trace.SEVERE, "Error while getting TypeScript
				// occurrences.", e);
			}
		}

		public void setSelection(ITextSelection selection) {
			this.selection = selection;
		}
	}

	/**
	 * Updates the occurrences annotations based on the current selection.
	 *
	 * @param selection
	 *            the text selection
	 * 
	 */
	private void updateOccurrenceAnnotations(ITextSelection selection) {
		if (fOccurrencesFinderJob != null)
			fOccurrencesFinderJob.cancel();

		if (!fMarkOccurrenceAnnotations) {
			return;
		}

		if (selection == null) {
			return;
		}

		final IDocument document = getSourceViewer().getDocument();
		if (document == null)
			return;

		if (occurrencesCollector == null) {
			occurrencesCollector = new OccurrencesCollector();
		}
		occurrencesCollector.setDocument(document);
		try {
			ITypeScriptFile tsFile = getTypeScriptFile(document);
			if (tsFile != null) {
				occurrencesCollector.setSelection(selection);
				if (occurrencesFuture != null && !occurrencesFuture.isDone()) {
					occurrencesFuture.cancel(true);
				}
				occurrencesFuture = tsFile.occurrences(selection.getOffset());
				occurrencesFuture.thenAccept(occurrences -> {
					occurrencesCollector.startCollect();

					for (OccurrencesResponseItem occurrence : occurrences) {
						occurrencesCollector.addOccurrence(occurrence);
					}
					occurrencesCollector.endCollect();
				});
			}

		} catch (Exception e) {
			Trace.trace(Trace.SEVERE, "Error while getting TypeScript occurrences.", e);
		}

	}

	private ITypeScriptFile getTypeScriptFile(IDocument document) {
		IResource file = EditorUtils.getResource(this);
		if (file != null) {
			IIDETypeScriptProject tsProject;
			try {
				tsProject = TypeScriptResourceUtil.getTypeScriptProject(file.getProject());
				return tsProject.openFile(file, document);
			} catch (Exception e) {
				Trace.trace(Trace.SEVERE, "Error while getting typscript file", e);
				return null;
			}
		}
		IFileStore fs = EditorUtils.getFileStore(this);
		if (fs != null) {
			// TODO
		}
		return null;
	}

	public ITypeScriptFile getTypeScriptFile() {
		final IDocument document = getSourceViewer().getDocument();
		return getTypeScriptFile(document);
	}

	protected void installOccurrencesFinder(boolean forceUpdate) {
		fMarkOccurrenceAnnotations = true;

		// fPostSelectionListenerWithAST= new ISelectionListenerWithAST() {
		// public void selectionChanged(IEditorPart part, ITextSelection
		// selection, JavaScriptUnit astRoot) {
		// updateOccurrenceAnnotations(selection, astRoot);
		// }
		// };
		// SelectionListenerWithASTManager.getDefault().addListener(this,
		// fPostSelectionListenerWithAST);
		if (forceUpdate && getSelectionProvider() != null) {
			fForcedMarkOccurrencesSelection = getSelectionProvider().getSelection();
			updateOccurrenceAnnotations((ITextSelection) fForcedMarkOccurrencesSelection);
		}

		if (fOccurrencesFinderJobCanceler == null) {
			fOccurrencesFinderJobCanceler = new OccurrencesFinderJobCanceler();
			fOccurrencesFinderJobCanceler.install();
		}
	}

	protected void uninstallOccurrencesFinder() {
		fMarkOccurrenceAnnotations = false;

		if (fOccurrencesFinderJob != null) {
			fOccurrencesFinderJob.cancel();
			fOccurrencesFinderJob = null;
		}

		if (fOccurrencesFinderJobCanceler != null) {
			fOccurrencesFinderJobCanceler.uninstall();
			fOccurrencesFinderJobCanceler = null;
		}

		occurrencesCollector = null;

		// if (fPostSelectionListenerWithAST != null) {
		// SelectionListenerWithASTManager.getDefault().removeListener(this,
		// fPostSelectionListenerWithAST);
		// fPostSelectionListenerWithAST= null;
		// }

		removeOccurrenceAnnotations();
	}

	private void removeOccurrenceAnnotations() {
		// fMarkOccurrenceModificationStamp=
		// IDocumentExtension4.UNKNOWN_MODIFICATION_STAMP;
		// fMarkOccurrenceTargetRegion= null;

		IDocumentProvider documentProvider = getDocumentProvider();
		if (documentProvider == null)
			return;

		IAnnotationModel annotationModel = documentProvider.getAnnotationModel(getEditorInput());
		if (annotationModel == null || fOccurrenceAnnotations == null)
			return;

		synchronized (getLockObject(annotationModel)) {
			if (annotationModel instanceof IAnnotationModelExtension) {
				((IAnnotationModelExtension) annotationModel).replaceAnnotations(fOccurrenceAnnotations, null);
			} else {
				for (int i = 0, length = fOccurrenceAnnotations.length; i < length; i++)
					annotationModel.removeAnnotation(fOccurrenceAnnotations[i]);
			}
			fOccurrenceAnnotations = null;
		}
	}

	/**
	 * Returns the lock object for the given annotation model.
	 *
	 * @param annotationModel
	 *            the annotation model
	 * @return the annotation model's lock object
	 * 
	 */
	private Object getLockObject(IAnnotationModel annotationModel) {
		if (annotationModel instanceof ISynchronizable) {
			Object lock = ((ISynchronizable) annotationModel).getLockObject();
			if (lock != null)
				return lock;
		}
		return annotationModel;
	}

	@Override
	protected void doSetInput(IEditorInput input) throws CoreException {
		super.doSetInput(input);
		// problemTickUpdater.update();
		configureToggleCommentAction();
		// try {
		// //IDocument document = getSourceViewer().getDocument();
		// //setOutlinePageInput(getTypeScriptFile(document));
		// } catch (TypeScriptException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }

	}
	// -------------- Outline

	@Override
	public Object getAdapter(@SuppressWarnings("rawtypes") Class key) {
		if (key.equals(IContentOutlinePage.class)) {
			return getOutlinePage();
		}
		return super.getAdapter(key);
	}

	/**
	 * Gets an outline page
	 * 
	 * @return an outline page
	 */
	public TypeScriptContentOutlinePage getOutlinePage() {
		if (contentOutlinePage == null) {
			contentOutlinePage = new TypeScriptContentOutlinePage(this);
			fOutlineSelectionChangedListener.install(contentOutlinePage);
			IDocument document = getSourceViewer().getDocument();
			try {
				setOutlinePageInput(getTypeScriptFile(document));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return contentOutlinePage;
	}

	private void setOutlinePageInput(ITypeScriptFile tsFile) {
		// try {
		contentOutlinePage.setInput(tsFile);
		// } catch (TypeScriptException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }

	}

	/**
	 * React to changed selection.
	 *
	 * 
	 */
	protected void selectionChanged() {
		if (getSelectionProvider() == null) {
			return;
		}
	}

	protected void doSelectionChanged(SelectionChangedEvent event) {
		ISelection selection = event.getSelection();
		NavigationBarItem item = null;
		Iterator iter = ((IStructuredSelection) selection).iterator();
		while (iter.hasNext()) {
			Object o = iter.next();
			if (o instanceof NavigationBarItem) {
				item = (NavigationBarItem) o;
				break;
			}
		}

		setSelection(item, !isActivePart());

		ISelectionProvider selectionProvider = getSelectionProvider();
		if (selectionProvider == null)
			return;

		ISelection textSelection = selectionProvider.getSelection();
		if (!(textSelection instanceof ITextSelection))
			return;

		fForcedMarkOccurrencesSelection = textSelection;
		updateOccurrenceAnnotations((ITextSelection) textSelection);

	}

	/**
	 * Highlights and moves to a corresponding element in editor
	 * 
	 * @param reference
	 *            corresponding entity in editor
	 * @param moveCursor
	 *            if true, moves cursor to the reference
	 */
	private void setSelection(NavigationBarItem reference, boolean moveCursor) {
		if (reference == null) {
			return;
		}

		if (moveCursor) {
			markInNavigationHistory();
		}

		ISourceViewer sourceViewer = getSourceViewer();
		if (sourceViewer == null) {
			return;
		}
		StyledText textWidget = sourceViewer.getTextWidget();
		if (textWidget == null) {
			return;
		}
		try {
			Location start = reference.getSpans().get(0).getStart();
			Location end = reference.getSpans().get(0).getEnd();

			if (start == null || end == null)
				return;

			ITypeScriptFile tsFile = getTypeScriptFile();

			int offset = tsFile.getPosition(start);
			int length = tsFile.getPosition(end) - offset;

			if (offset < 0 || length < 0 || length > sourceViewer.getDocument().getLength()) {
				return;
			}
			textWidget.setRedraw(false);

			// Uncomment that if we wish to select only variable and not the
			// whole block.
			// but there is a bug with this code with
			// private a: string. it's the first 'a' (of private) which is
			// selected and not the second.
			// String documentPart = sourceViewer.getDocument().get(offset,
			// length);
			//
			// // Try to find name because position returns for whole block
			// String name = reference.getText();
			// if (name != null) {
			// int nameoffset = documentPart.indexOf(name);
			// if (nameoffset != -1) {
			// offset += nameoffset;
			// length = name.length();
			// }
			// }
			if (length > 0) {
				setHighlightRange(offset, length, moveCursor);
			}

			if (!moveCursor) {
				return;
			}

			if (offset > -1 && length > 0) {
				sourceViewer.revealRange(offset, length);
				// Selected region begins one index after offset
				sourceViewer.setSelectedRange(offset, length);
				markInNavigationHistory();
			}
		} catch (Exception e) {

		} finally {
			textWidget.setRedraw(true);
		}
	}

	/**
	 * Configures the toggle comment action
	 *
	 * 
	 */
	private void configureToggleCommentAction() {
		IAction action = getAction("ToggleComment"); //$NON-NLS-1$
		if (action instanceof ToggleCommentAction) {
			ISourceViewer sourceViewer = getSourceViewer();
			SourceViewerConfiguration configuration = getSourceViewerConfiguration();
			((ToggleCommentAction) action).configure(sourceViewer, configuration);
		}
	}

	@Override
	protected void installTabsToSpacesConverter() {
		ISourceViewer sourceViewer = getSourceViewer();
		SourceViewerConfiguration config = getSourceViewerConfiguration();
		if (config != null && sourceViewer instanceof ITextViewerExtension7) {
			int tabWidth = config.getTabWidth(sourceViewer);
			TabsToSpacesConverter tabToSpacesConverter = new TabsToSpacesConverter();
			tabToSpacesConverter.setNumberOfSpacesPerTab(tabWidth);
			IDocumentProvider provider = getDocumentProvider();
			if (provider instanceof ICompilationUnitDocumentProvider) {
				ICompilationUnitDocumentProvider cup = (ICompilationUnitDocumentProvider) provider;
				tabToSpacesConverter.setLineTracker(cup.createLineTracker(getEditorInput()));
			} else
				tabToSpacesConverter.setLineTracker(new DefaultLineTracker());
			((ITextViewerExtension7) sourceViewer).setTabsToSpacesConverter(tabToSpacesConverter);
			updateIndentPrefixes();
		}
	}

	@Override
	protected boolean isTabsToSpacesConversionEnabled() {
		ITypeScriptFile tsFile = getTypeScriptFile();
		if (tsFile != null) {
			return tsFile.getFormatOptions().getConvertTabsToSpaces();
		}
		return super.isTabsToSpacesConversionEnabled();
	}

	@Override
	public int getCursorOffset() {
		ISourceViewer sourceViewer = getSourceViewer();
		StyledText styledText = sourceViewer.getTextWidget();
		if (styledText == null) {
			return 0;
		}
		if (sourceViewer instanceof ITextViewerExtension5) {
			ITextViewerExtension5 extension = (ITextViewerExtension5) sourceViewer;
			return extension.widgetOffset2ModelOffset(styledText.getCaretOffset());
		} else {
			int offset = sourceViewer.getVisibleRegion().getOffset();
			return offset + styledText.getCaretOffset();
		}
	}

	/**
	 * Creates a new line number ruler column that is appropriately initialized.
	 *
	 * @return the created line number column
	 */
	protected IVerticalRulerColumn createLineNumberRulerColumn() {
		/*
		 * Left for compatibility. See LineNumberColumn.
		 */
		fLineNumberRulerColumn = LineNumberChangeRulerColumnPatch.create(getSharedColors());
		((IChangeRulerColumn) fLineNumberRulerColumn).setHover(createChangeHover());
		initializeLineNumberRulerColumn(fLineNumberRulerColumn);
		return fLineNumberRulerColumn;
	}
}
