/**
 *  Copyright (c) 2015-2016 Angelo ZERR.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
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
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.preferences.IWorkbenchPreferenceContainer;

import ts.eclipse.ide.core.TypeScriptCorePlugin;
import ts.eclipse.ide.internal.ui.TypeScriptUIMessages;
import ts.eclipse.ide.internal.ui.dialogs.IStatusChangeListener;
import ts.eclipse.ide.internal.ui.dialogs.WorkspaceResourceSelectionDialog;
import ts.eclipse.ide.internal.ui.dialogs.WorkspaceResourceSelectionDialog.Mode;
import ts.eclipse.ide.ui.preferences.OptionsConfigurationBlock;
import ts.eclipse.ide.ui.preferences.ScrolledPageContent;
import ts.repository.ITypeScriptRepository;
import ts.utils.StringUtils;

/**
 * Server configuration block.
 *
 */
public abstract class AbstractTypeScriptRepositoryConfigurationBlock extends OptionsConfigurationBlock {

	private static final String[] DEFAULT_PATHS = new String[] { "${project_loc:node_modules/typescript}" };

	private Composite controlsComposite;
	private ControlEnableState blockEnableState;
	private Combo embeddedComboBox;
	private Combo installedComboBox;
	private Button useEmbedded;

	private Button browseFileSystemButton;
	private Button browseWorkspaceButton;

	public AbstractTypeScriptRepositoryConfigurationBlock(IStatusChangeListener context, IProject project,
			Key[] allKeys, IWorkbenchPreferenceContainer container) {
		super(context, project, allKeys, container);
		blockEnableState = null;
	}

	@Override
	protected Control createContents(Composite parent) {
		Composite contents = createUI(parent);
		validateSettings(null, null, null);
		return contents;
	}

	private Composite createUI(Composite parent) {
		final ScrolledPageContent pageContent = new ScrolledPageContent(parent);
		Composite composite = pageContent.getBody();
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		composite.setLayout(layout);

		controlsComposite = new Composite(composite, SWT.NONE);
		controlsComposite.setFont(composite.getFont());
		controlsComposite.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, false));

		layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.numColumns = 1;
		controlsComposite.setLayout(layout);

		createBody(controlsComposite);
		return pageContent;
	}

	protected void createBody(Composite parent) {
		int nColumns = 2;
		GridLayout layout = new GridLayout();
		layout.numColumns = nColumns;

		Group group = new Group(parent, SWT.NONE);
		group.setFont(controlsComposite.getFont());
		group.setText(getTypeScriptGroupLabel());
		group.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, false));
		group.setLayout(layout);

		// Embedded TypeScript
		createEmbeddedTypeScriptField(group);
		// Installed TypeScript
		createInstalledTypeScriptField(group);
		updateComboBoxes();
	}

	private void createEmbeddedTypeScriptField(Composite parent) {
		// Create "Embedded node.js" checkbox
		useEmbedded = addRadioBox(parent, getEmbeddedCheckboxLabel(), getUseEmbeddedTypescriptKey(),
				new String[] { "true", "true" }, 0);
		useEmbedded.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateComboBoxes();
			}
		});

		// Create combo of embedded node.js
		ITypeScriptRepository[] respositories = TypeScriptCorePlugin.getTypeScriptRepositoryManager().getRepositories();
		String[] values = new String[respositories.length];
		int i = 0;
		for (ITypeScriptRepository repository : respositories) {
			values[i] = repository.getName();
			i++;
		}
		embeddedComboBox = newComboControl(parent, getEmbeddedTypescriptKey(), values, values);
		embeddedComboBox.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	}

	private void createInstalledTypeScriptField(Composite parent) {
		// Create "Installed TypeScript" checkbox
		Button useInstalled = addRadioBox(parent, getInstalledCheckboxLabel(), getUseEmbeddedTypescriptKey(),
				new String[] { "false", "false" }, 0);
		useInstalled.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateComboBoxes();
			}
		});

		installedComboBox = newComboControl(parent, getInstalledTypescriptPathKey(), DEFAULT_PATHS, DEFAULT_PATHS,
				false);
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
				DirectoryDialog dialog = new DirectoryDialog(parent.getShell());
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
						Mode.FILE_FOLDER);
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

	private void updateComboBoxes() {
		boolean embedded = useEmbedded.getSelection();
		embeddedComboBox.setEnabled(embedded);
		installedComboBox.setEnabled(!embedded);
		browseFileSystemButton.setEnabled(!embedded);
		browseWorkspaceButton.setEnabled(!embedded);
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

	protected abstract String getTypeScriptGroupLabel();

	protected abstract String getEmbeddedCheckboxLabel();

	protected abstract String getInstalledCheckboxLabel();

	protected abstract Key getUseEmbeddedTypescriptKey();

	protected abstract Key getEmbeddedTypescriptKey();

	protected abstract Key getInstalledTypescriptPathKey();

}
