package ts.eclipse.ide.jsdt.internal.ui.actions;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.wst.jsdt.internal.ui.JavaScriptPlugin;
import org.eclipse.wst.jsdt.ui.PreferenceConstants;
import org.eclipse.wst.jsdt.ui.actions.SelectionDispatchAction;

import ts.eclipse.ide.jsdt.internal.ui.editor.TypeScriptEditor;
import ts.eclipse.ide.jsdt.internal.ui.refactoring.RenameLinkedMode;

public class RenameTypeScriptElementAction extends SelectionDispatchAction {

	private TypeScriptEditor fEditor;

	protected RenameTypeScriptElementAction(IWorkbenchSite site) {
		super(site);
	}

	protected RenameTypeScriptElementAction(TypeScriptEditor editor) {
		this(editor.getEditorSite());
		fEditor = editor;
		// setEnabled(SelectionConverter.canOperateOn(fEditor));
		setEnabled(true);
	}

	@Override
	public void selectionChanged(IStructuredSelection selection) {
		if (selection.size() == 1) {
			// setEnabled(canEnable(selection));
			setEnabled(true);
			return;
		}
		setEnabled(false);
	}

	@Override
	public void selectionChanged(ITextSelection selection) {
		//if (selection.size() == 1) {
			// setEnabled(canEnable(selection));
			setEnabled(true);
			//return;
		//}
		//setEnabled(false);
	}

	
	@Override
	public void run(ITextSelection selection) {
		if (canRunInEditor()) {
			doRun(selection);
		}
	}
	
	private boolean canRunInEditor() {
		if (RenameLinkedMode.getActiveLinkedMode() != null)
			return true;
		return true;
	}
	
	private void doRun(ITextSelection selection) {
		RenameLinkedMode activeLinkedMode= RenameLinkedMode.getActiveLinkedMode();
		if (activeLinkedMode != null) {
			if (activeLinkedMode.isCaretInLinkedPosition()) {
				activeLinkedMode.startFullDialog();
				return;
			} else {
				activeLinkedMode.cancel();
			}
		}

		IPreferenceStore store= JavaScriptPlugin.getDefault().getPreferenceStore();
		boolean lightweight= store.getBoolean(PreferenceConstants.REFACTOR_LIGHTWEIGHT);
		run(selection, lightweight);
		
//		try {
//			IJavaElement element= getJavaElementFromEditor();
//			IPreferenceStore store= JavaPlugin.getDefault().getPreferenceStore();
//			boolean lightweight= store.getBoolean(PreferenceConstants.REFACTOR_LIGHTWEIGHT);
//			if (element != null && RefactoringAvailabilityTester.isRenameElementAvailable(element)) {
//				run(element, lightweight);
//				return;
//			} else if (lightweight) {
//				// fall back to local rename:
//				CorrectionCommandHandler handler= new CorrectionCommandHandler(fEditor, LinkedNamesAssistProposal.ASSIST_ID, true);
//				if (handler.doExecute()) {
//					fEditor.setStatusLineErrorMessage(RefactoringMessages.RenameJavaElementAction_started_rename_in_file);
//					return;
//				}
//			}
//		} catch (CoreException e) {
//			ExceptionHandler.handle(e, RefactoringMessages.RenameJavaElementAction_name, RefactoringMessages.RenameJavaElementAction_exception);
//		}
//		MessageDialog.openInformation(getShell(), RefactoringMessages.RenameJavaElementAction_name, RefactoringMessages.RenameJavaElementAction_not_available);
	}

	private void run(ITextSelection selection, boolean lightweight) {
		if (lightweight && fEditor instanceof TypeScriptEditor) {
			new RenameLinkedMode(selection, (TypeScriptEditor) fEditor).start();
		} 
		/*else {
			RefactoringExecutionStarter.startRenameRefactoring(element, getShell());
		}*/
	}


}
