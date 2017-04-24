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
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class RenameInputWizardPage extends TextInputWizardPage {

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
		// RowLayouter layouter = new RowLayouter(2);

		Label label = new Label(composite, SWT.NONE);
		label.setText(RefactoringMessages.RenameInputWizardPage_new_name);

		Text text = createTextInputField(composite);
		text.selectAll();
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.widthHint = convertWidthInCharsToPixels(25);
		text.setLayoutData(gd);

		// layouter.perform(label, text, 1);

		Label separator = new Label(composite, SWT.NONE);
		GridData gridData = new GridData(SWT.FILL, SWT.FILL, false, false);
		gridData.heightHint = 2;
		separator.setLayoutData(gridData);

		Dialog.applyDialogFont(superComposite);
	}

}
