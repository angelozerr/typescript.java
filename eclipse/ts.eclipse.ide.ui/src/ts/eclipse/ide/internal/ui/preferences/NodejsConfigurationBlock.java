/**
 *  Copyright (c) 2015-2017 Angelo ZERR.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 *  Lorenzo Dalla Vecchia <lorenzo.dallavecchia@webratio.com> - added reconcileControls hook
 */
package ts.eclipse.ide.internal.ui.preferences;

import java.io.File;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.ControlEnableState;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.util.NLS;
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
import org.eclipse.ui.preferences.IWorkbenchPreferenceContainer;

import ts.eclipse.ide.core.TypeScriptCorePlugin;
import ts.eclipse.ide.core.nodejs.IDENodejsProcessHelper;
import ts.eclipse.ide.core.nodejs.IEmbeddedNodejs;
import ts.eclipse.ide.core.preferences.TypeScriptCorePreferenceConstants;
import ts.eclipse.ide.core.utils.WorkbenchResourceUtil;
import ts.eclipse.ide.internal.ui.TypeScriptUIMessages;
import ts.eclipse.ide.internal.ui.dialogs.WorkspaceResourceSelectionDialog;
import ts.eclipse.ide.internal.ui.dialogs.WorkspaceResourceSelectionDialog.Mode;
import ts.eclipse.ide.ui.preferences.IStatusChangeListener;
import ts.eclipse.ide.ui.preferences.OptionsConfigurationBlock;
import ts.eclipse.ide.ui.preferences.ScrolledPageContent;
import ts.eclipse.ide.ui.preferences.StatusInfo;
import ts.nodejs.NodejsProcess;
import ts.nodejs.NodejsProcessHelper;
import ts.utils.FileUtils;
import ts.utils.StringUtils;

/**
 * Node.js configuration block.
 *
 */
public class NodejsConfigurationBlock extends OptionsConfigurationBlock {

	private static final Key PREF_USE_NODEJS_EMBEDDED = getTypeScriptCoreKey(
			TypeScriptCorePreferenceConstants.USE_NODEJS_EMBEDDED);
	private static final Key PREF_NODEJS_EMBEDDED = getTypeScriptCoreKey(
			TypeScriptCorePreferenceConstants.NODEJS_EMBEDDED_ID);
	private static final Key PREF_NODEJS_PATH = getTypeScriptCoreKey(TypeScriptCorePreferenceConstants.NODEJS_PATH);

	private Composite controlsComposite;
	private ControlEnableState blockEnableState;
	private Combo embeddedComboBox;
	private Combo installedComboBox;
	private Button useEmbedNodeJs;

	private Button browseFileSystemButton;
	private Button browseWorkspaceButton;

	private Text nodePath;
	private Text nodeVersion;

	public NodejsConfigurationBlock(IStatusChangeListener context, IProject project,
			IWorkbenchPreferenceContainer container) {
		super(context, project, getKeys(), container);
		blockEnableState = null;
	}

	private static Key[] getKeys() {
		return new Key[] { PREF_USE_NODEJS_EMBEDDED, PREF_NODEJS_EMBEDDED, PREF_NODEJS_PATH };
	}

	public void enablePreferenceContent(boolean enable) {
		if (controlsComposite != null && !controlsComposite.isDisposed()) {
			if (enable) {
				if (blockEnableState != null) {
					blockEnableState.restore();
					blockEnableState = null;
				}
			} else {
				if (blockEnableState == null) {
					blockEnableState = ControlEnableState.disable(controlsComposite);
				}
			}
		}
	}

	@Override
	protected Composite createUI(Composite parent) {
		final ScrolledPageContent pageContent = new ScrolledPageContent(parent);
		Composite composite = pageContent.getBody();
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		composite.setLayout(layout);

		controlsComposite = new Composite(composite, SWT.NONE);
		controlsComposite.setFont(composite.getFont());
		controlsComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.numColumns = 1;
		controlsComposite.setLayout(layout);

		int nColumns = 2;
		layout = new GridLayout();
		layout.numColumns = nColumns;

		Group group = new Group(controlsComposite, SWT.NONE);
		group.setFont(controlsComposite.getFont());
		group.setText(TypeScriptUIMessages.NodejsConfigurationBlock_nodejs_group_label);
		group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		group.setLayout(layout);

		// Embedded node.js
		createEmbeddedNodejsField(group);
		// Installed node.js
		createInstalledNodejsField(group);
		// Path info.
		createNodePathInfo(composite);
		updateComboBoxes();
		return pageContent;
	}

