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

import java.io.File;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.dialogs.WizardNewProjectCreationPage;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.wizards.newresource.BasicNewResourceWizard;

import ts.eclipse.ide.core.TypeScriptCorePlugin;
import ts.eclipse.ide.core.nodejs.IDENodejsProcessHelper;
import ts.eclipse.ide.core.nodejs.IEmbeddedNodejs;
import ts.eclipse.ide.core.preferences.TypeScriptCorePreferenceConstants;
import ts.eclipse.ide.core.utils.WorkbenchResourceUtil;
import ts.eclipse.ide.internal.ui.TypeScriptUIMessages;
import ts.eclipse.ide.internal.ui.dialogs.WorkspaceResourceSelectionDialog;
import ts.eclipse.ide.internal.ui.dialogs.WorkspaceResourceSelectionDialog.Mode;
import ts.eclipse.ide.terminal.interpreter.LineCommand;
import ts.eclipse.ide.ui.preferences.ScrolledPageContent;
import ts.eclipse.ide.ui.preferences.StatusInfo;
import ts.eclipse.ide.ui.utils.StatusUtil;
import ts.eclipse.ide.ui.widgets.IStatusChangeListener;
import ts.nodejs.NodejsProcessHelper;
import ts.utils.FileUtils;
import ts.utils.StringUtils;

/**
 * Main wizard page to create a TypeScript project.
 *
 */
public abstract class AbstractWizardNewTypeScriptProjectCreationPage extends WizardNewProjectCreationPage implements Listener, IStatusChangeListener {

	private final BasicNewResourceWizard wizard;

	// Node.js
	private boolean useEmbeddedNodeJs;
	private String embeddedNodeJsId;
	private String customNodeJsPath;

	private boolean hasEmbeddedNodeJs;
	private Button useEmbeddedNodeJsButton;
	private Combo embeddedNodeJs;
	private Combo installedNodeJs;

	private Button browseFileSystemButton;
	private Button browseWorkspaceButton;

	private Text nodePath;
	private Text nodeVersion;

	public AbstractWizardNewTypeScriptProjectCreationPage(String pageName, BasicNewResourceWizard wizard) {
		super(pageName);
		this.wizard = wizard;
	}

	@Override
	public final void createControl(Composite parent) {
		super.createControl(parent);

		Composite body = (Composite) getControl();
		createPageBody(body);
		createWorkingSetGroup(body, wizard.getSelection(), new String[] { "org.eclipse.ui.resourceWorkingSetPage" }); //$NON-NLS-1$
		Dialog.applyDialogFont(body);

		// initialize page with default values
		initializeDefaultValues();

		// Updates the state of the components
		updateComponents(null);

	}

	/**
	 * Create page body. User can override this method to add new UI fields.
	 *
	 * @param parent
	 */
	protected void createPageBody(Composite parent) {
		createNodejsBody(parent);
	}

	/** Creates the Body for selecting the Node.js configuration. */
	private void createNodejsBody(Composite parent) {
		Group group = new Group(parent, SWT.NONE);
		group.setFont(parent.getFont());
		group.setText(TypeScriptUIMessages.AbstractWizardNewTypeScriptProjectCreationPage_nodejs_group_label);
		group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		int nColumns = 2;
		GridLayout layout = new GridLayout();
		layout.numColumns = nColumns;
		group.setLayout(layout);

		IEmbeddedNodejs[] installs = TypeScriptCorePlugin.getNodejsInstallManager().getNodejsInstalls();
		hasEmbeddedNodeJs = installs.length > 0;
		if (hasEmbeddedNodeJs) {
			// Embedded node.js
			createEmbeddedNodejsField(group, installs);
		}
		// Installed node.js
		createInstalledNodejsField(group);

		// Path info.
		createNodePathInfo(group);
	}
	
