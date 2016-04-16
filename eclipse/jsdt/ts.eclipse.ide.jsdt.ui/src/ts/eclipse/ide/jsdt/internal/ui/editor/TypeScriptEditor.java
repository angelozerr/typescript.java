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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.ISelectionValidator;
import org.eclipse.jface.text.ISynchronizable;
import org.eclipse.jface.text.ITextInputListener;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.link.LinkedModeModel;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.IAnnotationModelExtension;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.IPostSelectionProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWindowListener;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionContext;
import org.eclipse.ui.actions.ActionGroup;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditorPreferenceConstants;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.TextOperationAction;
import org.eclipse.wst.jsdt.ui.IContextMenuConstants;
import org.eclipse.wst.jsdt.ui.PreferenceConstants;

import ts.TypeScriptException;
import ts.client.ICancellationToken;
import ts.client.ITypeScriptAsynchCollector;
import ts.client.occurrences.ITypeScriptOccurrencesCollector;
import ts.eclipse.ide.core.resources.IIDETypeScriptProject;
import ts.eclipse.ide.core.utils.TypeScriptResourceUtil;
import ts.eclipse.ide.jsdt.internal.ui.Trace;
import ts.eclipse.ide.jsdt.internal.ui.actions.CompositeActionGroup;
import ts.eclipse.ide.jsdt.internal.ui.actions.JavaSearchActionGroup;
import ts.eclipse.ide.jsdt.ui.actions.ITypeScriptEditorActionDefinitionIds;
import ts.eclipse.ide.ui.utils.EditorUtils;
import ts.resources.ITypeScriptFile;

/**
 * TypeScript editor.
 *
 */
public class TypeScriptEditor extends JavaScriptLightWeightEditor {

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

	protected ActionGroup getActionGroup() {
		return fActionGroups;
	}

	@Override
	protected void createActions() {
		super.createActions();

		ActionGroup oeg, ovg, jsg;
		fActionGroups = new CompositeActionGroup(
				new ActionGroup[] { /*
									 * oeg = new OpenEditorActionGroup(this),
									 * ovg = new OpenViewActionGroup(this),
									 */ jsg = new JavaSearchActionGroup(this) });
		fContextMenuGroup = new CompositeActionGroup(
				new ActionGroup[] { /* oeg, ovg, */ jsg });

		// Format Action
		IAction action = new TextOperationAction(TypeScriptEditorMessages.getResourceBundle(), "Format.", this, //$NON-NLS-1$
				ISourceViewer.FORMAT);
		action.setActionDefinitionId(ITypeScriptEditorActionDefinitionIds.FORMAT);
		setAction("Format", action); //$NON-NLS-1$
		markAsStateDependentAction("Format", true); //$NON-NLS-1$
		markAsSelectionDependentAction("Format", true); //$NON-NLS-1$
		// PlatformUI.getWorkbench().getHelpSystem().setHelp(action,
		// IJavaHelpContextIds.FORMAT_ACTION);
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
		// IAction action=
		// getAction(IJavaEditorActionDefinitionIds.SHOW_OUTLINE);
		// menu.appendToGroup(IContextMenuConstants.GROUP_OPEN, action);
		// action= getAction(IJavaEditorActionDefinitionIds.OPEN_HIERARCHY);
		// menu.appendToGroup(IContextMenuConstants.GROUP_OPEN, action);

	}

	@Override
	public void dispose() {
		super.dispose();

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
	}

	/*
	 * @see AbstractTextEditor#handlePreferenceStoreChanged(PropertyChangeEvent)
	 */
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

	// ---------------------- Occurrences

	private class EditorSelectionChangedListener implements ISelectionChangedListener {

		public void install(ISelectionProvider selectionProvider) {
			if (selectionProvider == null) {
				return;
			}

			if (selectionProvider instanceof IPostSelectionProvider) {
				IPostSelectionProvider provider = (IPostSelectionProvider) selectionProvider;
				provider.addPostSelectionChangedListener(this);
			} else {
				selectionProvider.addSelectionChangedListener(this);
			}
		}

