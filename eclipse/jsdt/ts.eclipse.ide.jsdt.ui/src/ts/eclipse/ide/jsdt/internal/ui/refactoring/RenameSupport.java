/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.ltk.core.refactoring.RefactoringCore;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.RenameProcessor;
import org.eclipse.ltk.core.refactoring.participants.RenameRefactoring;
import org.eclipse.swt.widgets.Shell;

import ts.eclipse.ide.jsdt.internal.ui.JSDTTypeScriptUIPlugin;
import ts.resources.ITypeScriptFile;

public class RenameSupport {

	private RenameRefactoring fRefactoring;
	private RefactoringStatus fPreCheckStatus;

	// private RenameSupport(/*RenameJavaScriptElementDescriptor descriptor*/)
	// throws CoreException {
	// RefactoringStatus refactoringStatus= new RefactoringStatus();
	// fRefactoring= new TypeScriptRenameRefactoring(this);
	// descriptor.createRefactoring(refactoringStatus);
	// if (refactoringStatus.hasFatalError()) {
	// fPreCheckStatus= refactoringStatus;
	// } else {
	// preCheck();
	// refactoringStatus.merge(fPreCheckStatus);
	// fPreCheckStatus= refactoringStatus;
	// }
	// }

	private RenameSupport(TypeScriptRenameProcessor processor, String newName) throws CoreException {
		fRefactoring = new TypeScriptRenameRefactoring(processor);
		// initialize(fRefactoring, newName, flags);
	}

	public static RenameSupport create(ITypeScriptFile tsFile, int offset, String oldName, String newName)
			throws CoreException {
		TypeScriptRenameProcessor processor = new TypeScriptRenameProcessor(tsFile, offset, oldName);
		if (newName != null) {
			processor.setNewName(newName);
		}
		return new RenameSupport(processor, newName);
	}

	/**
	 * Executes some light weight precondition checking. If the returned status
	 * is an error then the refactoring can't be executed at all. However,
	 * returning an OK status doesn't guarantee that the refactoring can be
	 * executed. It may still fail while performing the exhaustive precondition
	 * checking done inside the methods <code>openDialog</code> or
	 * <code>perform</code>.
	 * 
	 * The method is mainly used to determine enable/disablement of actions.
	 * 
	 * @return the result of the light weight precondition checking.
	 * 
	 * @throws CoreException
	 *             if an unexpected exception occurs while performing the
	 *             checking.
	 * 
	 * @see #openDialog(Shell)
	 * @see #perform(Shell, IRunnableContext)
	 */
	public IStatus preCheck() throws CoreException {
		ensureChecked();
		if (fPreCheckStatus.hasFatalError())
			return fPreCheckStatus.getEntryMatchingSeverity(RefactoringStatus.FATAL).toStatus();
		else
			return new Status(IStatus.OK, JSDTTypeScriptUIPlugin.PLUGIN_ID, 0, "", null); //$NON-NLS-1$
	}

	/**
	 * Opens the refactoring dialog for this rename support.
	 *
	 * @param parent
	 *            a shell used as a parent for the refactoring dialog.
	 * @throws CoreException
	 *             if an unexpected exception occurs while opening the dialog.
	 *
	 * @see #openDialog(Shell, boolean)
	 */
	public void openDialog(Shell parent) throws CoreException {
		openDialog(parent, false);
	}

