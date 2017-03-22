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

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import ts.eclipse.ide.core.TypeScriptCorePlugin;
import ts.eclipse.ide.core.nodejs.IDENodejsProcessHelper;
import ts.eclipse.ide.core.nodejs.IEmbeddedNodejs;
import ts.eclipse.ide.internal.ui.TypeScriptUIMessages;
import ts.eclipse.ide.internal.ui.dialogs.WorkspaceResourceSelectionDialog;
import ts.eclipse.ide.internal.ui.dialogs.WorkspaceResourceSelectionDialog.Mode;
import ts.eclipse.ide.ui.widgets.NPMInstallWidget;
import ts.eclipse.ide.ui.wizards.AbstractWizardPage;
import ts.repository.ITypeScriptRepository;
import ts.utils.StringUtils;

public class TypeScriptRuntimeAndNodejsWizardPage extends AbstractWizardPage {

	private static final String PAGE_NAME = "TypeScriptRuntimeAndNodejsWizardPage";

	// TypeScript Runtime
	private Button useEmbeddedTsRuntimeButton;
	private boolean useEmbeddedTsRuntime;
	private Combo embeddedTsRuntime;
	private NPMInstallWidget installTsRuntime;

	// Node.js
	private Button useEmbeddedNodeJsButton;
	private Combo embeddedNodeJs;
	private Combo installedNodeJs;

	private Button browseFileSystemButton;
	private Button browseWorkspaceButton;

	private Text nodePath;
	private Text nodeVersion;

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

		createTypeScriptRuntimeBody(controlsComposite);
		createNodejsBody(controlsComposite);
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

		// Embedded TypeScript
		createEmbeddedTypeScriptField(group);
		// Install TypeScript
		createInstallScriptField(group);
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
	}

	private void updateTsRuntimeMode() {
		useEmbeddedTsRuntime = useEmbeddedTsRuntimeButton.getSelection();
		embeddedTsRuntime.setEnabled(useEmbeddedTsRuntime);
		installTsRuntime.setEnabled(!useEmbeddedTsRuntime);
	}

	// ------------------- Node.js content

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

	private void createEmbeddedNodejsField(Composite parent) {
		useEmbeddedNodeJsButton = new Button(parent, SWT.RADIO);
		useEmbeddedNodeJsButton
				.setText(TypeScriptUIMessages.TypeScriptRuntimeAndNodejsWizardPage_useEmbeddedNodeJs_label);
		useEmbeddedNodeJsButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateNodeJsMode();
			}
		});

		embeddedNodeJs = new Combo(parent, SWT.READ_ONLY);
		embeddedNodeJs.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		// Create combo of embedded node.js
		IEmbeddedNodejs[] installs = TypeScriptCorePlugin.getNodejsInstallManager().getNodejsInstalls();
		String[] values = new String[installs.length];
		String[] valueLabels = new String[installs.length];
		int i = 0;
		for (IEmbeddedNodejs install : installs) {
			values[i] = install.getId();
			valueLabels[i] = install.getName();
			i++;
		}
		embeddedNodeJs.setItems(valueLabels);
		embeddedNodeJs.setFont(JFaceResources.getDialogFont());

	}

	private void createInstalledNodejsField(Composite parent) {
		Button useInstalledNodejs = new Button(parent, SWT.RADIO);
		useInstalledNodejs.setText(TypeScriptUIMessages.TypeScriptRuntimeAndNodejsWizardPage_useInstalledNodeJs_label);
		useInstalledNodejs.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateNodeJsMode();
			}
		});

		String[] defaultPaths = IDENodejsProcessHelper.getAvailableNodejsPaths();
		installedNodeJs = new Combo(parent, SWT.NONE);
		installedNodeJs.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		installedNodeJs.setItems(defaultPaths);

		// Create Browse buttons.
		createBrowseButtons(parent, installedNodeJs);

	}

	protected void createBrowseButtons(final Composite parent, final Combo filePathCombo) {
		Composite buttons = new Composite(parent, SWT.NONE);
		buttons.setLayout(new GridLayout(2, false));
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		gd.horizontalAlignment = SWT.RIGHT;
		buttons.setLayoutData(gd);

		browseFileSystemButton = new Button(buttons, SWT.NONE);
		browseFileSystemButton.setText(TypeScriptUIMessages.Browse_FileSystem_button);
		browseFileSystemButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				FileDialog dialog = new FileDialog(parent.getShell());
				dialog.setFilterPath(filePathCombo.getText());
				String result = dialog.open();
				if (!StringUtils.isEmpty(result)) {
					filePathCombo.setText(result);
				}

			}
		});

		browseWorkspaceButton = new Button(buttons, SWT.NONE);
		browseWorkspaceButton.setText(TypeScriptUIMessages.Browse_Workspace_button);
		browseWorkspaceButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				WorkspaceResourceSelectionDialog dialog = new WorkspaceResourceSelectionDialog(parent.getShell(),
						Mode.FILE);
				if (dialog.open() == Window.OK) {
					IResource resource = (IResource) dialog.getFirstResult();
					filePathCombo.setText(TypeScriptCorePlugin.getTypeScriptRepositoryManager()
							.generateFileName(resource, null));
				}

			}
		});
	}

	private void createNodePathInfo(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout());
		GridData gridData = new GridData(GridData.FILL_BOTH);
		composite.setLayoutData(gridData);

		// Node version label
		Label nodeVersionTitle = new Label(composite, SWT.NONE);
		nodeVersionTitle.setText(TypeScriptUIMessages.NodejsConfigurationBlock_nodeVersion_label);
		gridData = new GridData(GridData.VERTICAL_ALIGN_BEGINNING);
		nodeVersionTitle.setLayoutData(gridData);

		nodeVersion = new Text(composite, SWT.WRAP | SWT.READ_ONLY);
		nodeVersion.setText(""); //$NON-NLS-1$
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = 2;
		gridData.widthHint = 200;
		nodeVersion.setLayoutData(gridData);

		// Node path label
		Label nodePathTitle = new Label(composite, SWT.NONE);
		nodePathTitle.setText(TypeScriptUIMessages.NodejsConfigurationBlock_nodePath_label);
		gridData = new GridData(GridData.VERTICAL_ALIGN_BEGINNING);
		nodePathTitle.setLayoutData(gridData);

		nodePath = new Text(composite, SWT.WRAP | SWT.READ_ONLY);
		nodePath.setText(""); //$NON-NLS-1$
		gridData = new GridData(GridData.FILL_BOTH);
		gridData.horizontalSpan = 2;
		gridData.widthHint = 200;
		nodePath.setLayoutData(gridData);

	}

	@Override
	protected void initializeDefaultValues() {
		// Default values for TypeScript runtime
		embeddedTsRuntime.select(0);
		useEmbeddedTsRuntimeButton.setSelection(true);
		updateTsRuntimeMode();

		// Default values for Node.js
		embeddedNodeJs.select(0);
		useEmbeddedNodeJsButton.setSelection(true);
		updateNodeJsMode();

	}

	private void updateNodeJsMode() {
		boolean useEmbeddedNodeJs = useEmbeddedNodeJsButton.getSelection();
		embeddedNodeJs.setEnabled(useEmbeddedNodeJs);
		installedNodeJs.setEnabled(!useEmbeddedNodeJs);
		browseFileSystemButton.setEnabled(!useEmbeddedNodeJs);
		browseWorkspaceButton.setEnabled(!useEmbeddedNodeJs);

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
