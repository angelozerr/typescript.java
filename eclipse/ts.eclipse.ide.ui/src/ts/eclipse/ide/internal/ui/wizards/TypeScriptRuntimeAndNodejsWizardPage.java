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
package ts.eclipse.ide.internal.ui.wizards;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;

import ts.eclipse.ide.core.TypeScriptCorePlugin;
import ts.eclipse.ide.core.nodejs.IEmbeddedNodejs;
import ts.eclipse.ide.internal.ui.TypeScriptUIMessages;
import ts.eclipse.ide.ui.widgets.NPMInstallWidget;
import ts.eclipse.ide.ui.wizards.AbstractWizardPage;
import ts.repository.ITypeScriptRepository;

public class TypeScriptRuntimeAndNodejsWizardPage extends AbstractWizardPage {

	private static final String PAGE_NAME = "TypeScriptRuntimeAndNodejsWizardPage";

	private Button useEmbeddedTsRuntimeButton;
	private boolean useEmbeddedTsRuntime;
	private NPMInstallWidget installTsRuntime;

	private Combo embeddedTsRuntime;

	protected TypeScriptRuntimeAndNodejsWizardPage() {
		super(PAGE_NAME, TypeScriptUIMessages.TypeScriptRuntimeAndNodejsWizardPage_title, null);
		super.setDescription(TypeScriptUIMessages.TypeScriptRuntimeAndNodejsWizardPage_description);
	}

	@Override
	protected void createBody(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));

		Composite controlsComposite = new Composite(composite, SWT.NONE);
		controlsComposite.setFont(composite.getFont());
		controlsComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.numColumns = 1;
		controlsComposite.setLayout(layout);

		// createNodejsBody(controlsComposite);
		createTypeScriptRuntimeBody(controlsComposite);

	}

	private void createNodejsBody(Composite parent) {
		Group group = new Group(parent, SWT.NONE);
		group.setFont(parent.getFont());
		group.setText(TypeScriptUIMessages.TypeScriptRuntimeAndNodejsWizardPage_nodejs_group_label);
		group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		int nColumns = 2;
		GridLayout layout = new GridLayout();
		layout.numColumns = nColumns;
		group.setLayout(layout);

		// Embedded node.js
		createEmbeddedNodejsField(group);
		// Installed node.js
		createInstalledNodejsField(group);
		// Path info.
		createNodePathInfo(group);
	}

	private void createNodePathInfo(Composite composite) {
		// TODO Auto-generated method stub

	}

	private void createInstalledNodejsField(Group group) {
		// TODO Auto-generated method stub

	}

	private void createEmbeddedNodejsField(Composite composite) {
		Combo comboBox = new Combo(composite, SWT.READ_ONLY);

		// Create combo of embedded node.js
		IEmbeddedNodejs[] installs = TypeScriptCorePlugin.getNodejsInstallManager().getNodejsInstalls();
		String[] values = new String[installs.length + 1];
		String[] valueLabels = new String[installs.length + 1];
		values[0] = "";
		valueLabels[0] = TypeScriptUIMessages.ComboBox_none;
		int i = 1;
		for (IEmbeddedNodejs install : installs) {
			values[i] = install.getId();
			valueLabels[i] = install.getName();
			i++;
		}
		comboBox.setItems(valueLabels);
		comboBox.setFont(JFaceResources.getDialogFont());

	}

	private void createTypeScriptRuntimeBody(Composite parent) {
		Group group = new Group(parent, SWT.NONE);
		group.setFont(parent.getFont());
		group.setText(TypeScriptUIMessages.TypeScriptRuntimeAndNodejsWizardPage_typescript_group_label);
		group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		int nColumns = 2;
		GridLayout layout = new GridLayout();
		layout.numColumns = nColumns;
		group.setLayout(layout);

		// Embedded TypeScript
		createEmbeddedTypeScriptField(group);
		// Install TypeScript
		createInstallScriptField(group);

		useEmbeddedTsRuntimeButton.setSelection(true);
		updateTsRuntimeMode();
	}

	private void createEmbeddedTypeScriptField(Composite parent) {
		useEmbeddedTsRuntimeButton = new Button(parent, SWT.RADIO);
		useEmbeddedTsRuntimeButton
				.setText(TypeScriptUIMessages.TypeScriptRuntimeAndNodejsWizardPage_useEmbeddedTsRuntime_label);
		useEmbeddedTsRuntimeButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateTsRuntimeMode();
			}
		});

		embeddedTsRuntime = new Combo(parent, SWT.READ_ONLY);
		embeddedTsRuntime.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		ComboViewer viewer = new ComboViewer(embeddedTsRuntime);
		viewer.setContentProvider(ArrayContentProvider.getInstance());
		viewer.setLabelProvider(new TypeScriptRepositoryLabelProvider());
		ITypeScriptRepository[] repositories = TypeScriptCorePlugin.getTypeScriptRepositoryManager().getRepositories();
		viewer.setInput(repositories);

		embeddedTsRuntime.select(0);
	}

	private void createInstallScriptField(Composite parent) {
		Button useInstallTsRuntime = new Button(parent, SWT.RADIO);
		useInstallTsRuntime
				.setText(TypeScriptUIMessages.TypeScriptRuntimeAndNodejsWizardPage_useInstallTsRuntime_label);
		useInstallTsRuntime.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateTsRuntimeMode();
			}
		});

		installTsRuntime = new NPMInstallWidget("typescript", this, parent, SWT.NONE);
		installTsRuntime.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	}

	private void updateTsRuntimeMode() {
		useEmbeddedTsRuntime = useEmbeddedTsRuntimeButton.getSelection();
		embeddedTsRuntime.setEnabled(useEmbeddedTsRuntime);
		installTsRuntime.setEnabled(!useEmbeddedTsRuntime);
	}

	@Override
	protected void initializeDefaultValues() {
		// TODO Auto-generated method stub

	}

	@Override
	protected boolean validatePage() {
		// TODO Auto-generated method stub
		return false;
	}

	public String getNpmInstallCommand() {
		if (useEmbeddedTsRuntime) {
			return null;
		}
		return installTsRuntime.getNpmInstallCommand();
	}

}
