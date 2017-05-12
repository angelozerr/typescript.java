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

import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
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
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.osgi.service.prefs.BackingStoreException;

import ts.eclipse.ide.core.TypeScriptCorePlugin;
import ts.eclipse.ide.core.preferences.TypeScriptCorePreferenceConstants;
import ts.eclipse.ide.internal.ui.TypeScriptUIMessages;
import ts.eclipse.ide.terminal.interpreter.LineCommand;
import ts.eclipse.ide.terminal.interpreter.TerminalCommandAdapter;
import ts.eclipse.ide.ui.widgets.NpmInstallWidget;
import ts.eclipse.ide.ui.wizards.AbstractWizardPage;
import ts.repository.ITypeScriptRepository;

public class TypeScriptRuntimeWizardPage extends AbstractWizardPage {

	private static final String PAGE_NAME = "TypeScriptRuntimeAndNodejsWizardPage";

	// TypeScript Runtime
	private boolean hasEmbeddedTsRuntime;
	private Button useEmbeddedTsRuntimeButton;
	private boolean useEmbeddedTsRuntime;
	private Combo embeddedTsRuntime;
	private NpmInstallWidget installTsRuntime;

	protected TypeScriptRuntimeWizardPage() {
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

		createTypeScriptRuntimeBody(controlsComposite);
	}

	// ------------------- TypeScript Runtime content

	private void createTypeScriptRuntimeBody(Composite parent) {
		Group group = new Group(parent, SWT.NONE);
		group.setFont(parent.getFont());
		group.setText(TypeScriptUIMessages.TypeScriptRuntimeAndNodejsWizardPage_typescript_group_label);
		group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		int nColumns = 2;
		GridLayout layout = new GridLayout();
		layout.numColumns = nColumns;
		group.setLayout(layout);

		ITypeScriptRepository[] repositories = TypeScriptCorePlugin.getTypeScriptRepositoryManager().getRepositories();
		hasEmbeddedTsRuntime = repositories.length > 0;
		if (hasEmbeddedTsRuntime) {
			// Embedded TypeScript
			createEmbeddedTypeScriptField(group, repositories);
		}
		// Install TypeScript
		createInstallScriptField(group);
	}

	private void createEmbeddedTypeScriptField(Composite parent, ITypeScriptRepository[] repositories) {
		useEmbeddedTsRuntimeButton = new Button(parent, SWT.RADIO);
		useEmbeddedTsRuntimeButton
				.setText(TypeScriptUIMessages.TypeScriptRuntimeAndNodejsWizardPage_useEmbeddedTsRuntime_label);
		useEmbeddedTsRuntimeButton.addListener(SWT.Selection, this);
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

		viewer.setInput(repositories);
	}

	private void createInstallScriptField(Composite parent) {
		Button useInstallTsRuntime = new Button(parent, SWT.RADIO);
		useInstallTsRuntime
				.setText(TypeScriptUIMessages.TypeScriptRuntimeAndNodejsWizardPage_useInstallTsRuntime_label);
		useInstallTsRuntime.addListener(SWT.Selection, this);
		useInstallTsRuntime.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateTsRuntimeMode();
			}
		});
		installTsRuntime = new NpmInstallWidget("typescript", this, parent, SWT.NONE);
		installTsRuntime.getVersionText().addListener(SWT.Modify, this);
	}

	private void updateTsRuntimeMode() {
		if (!hasEmbeddedTsRuntime) {
			return;
		}
		useEmbeddedTsRuntime = useEmbeddedTsRuntimeButton.getSelection();
		embeddedTsRuntime.setEnabled(useEmbeddedTsRuntime);
		installTsRuntime.setEnabled(!useEmbeddedTsRuntime);
	}

	private IStatus validateTypeScriptRuntime() {
		if (hasEmbeddedTsRuntime && useEmbeddedTsRuntimeButton.getSelection()) {
			return Status.OK_STATUS;
		}
		return installTsRuntime.getStatus();
	}

	@Override
	protected void initializeDefaultValues() {
		// Default values for TypeScript runtime
		if (hasEmbeddedTsRuntime) {
			embeddedTsRuntime.select(0);
			useEmbeddedTsRuntimeButton.setSelection(true);
		}
		updateTsRuntimeMode();
	}

	@Override
	protected IStatus[] validatePage() {
		IStatus[] status = new IStatus[1];
		// Validate TypeScript Runtime
		status[0] = validateTypeScriptRuntime();
		return status;
	}

	public void updateCommand(List<LineCommand> commands, final IEclipsePreferences preferences) {
		if (!useEmbeddedTsRuntime) {
			// when TypeScript is installed when "npm install typescript"
			// command is terminated, update the project Eclispe preferences
			// to consume this installed TypeScript runtime.
			commands.add(new LineCommand(installTsRuntime.getNpmInstallCommand(), new TerminalCommandAdapter() {
				@Override
				public void onTerminateCommand(LineCommand lineCommand) {

					Display.getDefault().asyncExec(new Runnable() {
						@Override
						public void run() {
							preferences.putBoolean(TypeScriptCorePreferenceConstants.USE_EMBEDDED_TYPESCRIPT, false);
							preferences.put(TypeScriptCorePreferenceConstants.INSTALLED_TYPESCRIPT_PATH,
									"${project_loc:node_modules/typescript}");
							try {
								preferences.flush();
							} catch (BackingStoreException e) {
								e.printStackTrace();
							}
						}
					});

				}
			}));
		}
	}

}