	private void createEmbeddedNodejsField(Composite parent) {
		// Create "Embedded node.js" checkbox
		useEmbedNodeJs = addRadioBox(parent, TypeScriptUIMessages.NodejsConfigurationBlock_embedded_checkbox_label,
				PREF_USE_NODEJS_EMBEDDED, new String[] { "true", "true" }, 0);
		useEmbedNodeJs.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateComboBoxes();
			}
		});

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
		embeddedComboBox = newComboControl(parent, PREF_NODEJS_EMBEDDED, values, valueLabels);
		embeddedComboBox.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	}

	private void createInstalledNodejsField(Composite parent) {
		// Create "Installed node.js" checkbox
		Button useInstalledNodeJs = addRadioBox(parent,
				TypeScriptUIMessages.NodejsConfigurationBlock_installed_checkbox_label, PREF_USE_NODEJS_EMBEDDED,
				new String[] { "false", "false" }, 0);
		useInstalledNodeJs.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateComboBoxes();
			}
		});

		String[] defaultPaths = IDENodejsProcessHelper.getAvailableNodejsPaths();
		installedComboBox = newComboControl(parent, PREF_NODEJS_PATH, defaultPaths, defaultPaths, false);
		installedComboBox.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		// Create Browse buttons.
		createBrowseButtons(parent, installedComboBox);

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
					installedComboBox.setText(result);
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
				IResource initialResource = TypeScriptCorePlugin.getTypeScriptRepositoryManager()
						.getResource(filePathCombo.getText(), getProject());
				if (initialResource != null) {
					dialog.setInitialSelection(initialResource);
				}
				if (dialog.open() == Window.OK) {
					IResource resource = (IResource) dialog.getFirstResult();
					filePathCombo.setText(TypeScriptCorePlugin.getTypeScriptRepositoryManager()
							.generateFileName(resource, getProject()));
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

	private void updateComboBoxes() {
		boolean embedded = useEmbedNodeJs.getSelection();
		embeddedComboBox.setEnabled(embedded);
		installedComboBox.setEnabled(!embedded);
		browseFileSystemButton.setEnabled(!embedded);
		browseWorkspaceButton.setEnabled(!embedded);
	}

	private class NodeJsStatus extends StatusInfo {

		private final File nodeFile;
		private final String version;

		public NodeJsStatus(File nodeFile, String version, String errorMessage) {
			if (errorMessage != null) {
				setError(errorMessage);
			}
			this.nodeFile = nodeFile;
			this.version = version;
		}

		public File getNodeFile() {
			return nodeFile;
		}

		public String getNodeVersion() {
			return version;
		}
	}

	/**
	 * Update the node version, path labels and returns the validation status of
	 * the nodejs path.
	 * 
	 * @return the validation status of the nodejs path.
	 */
	private IStatus validateAndUpdateNodejsPath() {
		// Compute node.j status
		NodeJsStatus status = validateNodejsPath();
		// Update node version & path
		if (status.isOK()) {
			nodeVersion.setText(status.getNodeVersion());
			nodePath.setText(FileUtils.getPath(status.getNodeFile()));
		} else {
			nodeVersion.setText("");
			nodePath.setText("");
		}
		return status;
	}

	/**
	 * Returns the status of the node.js path.
	 * 
	 * @return the status of the node.js path.
	 */
	private NodeJsStatus validateNodejsPath() {
		File nodeFile = null;
		String version = null;
		boolean embedded = useEmbedNodeJs.getSelection();
		if (embedded) {
			int selectedIndex = embeddedComboBox.getSelectionIndex();
			if (selectedIndex == 0) {
				// ERROR: the embedded node.js combo is not selected.
				return new NodeJsStatus(null, null,
						TypeScriptUIMessages.NodejsConfigurationBlock_embeddedNode_required_error);
			} else {
				IEmbeddedNodejs[] installs = TypeScriptCorePlugin.getNodejsInstallManager().getNodejsInstalls();
				IEmbeddedNodejs install = installs[selectedIndex - 1];
				nodeFile = install.getPath();
			}
		} else {
			String nodeJsPath = installedComboBox.getText();
			if (StringUtils.isEmpty(nodeJsPath)) {
				// ERROR: the installed path is empty
				return new NodeJsStatus(null, null,
						TypeScriptUIMessages.NodejsConfigurationBlock_installedNode_required_error);
			} else {
				nodeFile = WorkbenchResourceUtil.resolvePath(nodeJsPath, getProject());
			}
		}

		if (!nodeFile.exists()) {
			// ERROR: node.js file doesn't exists
			return new NodeJsStatus(null, null, NLS.bind(
					TypeScriptUIMessages.NodejsConfigurationBlock_nodeFile_exists_error, FileUtils.getPath(nodeFile)));
		} else {
			version = NodejsProcessHelper.getNodeVersion(nodeFile);
			if (StringUtils.isEmpty(version)) {
				// ERROR: the file path is not a node.exe
				return new NodeJsStatus(null, null,
						NLS.bind(TypeScriptUIMessages.NodejsConfigurationBlock_nodeFile_invalid_error,
								FileUtils.getPath(nodeFile)));
			}
		}
		// Node.js path is valid
		return new NodeJsStatus(nodeFile, version, null);
	}

	@Override
	protected void validateSettings(Key changedKey, String oldValue, String newValue) {
		IStatus status = validateAndUpdateNodejsPath();
		fContext.statusChanged(status);
	}

	@Override
	protected String[] getFullBuildDialogStrings(boolean workspaceSettings) {
		return null;
	}

}
