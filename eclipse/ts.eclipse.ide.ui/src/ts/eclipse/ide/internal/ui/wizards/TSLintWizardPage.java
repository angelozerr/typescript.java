package ts.eclipse.ide.internal.ui.wizards;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ControlEnableState;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
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
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;

import ts.eclipse.ide.core.TypeScriptCorePlugin;
import ts.eclipse.ide.internal.ui.TypeScriptUIMessages;
import ts.eclipse.ide.ui.widgets.NPMInstallWidget;
import ts.eclipse.ide.ui.wizards.AbstractWizardPage;
import ts.repository.ITypeScriptRepository;

public class TSLintWizardPage extends AbstractWizardPage {

	private static final String PAGE_NAME = "TSLintWizardPage";

	private ControlEnableState fBlockEnableState;

	// tslint Runtime
	private Button useEmbeddedTslintRuntimeButton;
	private boolean useEmbeddedTslintRuntime;
	private Combo embeddedTslintRuntime;
	private NPMInstallWidget installTslintRuntime;

	private Button enableTslint;

	private Composite controlsComposite;

	protected TSLintWizardPage() {
		super(PAGE_NAME, TypeScriptUIMessages.TSLintWizardPage_title, null);
		super.setDescription(TypeScriptUIMessages.TSLintWizardPage_description);
	}

	@Override
	protected void createBody(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));

		enableTslint = new Button(composite, SWT.CHECK);
		enableTslint.setText(TypeScriptUIMessages.TSLintWizardPage_enableTslint_text);
		enableTslint.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				enableTslintContent(enableTslint.getSelection());
			}
		});
		
		controlsComposite = new Composite(composite, SWT.NONE);
		controlsComposite.setFont(composite.getFont());
		controlsComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.numColumns = 1;
		controlsComposite.setLayout(layout);
		createTslintRuntimeBody(controlsComposite);

	}

	// ------------------- TypeScript Runtime content

	private void createTslintRuntimeBody(Composite parent) {
		Group group = new Group(parent, SWT.NONE);
		group.setFont(parent.getFont());
		group.setText(TypeScriptUIMessages.TSLintWizardPage_tslint_group_label);
		group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		int nColumns = 2;
		GridLayout layout = new GridLayout();
		layout.numColumns = nColumns;
		group.setLayout(layout);

		// Embedded tslint
		createEmbeddedTypeScriptField(group);
		// Install Typetslint
		createInstallScriptField(group);
	}

	private void createEmbeddedTypeScriptField(Composite parent) {
		useEmbeddedTslintRuntimeButton = new Button(parent, SWT.RADIO);
		useEmbeddedTslintRuntimeButton.setText(TypeScriptUIMessages.TSLintWizardPage_useEmbeddedTslintRuntime_label);
		useEmbeddedTslintRuntimeButton.addListener(SWT.Selection, this);
		useEmbeddedTslintRuntimeButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateTslintRuntimeMode();
			}
		});

		embeddedTslintRuntime = new Combo(parent, SWT.READ_ONLY);
		embeddedTslintRuntime.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		ComboViewer viewer = new ComboViewer(embeddedTslintRuntime);
		viewer.setContentProvider(ArrayContentProvider.getInstance());
		viewer.setLabelProvider(new TypeScriptRepositoryLabelProvider(true));
		List<ITypeScriptRepository> repositories = Arrays
				.stream(TypeScriptCorePlugin.getTypeScriptRepositoryManager().getRepositories())
				.filter(r -> r.getTslintFile() != null).collect(Collectors.toList());
		viewer.setInput(repositories);
	}

	private void createInstallScriptField(Composite parent) {
		Button useInstallTslintRuntime = new Button(parent, SWT.RADIO);
		useInstallTslintRuntime.setText(TypeScriptUIMessages.TSLintWizardPage_useInstallTslintRuntime_label);
		useInstallTslintRuntime.addListener(SWT.Selection, this);
		useInstallTslintRuntime.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateTslintRuntimeMode();
			}
		});
		installTslintRuntime = new NPMInstallWidget("tslint", this, parent, SWT.NONE);
		installTslintRuntime.getVersionText().addListener(SWT.Modify, this);
	}

	private void updateTslintRuntimeMode() {
		useEmbeddedTslintRuntime = useEmbeddedTslintRuntimeButton.getSelection();
		embeddedTslintRuntime.setEnabled(useEmbeddedTslintRuntime);
		installTslintRuntime.setEnabled(!useEmbeddedTslintRuntime);
	}

	private IStatus validateTslintRuntime() {
		if (useEmbeddedTslintRuntimeButton.getSelection()) {
			return Status.OK_STATUS;
		}
		return installTslintRuntime.getStatus();
	}

	@Override
	protected void initializeDefaultValues() {
		// Default values for tslint runtime
		if (embeddedTslintRuntime.getItemCount() > 0) {
			embeddedTslintRuntime.select(0);
		}
		useEmbeddedTslintRuntimeButton.setSelection(true);
		updateTslintRuntimeMode();
		
		// Disable tslint
		enableTslint.setSelection(false);
		enableTslintContent(false);
	}

	@Override
	protected IStatus[] validatePage() {
		IStatus[] status = new IStatus[1];
		status[0] = validateTslintRuntime();
		return status;
	}

	protected void enableTslintContent(boolean enable) {
		if (enable) {
			if (fBlockEnableState != null) {
				fBlockEnableState.restore();
				fBlockEnableState = null;
			}
		} else {
			if (fBlockEnableState == null) {
				fBlockEnableState = ControlEnableState.disable(controlsComposite);
			}
		}
	}
	
	public String getNpmInstallCommand() {
		if (useEmbeddedTslintRuntime) {
			return null;
		}
		return installTslintRuntime.getNpmInstallCommand();
	}
}