	/**
	 * Opens the refactoring dialog for this rename support.
	 *
	 * <p>
	 * This method has to be called from within the UI thread.
	 * </p>
	 *
	 * @param parent
	 *            a shell used as a parent for the refactoring, preview, or
	 *            error dialog
	 * @param showPreviewOnly
	 *            if <code>true</code>, the dialog skips all user input pages
	 *            and directly shows the preview or error page. Otherwise, shows
	 *            all pages.
	 * @return <code>true</code> if the refactoring has been executed
	 *         successfully, <code>false</code> if it has been canceled or if an
	 *         error has happened during initial conditions checking.
	 *
	 * @throws CoreException
	 *             if an error occurred while executing the operation.
	 *
	 * @see #openDialog(Shell)
	 * @since 3.3
	 */
	public boolean openDialog(Shell parent, boolean showPreviewOnly) throws CoreException {
		ensureChecked();
		if (fPreCheckStatus.hasFatalError()) {
			showInformation(parent, fPreCheckStatus);
			return false;
		}

		UserInterfaceStarter starter = null;
		if (!showPreviewOnly) {
			starter = new RenameUserInterfaceStarter();
			RenameRefactoringWizard wizard = new RenameRefactoringWizard(fRefactoring);
			starter.initialize(wizard);
		} else {
			starter = new RenameUserInterfaceStarter();
			RenameRefactoringWizard wizard = new RenameRefactoringWizard(fRefactoring) {
				@Override
				protected void addUserInputPages() {
					// nothing to add
				}
			};
			wizard.setForcePreviewReview(showPreviewOnly);
			starter.initialize(wizard);
		}
		return starter.activate(fRefactoring, parent, getTypeScriptRenameProcessor().getSaveMode());
	}

	/**
	 * Executes the rename refactoring without showing a dialog to gather
	 * additional user input (for example the new name of the
	 * <tt>IJavaElement</tt>). Only an error dialog is shown (if necessary) to
	 * present the result of the refactoring's full precondition checking.
	 * <p>
	 * The method has to be called from within the UI thread.
	 * </p>
	 *
	 * @param parent
	 *            a shell used as a parent for the error dialog.
	 * @param context
	 *            a {@link IRunnableContext} to execute the operation.
	 *
	 * @throws InterruptedException
	 *             if the operation has been canceled by the user.
	 * @throws InvocationTargetException
	 *             if an error occurred while executing the operation.
	 *
	 * @see #openDialog(Shell)
	 * @see IRunnableContext#run(boolean, boolean,
	 *      org.eclipse.jface.operation.IRunnableWithProgress)
	 */
	public void perform(Shell parent, IRunnableContext context) throws InterruptedException, InvocationTargetException {
		try {
			ensureChecked();
			if (fPreCheckStatus.hasFatalError()) {
				showInformation(parent, fPreCheckStatus);
				return;
			}

			RenameSelectionState state = createSelectionState();

			RefactoringExecutionHelper helper = new RefactoringExecutionHelper(fRefactoring,
					RefactoringCore.getConditionCheckingFailedSeverity(), getTypeScriptRenameProcessor().getSaveMode(),
					parent, context);
			helper.perform(true, true);

			restoreSelectionState(state);
		} catch (CoreException e) {
			throw new InvocationTargetException(e);
		}
	}

	private TypeScriptRenameProcessor getTypeScriptRenameProcessor() {
		return (TypeScriptRenameProcessor) fRefactoring.getProcessor();
	}

	private void ensureChecked() throws CoreException {
		if (fPreCheckStatus == null) {
			if (!fRefactoring.isApplicable()) {
				fPreCheckStatus = RefactoringStatus
						.createFatalErrorStatus(RefactoringMessages.RenameSupport_not_available);
			} else {
				fPreCheckStatus = new RefactoringStatus();
			}
		}
	}

	private void showInformation(Shell parent, RefactoringStatus status) {
		String message = status.getMessageMatchingSeverity(RefactoringStatus.FATAL);
		MessageDialog.openInformation(parent, RefactoringMessages.RenameSupport_dialog_title, message);
	}

	private RenameSelectionState createSelectionState() {
		RenameProcessor processor = (RenameProcessor) fRefactoring.getAdapter(RenameProcessor.class);
		Object[] elements = processor.getElements();
		RenameSelectionState state = null;// elements.length == 1 ? new
											// RenameSelectionState(elements[0])
											// : null;
		return state;
	}

	private void restoreSelectionState(RenameSelectionState state) throws CoreException {
		TypeScriptRenameProcessor nameUpdating = (TypeScriptRenameProcessor) fRefactoring
				.getAdapter(TypeScriptRenameProcessor.class);
		if (nameUpdating != null && state != null) {
			// Object newElement= nameUpdating.getNewElement();
			// if (newElement != null) {
			// state.restore(newElement);
		}
	}
}