	protected ExpandableComposite createStyleSection(Composite parent, String label, int nColumns) {
		ExpandableComposite excomposite= new ExpandableComposite(parent, SWT.NONE, ExpandableComposite.TWISTIE | ExpandableComposite.CLIENT_INDENT);
		excomposite.setText(label);
		excomposite.setExpanded(false);
		excomposite.setFont(JFaceResources.getFontRegistry().getBold(JFaceResources.DIALOG_FONT));
		excomposite.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, false, nColumns, 1));
		excomposite.addExpansionListener(new ExpansionAdapter() {
			public void expansionStateChanged(ExpansionEvent e) {
				expandedStateChanged((ExpandableComposite) e.getSource());
			}
		});
		//fExpandables.add(excomposite);
		makeScrollableCompositeAware(excomposite);
		return excomposite;
	}
	
	protected final void expandedStateChanged(ExpandableComposite expandable) {
		ScrolledPageContent parentScrolledComposite= getParentScrolledComposite(expandable);
		if (parentScrolledComposite != null) {
			parentScrolledComposite.reflow(true);
		}
	}
	
	protected void makeScrollableCompositeAware(Control control) {
		ScrolledPageContent parentScrolledComposite= getParentScrolledComposite(control);
		if (parentScrolledComposite != null) {
			parentScrolledComposite.adaptChild(control);
		}
	}
	
	protected ScrolledPageContent getParentScrolledComposite(Control control) {
		Control parent= control.getParent();
		while (!(parent instanceof ScrolledPageContent) && parent != null) {
			parent= parent.getParent();
		}
		if (parent instanceof ScrolledPageContent) {
			return (ScrolledPageContent) parent;
		}
		return null;
	}


	/** Creates the field for the embedded Node.js. */
	private void createEmbeddedNodejsField(Composite parent, IEmbeddedNodejs[] installs) {
		useEmbeddedNodeJsButton = new Button(parent, SWT.RADIO);
		useEmbeddedNodeJsButton
				.setText(TypeScriptUIMessages.AbstractWizardNewTypeScriptProjectCreationPage_useEmbeddedNodeJs_label);
		useEmbeddedNodeJsButton.addListener(SWT.Selection, this);

		embeddedNodeJs = new Combo(parent, SWT.READ_ONLY);
		embeddedNodeJs.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		// Create combo of embedded node.js
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
		embeddedNodeJs.addListener(SWT.Modify, this);
		embeddedNodeJs.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent e) {
				embeddedNodeJsId = embeddedNodeJs.getText();
			}
		});
	}

	/** Creates the field for selecting the installed Node.js. */
	private void createInstalledNodejsField(Composite parent) {
		if (hasEmbeddedNodeJs) {
			Button useInstalledNodejs = new Button(parent, SWT.RADIO);
			useInstalledNodejs
					.setText(TypeScriptUIMessages.AbstractWizardNewTypeScriptProjectCreationPage_useInstalledNodeJs_label);
			useInstalledNodejs.addListener(SWT.Selection, this);
		}
		String[] defaultPaths = IDENodejsProcessHelper.getAvailableNodejsPaths();
		installedNodeJs = new Combo(parent, SWT.NONE);
		installedNodeJs.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		installedNodeJs.setItems(defaultPaths);
		installedNodeJs.addListener(SWT.Modify, this);
		installedNodeJs.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent e) {
				customNodeJsPath = installedNodeJs.getText();
			}
		});
		// Create Browse buttons.
		createBrowseButtons(parent, installedNodeJs);
	}

	/** Creates the "Browse" Buttons (file system and workspace). */
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
					filePathCombo.setText(
							TypeScriptCorePlugin.getTypeScriptRepositoryManager().generateFileName(resource, null));
				}

			}
		});
	}

	/** Creates the Info part, where Node.js path and version is shown. 
	 * @return */
	private void createNodePathInfo(Composite parent) {
		ExpandableComposite expandable = createStyleSection(parent, TypeScriptUIMessages.AbstractWizardNewTypeScriptProjectCreationPage_nodejs_info_label, 2);
		
		Composite composite = new Composite(expandable, SWT.NONE);
		composite.setLayout(new GridLayout());
		GridData gridData = new GridData(GridData.FILL_BOTH);
		gridData.horizontalSpan = 2;
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

		expandable.setClient(composite);
	}

	/** Initializes the Widgets with default-values. */
	protected void initializeDefaultValues() {

		// Default values for Node.js
		if (hasEmbeddedNodeJs) {
			useEmbeddedNodeJsButton.setSelection(true);
			embeddedNodeJs.select(0);
		} else {
			if (installedNodeJs.getItemCount() > 0) {
				installedNodeJs.select(0);
			}
		}
	}

	@Override
	protected boolean validatePage() {
		boolean valid = super.validatePage();
		if (valid) {
			IStatus status = validatePageImpl();
			statusChanged(status);
			valid = !status.matches(IStatus.ERROR);
		}
		return valid;
	}

	/** Validates the Page and returns the most severe status. */
	protected IStatus validatePageImpl() {
		return validateAndUpdateNodejsPath();
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
		nodeJsChanged(status.getNodeFile());
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
		boolean embedded = hasEmbeddedNodeJs && useEmbeddedNodeJsButton.getSelection();
		if (embedded) {
			int selectedIndex = embeddedNodeJs.getSelectionIndex();
			if (selectedIndex >= 0) {
				IEmbeddedNodejs[] installs = TypeScriptCorePlugin.getNodejsInstallManager().getNodejsInstalls();
				IEmbeddedNodejs install = installs[selectedIndex];
				nodeFile = install.getPath();
			} else {
				return new NodeJsStatus(null, null,
						TypeScriptUIMessages.NodejsConfigurationBlock_installedNode_required_error);
			}
		} else {
			String nodeJsPath = installedNodeJs.getText();
			if (StringUtils.isEmpty(nodeJsPath)) {
				// ERROR: the installed path is empty
				return new NodeJsStatus(null, null,
						TypeScriptUIMessages.NodejsConfigurationBlock_installedNode_required_error);
			} else {
				nodeFile = WorkbenchResourceUtil.resolvePath(nodeJsPath, null);
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

	/** Writes the Node.js settings into the given preferences. */
	public void updateNodeJSPreferences(IEclipsePreferences preferences) {
		if (useEmbeddedNodeJs) {
			preferences.putBoolean(TypeScriptCorePreferenceConstants.USE_NODEJS_EMBEDDED, true);
			preferences.put(TypeScriptCorePreferenceConstants.NODEJS_EMBEDDED_ID, embeddedNodeJsId);
		} else {
			preferences.putBoolean(TypeScriptCorePreferenceConstants.USE_NODEJS_EMBEDDED, false);
			preferences.put(TypeScriptCorePreferenceConstants.NODEJS_PATH, customNodeJsPath);
		}
	}

	@Override
	public final void statusChanged(IStatus status) {
		StatusUtil.applyToStatusLine(this, status);
	}

	@Override
	public void handleEvent(Event event) {
		setPageComplete(validatePage());
		updateComponents(event);
	}

	/** Updates the state of the components. */
	protected void updateComponents(Event event) {
		Widget item = event != null ? event.item : null;
		if (item == null || item == useEmbeddedNodeJsButton)
			updateNodeJsMode();
	}

	/** Updates the Node.js mode and enables/disables the widgets accordingly. */
	private void updateNodeJsMode() {
		if (!hasEmbeddedNodeJs) {
			return;
		}
		useEmbeddedNodeJs = useEmbeddedNodeJsButton.getSelection();
		embeddedNodeJs.setEnabled(useEmbeddedNodeJs);
		installedNodeJs.setEnabled(!useEmbeddedNodeJs);
		browseFileSystemButton.setEnabled(!useEmbeddedNodeJs);
		browseWorkspaceButton.setEnabled(!useEmbeddedNodeJs);
	}

	/** The NodeJS-File has changed. */
	protected void nodeJsChanged(File nodeFile) {
	}

	/** Updates the Commands, which should be executed after creating the Project. */
	public void updateCommand(List<LineCommand> commands, IProject project) {
	}

	/** Class for the Status of the selected Node.js. */
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
}
