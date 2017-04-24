package ts.eclipse.ide.jsdt.internal.ui.refactoring;

import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.ui.refactoring.RefactoringWizard;

import ts.eclipse.ide.jsdt.internal.ui.JSDTTypeScriptUIPlugin;

public class RenameRefactoringWizard extends RefactoringWizard {

	public RenameRefactoringWizard(Refactoring refactoring) {
		super(refactoring, DIALOG_BASED_USER_INTERFACE);

		// this.setWindowTitle(Resources.BUNDLE.getString("wizard.refactoring.window.title"));
	}

	@Override
	protected void addUserInputPages() {
		String initialSetting= getNameUpdating().getOldName();
		RenameInputWizardPage inputPage= createInputPage("TODO desc", initialSetting);
		//inputPage.setImageDescriptor(fInputPageImageDescriptor);
		addPage(inputPage);
	}

	private TypeScriptRenameProcessor getNameUpdating() {
		return (TypeScriptRenameProcessor)getRefactoring().getAdapter(TypeScriptRenameProcessor.class);	
	}
	
	protected RenameInputWizardPage createInputPage(String message, String initialSetting) {
		return new RenameInputWizardPage(message, true, initialSetting) {
			protected RefactoringStatus validateTextField(String text) {
				return validateNewName(text);
			}	
		};
	}
	
	protected RefactoringStatus validateNewName(String newName) {
		TypeScriptRenameProcessor ref= getNameUpdating();
		ref.setNewName(newName);
		try{
			return null;//ref.checkNewElementName(newName);
		} catch (Exception e){
			JSDTTypeScriptUIPlugin.log(e);
			return RefactoringStatus.createFatalErrorStatus(RefactoringMessages.RenameRefactoringWizard_internal_error);
		}	
	}
}
