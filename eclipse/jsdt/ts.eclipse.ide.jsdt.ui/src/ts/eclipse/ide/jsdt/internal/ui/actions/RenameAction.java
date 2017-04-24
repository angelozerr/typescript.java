package ts.eclipse.ide.jsdt.internal.ui.actions;

import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.wst.jsdt.ui.actions.SelectionDispatchAction;

import ts.eclipse.ide.jsdt.internal.ui.JSDTTypeScriptUIMessages;
import ts.eclipse.ide.jsdt.internal.ui.editor.TypeScriptEditor;
import ts.eclipse.ide.jsdt.internal.ui.refactoring.RefactoringMessages;

public class RenameAction extends SelectionDispatchAction {

	private RenameTypeScriptElementAction fRenameJavaElement;

	/**
	 * Creates a new <code>RenameAction</code>. The action requires
	 * that the selection provided by the site's selection provider is of type <code>
	 * org.eclipse.jface.viewers.IStructuredSelection</code>.
	 *
	 * @param site the site providing context information for this action
	 */
	public RenameAction(IWorkbenchSite site) {
		super(site);
		setText(RefactoringMessages.RenameAction_text);
		fRenameJavaElement= new RenameTypeScriptElementAction(site);
		fRenameJavaElement.setText(getText());
//		fRenameResource= new RenameResourceAction(site);
//		fRenameResource.setText(getText());
//		PlatformUI.getWorkbench().getHelpSystem().setHelp(this, IJavaHelpContextIds.RENAME_ACTION);
	}

	/**
	 * Note: This constructor is for internal use only. Clients should not call this constructor.
	 * @param editor the Java editor
	 *
	 * @noreference This constructor is not intended to be referenced by clients.
	 */
	public RenameAction(TypeScriptEditor editor) {
		this(editor.getEditorSite());
		fRenameJavaElement= new RenameTypeScriptElementAction(editor);
	}
	
	@Override
	public void selectionChanged(IStructuredSelection event) {
		fRenameJavaElement.selectionChanged(event);
//		if (fRenameResource != null)
//			fRenameResource.selectionChanged(event);
		setEnabled(computeEnabledState());
	}
	
	@Override
	public void update(ISelection selection) {
		fRenameJavaElement.update(selection);

		// if (fRenameResource != null)
		// fRenameResource.update(selection);

		setEnabled(computeEnabledState());
	}
	
	private boolean computeEnabledState(){
		//if (fRenameResource != null) {
		//	return fRenameJavaElement.isEnabled() || fRenameResource.isEnabled();
		//} else {
			return fRenameJavaElement.isEnabled();
		//}
	}

	@Override
	public void run(IStructuredSelection selection) {
		if (fRenameJavaElement.isEnabled())
			fRenameJavaElement.run(selection);
//		if (fRenameResource != null && fRenameResource.isEnabled())
//			fRenameResource.run(selection);
	}

	@Override
	public void run(ITextSelection selection) {
		if (fRenameJavaElement.isEnabled())
			fRenameJavaElement.run(selection);
	}
}
