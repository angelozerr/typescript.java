/*******************************************************************************
 * Copyright (c) 2000, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package ts.eclipse.ide.jsdt.internal.ui.refactoring;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import ts.eclipse.ide.jsdt.internal.ui.util.RowLayouter;

public class RenameInputWizardPage extends TextInputWizardPage {

	private Button findInComments;
	private Button findInStrings;

	public RenameInputWizardPage(String description, boolean isLastUserPage, String initialValue) {
		super(description, isLastUserPage, initialValue);
	}

	@Override
	public void createControl(Composite parent) {
		Composite superComposite = new Composite(parent, SWT.NONE);
		setControl(superComposite);
		initializeDialogUnits(superComposite);
		superComposite.setLayout(new GridLayout());
		Composite composite = new Composite(superComposite, SWT.NONE);
		composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginHeight = 0;
		layout.marginWidth = 0;

		composite.setLayout(layout);
		RowLayouter layouter = new RowLayouter(2);

		Label label = new Label(composite, SWT.NONE);
		label.setText(RefactoringMessages.RenameInputWizardPage_new_name);

		Text text = createTextInputField(composite);
		text.selectAll();
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.widthHint = convertWidthInCharsToPixels(25);
		text.setLayoutData(gd);

		layouter.perform(label, text, 1);

		Label separator = new Label(composite, SWT.NONE);
		GridData gridData = new GridData(SWT.FILL, SWT.FILL, false, false);
		gridData.heightHint = 2;
		separator.setLayoutData(gridData);

		addFindInCommentsCheckbox(composite, layouter);
		addFindInStringsCheckbox(composite, layouter);

		Dialog.applyDialogFont(superComposite);
	}

	/**
	 * Add "Find in comments?" checkbox.
	 * 
	 * @param result
	 * @param layouter
	 */
	private void addFindInCommentsCheckbox(Composite result, RowLayouter layouter) {
		final TypeScriptRenameProcessor ref = (TypeScriptRenameProcessor) getRefactoring()
				.getAdapter(TypeScriptRenameProcessor.class);
		String title = RefactoringMessages.RenameInputWizardPage_findInComments;
		boolean defaultValue = false;
		findInComments = createCheckbox(result, title, defaultValue, layouter);
		ref.setFindInComments(findInComments.getSelection());
		findInComments.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				ref.setFindInComments(findInComments.getSelection());
			}
		});
	}

	/**
	 * Add "Find in strings?" checkbox.
	 * 
	 * @param result
	 * @param layouter
	 */
	private void addFindInStringsCheckbox(Composite result, RowLayouter layouter) {
		final TypeScriptRenameProcessor ref = (TypeScriptRenameProcessor) getRefactoring()
				.getAdapter(TypeScriptRenameProcessor.class);
		String title = RefactoringMessages.RenameInputWizardPage_findInStrings;
		boolean defaultValue = false;
		findInStrings = createCheckbox(result, title, defaultValue, layouter);
		ref.setFindInStrings(findInStrings.getSelection());
		findInStrings.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				ref.setFindInStrings(findInStrings.getSelection());
			}
		});
	}

	private static Button createCheckbox(Composite parent, String title, boolean value, RowLayouter layouter) {
		Button checkBox = new Button(parent, SWT.CHECK);
		checkBox.setText(title);
		checkBox.setSelection(value);
		layouter.perform(checkBox);
		return checkBox;
	}

}
