/**
 *  Copyright (c) 2015-2017 Angelo ZERR.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 *
 */
package ts.eclipse.ide.ui.wizards;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import ts.eclipse.ide.internal.ui.dialogs.StatusUtil;
import ts.eclipse.ide.ui.widgets.IStatusChangeListener;

/**
 * Abstract class for wizard page.
 *
 */
public abstract class AbstractWizardPage extends WizardPage implements Listener, IStatusChangeListener {

	protected AbstractWizardPage(String pageName) {
		super(pageName);
	}

	protected AbstractWizardPage(String pageName, String title, ImageDescriptor titleImage) {
		super(pageName, title, titleImage);
	}

	@Override
	public void createControl(Composite parent) {
		initializeDialogUnits(parent);
		// top level group
		Composite topLevel = new Composite(parent, SWT.NONE);
		topLevel.setLayout(new GridLayout());
		topLevel.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_FILL | GridData.HORIZONTAL_ALIGN_FILL));
		topLevel.setFont(parent.getFont());

		// Text tsconfigText = new Text(parent, style)

		createBody(topLevel);

		// initialize page with default values
		initializeDefaultValues();

		validatePage();
		// Show description on opening
		setErrorMessage(null);
		setMessage(null);
		setControl(topLevel);
	}

	@Override
	public void handleEvent(Event event) {
		setPageComplete(validatePage());
	}

	@Override
	public void statusChanged(IStatus status) {
		setPageComplete(!status.matches(IStatus.ERROR));
		StatusUtil.applyToStatusLine(this, status);
	}

	protected abstract void createBody(Composite parent);

	protected abstract void initializeDefaultValues();

	protected abstract boolean validatePage();

}