/**
 *  Copyright (c) 2015-2017 Angelo ZERR.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package ts.eclipse.ide.ui.wizards;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.dialogs.WizardNewProjectCreationPage;
import org.eclipse.ui.wizards.newresource.BasicNewResourceWizard;

/**
 * Main wizard page to create a TypeScript project.
 *
 */
public class WizardNewTypeScriptProjectCreationPage extends WizardNewProjectCreationPage {

	private final BasicNewResourceWizard wizard;

	public WizardNewTypeScriptProjectCreationPage(String pageName, BasicNewResourceWizard wizard) {
		super(pageName);
		this.wizard = wizard;
	}

	@Override
	public final void createControl(Composite parent) {
		super.createControl(parent);
		Composite body = (Composite) getControl();
		createPageBody(body);
		Dialog.applyDialogFont(body);
	}

	/**
	 * Create page body. User can override this method to add new UI fields.
	 * 
	 * @param parent
	 */
	protected void createPageBody(Composite parent) {
		createWorkingSetGroup(parent, wizard.getSelection(), new String[] { "org.eclipse.ui.resourceWorkingSetPage" }); //$NON-NLS-1$
	}

}