		public void uninstall(ISelectionProvider selectionProvider) {
			if (selectionProvider == null) {
				return;
			}

			if (selectionProvider instanceof IPostSelectionProvider) {
				IPostSelectionProvider provider = (IPostSelectionProvider) selectionProvider;
				provider.removePostSelectionChangedListener(this);
			} else {
				selectionProvider.removeSelectionChangedListener(this);
			}
		}

		@Override
		public void selectionChanged(SelectionChangedEvent event) {
			ISelection selection = event.getSelection();
			if (selection instanceof ITextSelection) {
				ITextSelection textSelection = (ITextSelection) selection;
				updateOccurrenceAnnotations(textSelection);
			}
		}
	}

	private EditorSelectionChangedListener editorSelectionChangedListener;

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
			super(TypeScriptEditorMessages.TypeScriptEditor_markOccurrences_job_name);
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
			return fCanceled || fProgressMonitor.isCanceled()
					|| fPostSelectionValidator != null && !(fPostSelectionValidator.isValid(fSelection)
							|| fForcedMarkOccurrencesSelection == fSelection)
					|| LinkedModeModel.hasInstalledModel(fDocument);
		}

		/*
		 * @see Job#run(org.eclipse.core.runtime.IProgressMonitor)
		 */
		public IStatus run(IProgressMonitor progressMonitor) {

			fProgressMonitor = progressMonitor;

			if (isCanceled())
				return Status.CANCEL_STATUS;

			ITextViewer textViewer = getViewer();
			if (textViewer == null)
				return Status.CANCEL_STATUS;

			IDocument document = textViewer.getDocument();
			if (document == null)
				return Status.CANCEL_STATUS;

			IDocumentProvider documentProvider = getDocumentProvider();
			if (documentProvider == null)
				return Status.CANCEL_STATUS;

			IAnnotationModel annotationModel = documentProvider.getAnnotationModel(getEditorInput());
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

	class OccurrencesCollector
			implements ITypeScriptOccurrencesCollector, ITypeScriptAsynchCollector, ICancellationToken {

		private final IDocument document;
		private List<Position> positions;
		private ITextSelection selection;
		private boolean canceled;

		public OccurrencesCollector(IDocument document) {
			this.document = document;
			this.positions = new ArrayList<Position>();
		}

		@Override
		public void startCollect() {
			this.positions.clear();
		}

		@Override
		public void endCollect() {
			fOccurrencesFinderJob = new OccurrencesFinderJob(document, positions.toArray(new Position[0]), selection);
			fOccurrencesFinderJob.run(new NullProgressMonitor());
		}

		@Override
		public void addOccurrence(String file, int startLine, int startOffset, int endLine, int endOffset,
				boolean isWriteAccess) throws TypeScriptException {
			try {
				int start = document.getLineOffset(startLine - 1) + startOffset - 1;
				int end = document.getLineOffset(endLine - 1) + endOffset - 1;
				int offset = start;
				int length = end - start;
				positions.add(new Position(offset, length));
			} catch (BadLocationException e) {
				Trace.trace(Trace.SEVERE, "Error while getting TypeScript occurrences.", e);
			}
		}

		public void setSelection(ITextSelection selection) {
			this.selection = selection;
		}

		@Override
		public boolean isCancellationRequested() {
			return canceled;
		}

		@Override
		public void onError(TypeScriptException e) {
			Trace.trace(Trace.SEVERE, "Error while getting TypeScript occurrences.", e);
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
			occurrencesCollector = new OccurrencesCollector(document);
		}
		try {
			ITypeScriptFile tsFile = getTypeScriptFile(document);
			occurrencesCollector.setSelection(selection);
			tsFile.occurrences(selection.getOffset(), occurrencesCollector);
		} catch (Exception e) {
			Trace.trace(Trace.SEVERE, "Error while getting TypeScript occurrences.", e);
		}

	}

	private ITypeScriptFile getTypeScriptFile(IDocument document) throws CoreException, TypeScriptException {
		IResource file = EditorUtils.getResource(this);
		IIDETypeScriptProject tsProject = TypeScriptResourceUtil.getTypeScriptProject(file.getProject());
		return tsProject.openFile(file, document);
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
}
