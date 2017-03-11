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
package ts.eclipse.ide.ui.utils;

import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import ts.eclipse.ide.internal.ui.TypeScriptUIMessages;
import ts.eclipse.ide.internal.ui.dialogs.NPMModuleVersionsSelectionDialog;

/**
 * NPM install widget provides :
 * 
 * <ul>
 * <li>a text field to fill a NPM module version</li>
 * <li>a "Browse..." button to search available versions for the given node
 * module name.</li>
 * </ul>
 *
 */
public class NPMInstallWidget extends Composite {

	private final String moduleName;
	private Text versionText;
	private Button searchButton;

	public NPMInstallWidget(String moduleName, Composite parent, int style) {
		super(parent, style);
		this.moduleName = moduleName;
		createUI(this);
	}

	private void createUI(Composite parent) {
		this.setLayout(new GridLayout());
		Composite body = new Composite(parent, SWT.NONE);
		body.setLayout(new GridLayout(3, false));
		body.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Label versionLabel = new Label(body, SWT.NONE);
		versionLabel.setText(moduleName + "@");

		versionText = new Text(body, SWT.SINGLE | SWT.BORDER);
		versionText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		searchButton = new Button(body, SWT.PUSH);
		searchButton.setText(TypeScriptUIMessages.Browse);
		searchButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				NPMModuleVersionsSelectionDialog dialog = new NPMModuleVersionsSelectionDialog(moduleName,
						searchButton.getShell(), false);
				if (dialog.open() == Window.OK) {
					String version = (String) dialog.getFirstResult();
					versionText.setText(version);
				}
			}
		});
	}

	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		versionText.setEnabled(enabled);
		searchButton.setEnabled(enabled);
	}
}
