/*******************************************************************************
 * Copyright (c) 2005, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package ts.eclipse.ide.jsdt.internal.ui.refactoring;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.eclipse.core.commands.operations.IOperationHistory;
import org.eclipse.core.commands.operations.IUndoContext;
import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.commands.operations.OperationHistoryFactory;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IEditingSupport;
import org.eclipse.jface.text.IEditingSupportRegistry;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.ITextViewerExtension6;
import org.eclipse.jface.text.IUndoManager;
import org.eclipse.jface.text.IUndoManagerExtension;
import org.eclipse.jface.text.link.ILinkedModeListener;
import org.eclipse.jface.text.link.LinkedModeModel;
import org.eclipse.jface.text.link.LinkedModeUI;
import org.eclipse.jface.text.link.LinkedModeUI.ExitFlags;
import org.eclipse.jface.text.link.LinkedPosition;
import org.eclipse.jface.text.link.LinkedPositionGroup;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.texteditor.link.EditorLinkedModeUI;
import org.eclipse.wst.jsdt.internal.ui.refactoring.reorg.ReorgMessages;

import ts.client.occurrences.OccurrencesResponseItem;
import ts.eclipse.ide.jsdt.internal.ui.JSDTTypeScriptUIPlugin;
import ts.eclipse.ide.jsdt.internal.ui.editor.EditorHighlightingSynchronizer;
import ts.eclipse.ide.jsdt.internal.ui.editor.TypeScriptEditor;
import ts.eclipse.ide.jsdt.internal.ui.text.correction.proposals.LinkedNamesAssistProposal.DeleteBlockingExitPolicy;
import ts.resources.ITypeScriptFile;

//import org.eclipse.jdt.core.ICompilationUnit;
//import org.eclipse.jdt.core.IField;
//import org.eclipse.jdt.core.IJavaElement;
//import org.eclipse.jdt.core.IMethod;
//import org.eclipse.jdt.core.JavaModelException;
//import org.eclipse.jdt.core.dom.ASTNode;
//import org.eclipse.jdt.core.dom.CompilationUnit;
//import org.eclipse.jdt.core.dom.NodeFinder;
//import org.eclipse.jdt.core.dom.SimpleName;
//import org.eclipse.jdt.core.refactoring.IJavaRefactorings;
//import org.eclipse.jdt.core.refactoring.descriptors.RenameJavaElementDescriptor;
//
//import org.eclipse.jdt.internal.corext.dom.LinkedNodeFinder;
//import org.eclipse.jdt.internal.corext.refactoring.rename.RenamingNameSuggestor;
//import org.eclipse.jdt.internal.corext.util.JavaConventionsUtil;
//import org.eclipse.jdt.internal.corext.util.JavaModelUtil;
//
//import org.eclipse.jdt.ui.SharedASTProvider;
//import org.eclipse.jdt.ui.refactoring.RenameSupport;
//
//import org.eclipse.jdt.internal.ui.JSDTTypeScriptUIPlugin;
//import org.eclipse.jdt.internal.ui.javaeditor.CompilationUnitEditor;
//import org.eclipse.jdt.internal.ui.javaeditor.EditorHighlightingSynchronizer;
//import org.eclipse.jdt.internal.ui.javaeditor.EditorUtility;
//import org.eclipse.jdt.internal.ui.refactoring.DelegateUIHelper;
//import org.eclipse.jdt.internal.ui.text.correction.proposals.LinkedNamesAssistProposal.DeleteBlockingExitPolicy;

public class RenameLinkedMode {

	private class FocusEditingSupport implements IEditingSupport {
		@Override
		public boolean ownsFocusShell() {
			if (fInfoPopup == null)
				return false;
			if (fInfoPopup.ownsFocusShell()) {
				return true;
			}

			Shell editorShell = fEditor.getSite().getShell();
			Shell activeShell = editorShell.getDisplay().getActiveShell();
			if (editorShell == activeShell)
				return true;
			return false;
		}

		@Override
		public boolean isOriginator(DocumentEvent event, IRegion subjectRegion) {
			return false; // leave on external modification outside positions
		}
	}

	private class EditorSynchronizer implements ILinkedModeListener {
		@Override
		public void left(LinkedModeModel model, int flags) {
			linkedModeLeft();
			if ((flags & ILinkedModeListener.UPDATE_CARET) != 0) {
				doRename(fShowPreview);
			}
		}

		@Override
		public void resume(LinkedModeModel model, int flags) {
		}

		@Override
		public void suspend(LinkedModeModel model) {
		}
	}

	private class ExitPolicy extends DeleteBlockingExitPolicy {
		public ExitPolicy(IDocument document) {
			super(document);
		}

		@Override
		public ExitFlags doExit(LinkedModeModel model, VerifyEvent event, int offset, int length) {
			fShowPreview = (event.stateMask & SWT.CTRL) != 0
					&& (event.character == SWT.CR || event.character == SWT.LF);
			return super.doExit(model, event, offset, length);
		}
	}

	private static RenameLinkedMode fgActiveLinkedMode;

	private final TypeScriptEditor fEditor;
	private final ITextSelection selection;

	private RenameInformationPopup fInfoPopup;

	private Point fOriginalSelection;
	private String fOriginalName;

	private LinkedPosition fNamePosition;
	private LinkedModeModel fLinkedModeModel;
	private LinkedPositionGroup fLinkedPositionGroup;
	private final FocusEditingSupport fFocusEditingSupport;
	private boolean fShowPreview;

	/**
	 * The operation on top of the undo stack when the rename is
	 * {@link #start()}ed, or <code>null</code> if rename has not been started
	 * or the undo stack was empty.
	 * 
	 * @since 3.5
	 */
	private IUndoableOperation fStartingUndoOperation;

	public RenameLinkedMode(ITextSelection selection,
			/* IJavaElement element, */ TypeScriptEditor editor) {
		Assert.isNotNull(selection);
		Assert.isNotNull(editor);
		fEditor = editor;
		this.selection = selection;
		fFocusEditingSupport = new FocusEditingSupport();
	}

	public static RenameLinkedMode getActiveLinkedMode() {
		if (fgActiveLinkedMode != null) {
			ISourceViewer viewer = fgActiveLinkedMode.fEditor.getViewer();
			if (viewer != null) {
				StyledText textWidget = viewer.getTextWidget();
				if (textWidget != null && !textWidget.isDisposed()) {
					return fgActiveLinkedMode;
				}
			}
			// make sure we don't hold onto the active linked mode if anything
			// went wrong with canceling:
			fgActiveLinkedMode = null;
		}
		return null;
	}

	public void start() {
		if (getActiveLinkedMode() != null) {
			// for safety; should already be handled in RenameJavaElementAction
			fgActiveLinkedMode.startFullDialog();
			return;
		}

		ISourceViewer viewer = fEditor.getViewer();
		IDocument document = viewer.getDocument();
		fOriginalSelection = viewer.getSelectedRange();
		int offset = fOriginalSelection.x;

		try {
			fLinkedPositionGroup = new LinkedPositionGroup();
			if (viewer instanceof ITextViewerExtension6) {
				IUndoManager undoManager = ((ITextViewerExtension6) viewer).getUndoManager();
				if (undoManager instanceof IUndoManagerExtension) {
					IUndoManagerExtension undoManagerExtension = (IUndoManagerExtension) undoManager;
					IUndoContext undoContext = undoManagerExtension.getUndoContext();
					IOperationHistory operationHistory = OperationHistoryFactory.getOperationHistory();
					fStartingUndoOperation = operationHistory.getUndoOperation(undoContext);
				}
			}

			// Find occurrences
			ITypeScriptFile tsFile = fEditor.getTypeScriptFile();
			List<OccurrencesResponseItem> occurrences = tsFile.occurrences(offset).get(1000, TimeUnit.MILLISECONDS);

			// Create Eclipse linked position from the occurrences list.
			int start, length;
			for (int i = 0; i < occurrences.size(); i++) {
				OccurrencesResponseItem item = occurrences.get(i);
				start = tsFile.getPosition(item.getStart());
				length = tsFile.getPosition(item.getEnd()) - start;
				LinkedPosition linkedPosition = new LinkedPosition(document, start, length, i);
				if (i == 0) {
					fOriginalName = document.get(start, length);
					fNamePosition = linkedPosition;
				}
				fLinkedPositionGroup.addPosition(linkedPosition);
			}

			fLinkedModeModel = new LinkedModeModel();
			fLinkedModeModel.addGroup(fLinkedPositionGroup);
			fLinkedModeModel.forceInstall();
			fLinkedModeModel.addLinkingListener(new EditorHighlightingSynchronizer(fEditor));
			fLinkedModeModel.addLinkingListener(new EditorSynchronizer());

			LinkedModeUI ui = new EditorLinkedModeUI(fLinkedModeModel, viewer);
			ui.setExitPosition(viewer, offset, 0, Integer.MAX_VALUE);
			ui.setExitPolicy(new ExitPolicy(document));
			ui.enter();

			viewer.setSelectedRange(fOriginalSelection.x, fOriginalSelection.y); // by
																					// default,
																					// full
																					// word
																					// is
																					// selected;
																					// restore
																					// original
																					// selection

			if (viewer instanceof IEditingSupportRegistry) {
				IEditingSupportRegistry registry = (IEditingSupportRegistry) viewer;
				registry.register(fFocusEditingSupport);
			}

			openSecondaryPopup();
			// startAnimation();
			fgActiveLinkedMode = this;

		} catch (Exception e) {
			JSDTTypeScriptUIPlugin.log(e);
		}
	}

	// private void startAnimation() {
	// //TODO:
	// // - switch off if animations disabled
	// // - show rectangle around target for 500ms after animation
	// Shell shell= fEditor.getSite().getShell();
	// StyledText textWidget= fEditor.getViewer().getTextWidget();
	//
	// // from popup:
	// Rectangle startRect= fPopup.getBounds();
	//
	// // from editor:
	//// Point startLoc=
	// textWidget.getParent().toDisplay(textWidget.getLocation());
	//// Point startSize= textWidget.getSize();
	//// Rectangle startRect= new Rectangle(startLoc.x, startLoc.y, startSize.x,
	// startSize.y);
	//
	// // from hell:
	//// Rectangle startRect= shell.getClientArea();
	//
	// Point caretLocation=
	// textWidget.getLocationAtOffset(textWidget.getCaretOffset());
	// Point displayLocation= textWidget.toDisplay(caretLocation);
	// Rectangle targetRect= new Rectangle(displayLocation.x, displayLocation.y,
	// 0, 0);
	//
	// RectangleAnimation anim= new RectangleAnimation(shell, startRect,
	// targetRect);
	// anim.schedule();
	// }

	void doRename(boolean showPreview) {
		cancel();

		Image image = null;
		Label label = null;

		fShowPreview |= showPreview;
		try {
			ISourceViewer viewer = fEditor.getViewer();
			if (viewer instanceof SourceViewer) {
				SourceViewer sourceViewer = (SourceViewer) viewer;
				Control viewerControl = sourceViewer.getControl();
				if (viewerControl instanceof Composite) {
					Composite composite = (Composite) viewerControl;
					Display display = composite.getDisplay();

					// Flush pending redraw requests:
					while (!display.isDisposed() && display.readAndDispatch()) {
					}

					// Copy editor area:
					GC gc = new GC(composite);
					Point size;
					try {
						size = composite.getSize();
						image = new Image(gc.getDevice(), size.x, size.y);
						gc.copyArea(image, 0, 0);
					} finally {
						gc.dispose();
						gc = null;
					}

					// Persist editor area while executing refactoring:
					label = new Label(composite, SWT.NONE);
					label.setImage(image);
					label.setBounds(0, 0, size.x, size.y);
					label.moveAbove(null);
				}
			}

			String newName = fNamePosition.getContent();
			if (fOriginalName.equals(newName))
				return;

			RenameSupport renameSupport = undoAndCreateRenameSupport(newName);
			if (renameSupport == null)
				return;

			Shell shell = fEditor.getSite().getShell();
			boolean executed;
			if (fShowPreview) { // could have been updated by
								// undoAndCreateRenameSupport(..)
				executed = renameSupport.openDialog(shell, true);
			} else {
				renameSupport.perform(shell, fEditor.getSite().getWorkbenchWindow());
				executed = true;
			}
			if (executed) {
				restoreFullSelection();
			}
			// JavaModelUtil.reconcile(getCompilationUnit());
		} catch (CoreException ex) {
			JSDTTypeScriptUIPlugin.log(ex);
		} catch (InterruptedException ex) {
			// canceling is OK -> redo text changes in that case?
		} catch (InvocationTargetException ex) {
			JSDTTypeScriptUIPlugin.log(ex);
		} catch (BadLocationException e) {
			JSDTTypeScriptUIPlugin.log(e);
		} finally {
			if (label != null)
				label.dispose();
			if (image != null)
				image.dispose();
		}
	}

	public void cancel() {
		if (fLinkedModeModel != null) {
			fLinkedModeModel.exit(ILinkedModeListener.NONE);
		}
		linkedModeLeft();
	}

	private void restoreFullSelection() {
		if (fOriginalSelection.y != 0) {
			int originalOffset = fOriginalSelection.x;
			LinkedPosition[] positions = fLinkedPositionGroup.getPositions();
			for (int i = 0; i < positions.length; i++) {
				LinkedPosition position = positions[i];
				if (!position.isDeleted() && position.includes(originalOffset)) {
					fEditor.getViewer().setSelectedRange(position.offset, position.length);
					return;
				}
			}
		}
	}

	private RenameSupport undoAndCreateRenameSupport(String newName) throws CoreException {
		// Assumption: the linked mode model should be shutdown by now.
		final ISourceViewer viewer = fEditor.getViewer();
		try {
			if (!fOriginalName.equals(newName)) {
				fEditor.getSite().getWorkbenchWindow().run(false, true, new IRunnableWithProgress() {

					@Override
					public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
						if (viewer instanceof ITextViewerExtension6) {
							IUndoManager undoManager = ((ITextViewerExtension6) viewer).getUndoManager();
							if (undoManager instanceof IUndoManagerExtension) {
								IUndoManagerExtension undoManagerExtension = (IUndoManagerExtension) undoManager;
								IUndoContext undoContext = undoManagerExtension.getUndoContext();
								IOperationHistory operationHistory = OperationHistoryFactory.getOperationHistory();
								while (undoManager.undoable()) {
									if (fStartingUndoOperation != null && fStartingUndoOperation
											.equals(operationHistory.getUndoOperation(undoContext)))
										return;
									undoManager.undo();
								}
							}
						}
					}
				});
			}
		} catch (InvocationTargetException e) {
			throw new CoreException(new Status(IStatus.ERROR, JSDTTypeScriptUIPlugin.PLUGIN_ID,
					ReorgMessages.RenameLinkedMode_error_saving_editor, e));
		} catch (InterruptedException e) { // canceling is OK return null; }
											// finally {
			// JavaModelUtil.reconcile(getCompilationUnit());
		}

		viewer.setSelectedRange(fOriginalSelection.x, fOriginalSelection.y);

		if (newName.length() == 0)
			return null;

		// RenameJavaElementDescriptor descriptor =
		// createRenameDescriptor(fJavaElement, newName);
		RenameSupport renameSupport = RenameSupport.create(fEditor.getTypeScriptFile(), selection.getOffset(), newName);
		return renameSupport;
	}

	// private ICompilationUnit getCompilationUnit() {
	// return (ICompilationUnit)
	// EditorUtility.getEditorInputJavaElement(fEditor, false);
	// }

	public void startFullDialog() {
		cancel();

		try {
			String newName = fNamePosition.getContent();
			RenameSupport renameSupport = undoAndCreateRenameSupport(newName);
			if (renameSupport != null)
				renameSupport.openDialog(fEditor.getSite().getShell());
		} catch (CoreException e) {
			JSDTTypeScriptUIPlugin.log(e);
		} catch (BadLocationException e) {
			JSDTTypeScriptUIPlugin.log(e);
		}
	}

	/**
	 * Creates a rename descriptor.
	 * 
	 * @param javaElement
	 *            element to rename
	 * @param newName
	 *            new name
	 * @return a rename descriptor with current settings as used in the
	 *         refactoring dialogs
	 * @throws JavaModelException
	 *             if an error occurs while accessing the element
	 */
	// private RenameJavaElementDescriptor createRenameDescriptor(IJavaElement
	// javaElement, String newName) throws JavaModelException {
	// String contributionId;
	// // see RefactoringExecutionStarter#createRenameSupport(..):
	// int elementType= javaElement.getElementType();
	// switch (elementType) {
	// case IJavaElement.JAVA_PROJECT:
	// contributionId= IJavaRefactorings.RENAME_JAVA_PROJECT;
	// break;
	// case IJavaElement.PACKAGE_FRAGMENT_ROOT:
	// contributionId= IJavaRefactorings.RENAME_SOURCE_FOLDER;
	// break;
	// case IJavaElement.PACKAGE_FRAGMENT:
	// contributionId= IJavaRefactorings.RENAME_PACKAGE;
	// break;
	// case IJavaElement.COMPILATION_UNIT:
	// contributionId= IJavaRefactorings.RENAME_COMPILATION_UNIT;
	// break;
	// case IJavaElement.TYPE:
	// contributionId= IJavaRefactorings.RENAME_TYPE;
	// break;
	// case IJavaElement.METHOD:
	// final IMethod method= (IMethod) javaElement;
	// if (method.isConstructor())
	// return createRenameDescriptor(method.getDeclaringType(), newName);
	// else
	// contributionId= IJavaRefactorings.RENAME_METHOD;
	// break;
	// case IJavaElement.FIELD:
	// IField field= (IField) javaElement;
	// if (field.isEnumConstant())
	// contributionId= IJavaRefactorings.RENAME_ENUM_CONSTANT;
	// else
	// contributionId= IJavaRefactorings.RENAME_FIELD;
	// break;
	// case IJavaElement.TYPE_PARAMETER:
	// contributionId= IJavaRefactorings.RENAME_TYPE_PARAMETER;
	// break;
	// case IJavaElement.LOCAL_VARIABLE:
	// contributionId= IJavaRefactorings.RENAME_LOCAL_VARIABLE;
	// break;
	// default:
	// return null;
	// }
	//
	// RenameJavaElementDescriptor descriptor= (RenameJavaElementDescriptor)
	// RefactoringCore.getRefactoringContribution(contributionId).createDescriptor();
	// descriptor.setJavaElement(javaElement);
	// descriptor.setNewName(newName);
	// if (elementType != IJavaElement.PACKAGE_FRAGMENT_ROOT)
	// descriptor.setUpdateReferences(true);
	//
	// IDialogSettings javaSettings=
	// JSDTTypeScriptUIPlugin.getDefault().getDialogSettings();
	// IDialogSettings refactoringSettings=
	// javaSettings.getSection(RefactoringWizardPage.REFACTORING_SETTINGS);
	// //TODO: undocumented API
	// if (refactoringSettings == null) {
	// refactoringSettings=
	// javaSettings.addNewSection(RefactoringWizardPage.REFACTORING_SETTINGS);
	// }
	//
	// switch (elementType) {
	// case IJavaElement.METHOD:
	// case IJavaElement.FIELD:
	// descriptor.setDeprecateDelegate(refactoringSettings.getBoolean(DelegateUIHelper.DELEGATE_DEPRECATION));
	// descriptor.setKeepOriginal(refactoringSettings.getBoolean(DelegateUIHelper.DELEGATE_UPDATING));
	// }
	// switch (elementType) {
	// case IJavaElement.TYPE:
	//// case IJavaElement.COMPILATION_UNIT: // TODO
	// descriptor.setUpdateSimilarDeclarations(refactoringSettings.getBoolean(RenameRefactoringWizard.TYPE_UPDATE_SIMILAR_ELEMENTS));
	// int strategy;
	// try {
	// strategy=
	// refactoringSettings.getInt(RenameRefactoringWizard.TYPE_SIMILAR_MATCH_STRATEGY);
	// } catch (NumberFormatException e) {
	// strategy= RenamingNameSuggestor.STRATEGY_EXACT;
	// }
	// descriptor.setMatchStrategy(strategy);
	// }
	// switch (elementType) {
	// case IJavaElement.PACKAGE_FRAGMENT:
	// descriptor.setUpdateHierarchy(refactoringSettings.getBoolean(RenameRefactoringWizard.PACKAGE_RENAME_SUBPACKAGES));
	// }
	// switch (elementType) {
	// case IJavaElement.PACKAGE_FRAGMENT:
	// case IJavaElement.TYPE:
	// String fileNamePatterns=
	// refactoringSettings.get(RenameRefactoringWizard.QUALIFIED_NAMES_PATTERNS);
	// if (fileNamePatterns != null && fileNamePatterns.length() != 0) {
	// descriptor.setFileNamePatterns(fileNamePatterns);
	// boolean updateQualifiedNames=
	// refactoringSettings.getBoolean(RenameRefactoringWizard.UPDATE_QUALIFIED_NAMES);
	// descriptor.setUpdateQualifiedNames(updateQualifiedNames);
	// fShowPreview|= updateQualifiedNames;
	// }
	// }
	// switch (elementType) {
	// case IJavaElement.PACKAGE_FRAGMENT:
	// case IJavaElement.TYPE:
	// case IJavaElement.FIELD:
	// boolean updateTextualOccurrences=
	// refactoringSettings.getBoolean(RenameRefactoringWizard.UPDATE_TEXTUAL_MATCHES);
	// descriptor.setUpdateTextualOccurrences(updateTextualOccurrences);
	// fShowPreview|= updateTextualOccurrences;
	// }
	// switch (elementType) {
	// case IJavaElement.FIELD:
	// descriptor.setRenameGetters(refactoringSettings.getBoolean(RenameRefactoringWizard.FIELD_RENAME_GETTER));
	// descriptor.setRenameSetters(refactoringSettings.getBoolean(RenameRefactoringWizard.FIELD_RENAME_SETTER));
	// }
	// return descriptor;
	// }

	private void linkedModeLeft() {
		fgActiveLinkedMode = null;
		if (fInfoPopup != null) {
			fInfoPopup.close();
		}

		ISourceViewer viewer = fEditor.getViewer();
		if (viewer instanceof IEditingSupportRegistry) {
			IEditingSupportRegistry registry = (IEditingSupportRegistry) viewer;
			registry.unregister(fFocusEditingSupport);
		}
	}

	private void openSecondaryPopup() {
		fInfoPopup = new RenameInformationPopup(fEditor, this);
		fInfoPopup.open();
	}

	public boolean isCaretInLinkedPosition() {
		return getCurrentLinkedPosition() != null;
	}

	public LinkedPosition getCurrentLinkedPosition() {
		Point selection = fEditor.getViewer().getSelectedRange();
		int start = selection.x;
		int end = start + selection.y;
		LinkedPosition[] positions = fLinkedPositionGroup.getPositions();
		for (int i = 0; i < positions.length; i++) {
			LinkedPosition position = positions[i];
			if (position.includes(start) && position.includes(end))
				return position;
		}
		return null;
	}

	public boolean isEnabled() {
		try {
			String newName = fNamePosition.getContent();
			if (fOriginalName.equals(newName))
				return false;
			/*
			 * TODO: use JavaRenameProcessor#checkNewElementName(String) but
			 * make sure implementations don't access outdated Java Model (cache
			 * all necessary information before starting linked mode).
			 */
			return true; // JavaConventionsUtil.validateIdentifier(newName,
							// fJavaElement).isOK();
		} catch (BadLocationException e) {
			return false;
		}

	}

	public boolean isOriginalName() {
		try {
			String newName = fNamePosition.getContent();
			return fOriginalName.equals(newName);
		} catch (BadLocationException e) {
			return false;
		}
	}

}
