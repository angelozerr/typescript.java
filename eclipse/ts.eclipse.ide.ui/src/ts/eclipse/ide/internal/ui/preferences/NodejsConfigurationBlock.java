/**
 *  Copyright (c) 2015-2016 Angelo ZERR.
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

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.dialogs.ControlEnableState;
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
import org.eclipse.ui.preferences.IWorkbenchPreferenceContainer;

import ts.eclipse.ide.core.TypeScriptCorePlugin;
import ts.eclipse.ide.core.nodejs.IDENodejsProcessHelper;
import ts.eclipse.ide.core.nodejs.IEmbeddedNodejs;
import ts.eclipse.ide.core.preferences.TypeScriptCorePreferenceConstants;
import ts.eclipse.ide.internal.ui.TypeScriptUIMessages;
import ts.eclipse.ide.internal.ui.dialogs.IStatusChangeListener;
import ts.eclipse.ide.internal.ui.dialogs.WorkspaceResourceSelectionDialog;
import ts.eclipse.ide.internal.ui.dialogs.WorkspaceResourceSelectionDialog.Mode;
import ts.eclipse.ide.ui.preferences.OptionsConfigurationBlock;
import ts.eclipse.ide.ui.preferences.ScrolledPageContent;
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
	private Label nodePathTitle;
	private Text nodePath;

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
		String[] values = new String[installs.length];
		String[] valueLabels = new String[installs.length];
		int i = 0;
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

		String[] defaultPaths = IDENodejsProcessHelper.getDefaultNodejsPaths();
		installedComboBox = newComboControl(parent, PREF_NODEJS_PATH, defaultPaths, defaultPaths, false);
		installedComboBox.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		installedComboBox.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updatePath();
			}
		});
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

		// Node path label
		nodePathTitle = new Label(composite, SWT.NONE);
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
		updatePath();
	}

	private void updatePath() {
		boolean embedded = useEmbedNodeJs.getSelection();
		if (embedded) {
			IEmbeddedNodejs[] installs = TypeScriptCorePlugin.getNodejsInstallManager().getNodejsInstalls();
			IEmbeddedNodejs install = installs[embeddedComboBox.getSelectionIndex()];
			nodePath.setText(FileUtils.getPath(install.getPath()));
		} else {
			nodePath.setText(installedComboBox.getText());
		}
	}

	@Override
	protected void validateSettings(Key changedKey, String oldValue, String newValue) {
		if (!areSettingsEnabled()) {
			return;
		}
		if (changedKey != null) {

		}
	}

	@Override
	protected String[] getFullBuildDialogStrings(boolean workspaceSettings) {
		return null;
	}

}
