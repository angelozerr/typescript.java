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
import org.eclipse.jface.dialogs.ControlEnableState;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.preferences.IWorkbenchPreferenceContainer;

import ts.eclipse.ide.core.preferences.TypeScriptCorePreferenceConstants;
import ts.eclipse.ide.internal.ui.TypeScriptUIMessages;
import ts.eclipse.ide.ui.preferences.IStatusChangeListener;
import ts.eclipse.ide.ui.preferences.OptionsConfigurationBlock;
import ts.eclipse.ide.ui.preferences.ScrolledPageContent;

/**
 * Automatic Type Acquisition (ATA) configuration block.
 *
 */
public class ATAConfigurationBlock extends OptionsConfigurationBlock {

	private static final Key PREF_INSTALL_TYPES_DISABLE_ATA = getTypeScriptCoreKey(
			TypeScriptCorePreferenceConstants.INSTALL_TYPES_DISABLE_ATA);
	private static final Key PREF_INSTALL_TYPES_ENABLE_TELEMETRY = getTypeScriptCoreKey(
			TypeScriptCorePreferenceConstants.INSTALL_TYPES_ENABLE_TELEMETRY);

	private Composite controlsComposite;
	private ControlEnableState blockEnableState;
	private Button enableTelemetry;

	public ATAConfigurationBlock(IStatusChangeListener context, IProject project,
			IWorkbenchPreferenceContainer container) {
		super(context, project, getKeys(), container);
		blockEnableState = null;
	}

	private static Key[] getKeys() {
		return new Key[] { PREF_INSTALL_TYPES_DISABLE_ATA, PREF_INSTALL_TYPES_ENABLE_TELEMETRY };
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

		Label group = new Label(controlsComposite, SWT.NONE);
		group.setFont(controlsComposite.getFont());
		group.setText(TypeScriptUIMessages.ATAConfigurationBlock_description);
		group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		// Disable ATA
		createDisableATAField(controlsComposite);
		// Enable telemetry
		createEnableTelementryField(controlsComposite);
		return pageContent;
	}

	private void createDisableATAField(Composite parent) {
		// Create "Disable ATA" checkbox
		enableTelemetry = addCheckBox(parent, TypeScriptUIMessages.ATAConfigurationBlock_disableATA_checkbox_label,
				PREF_INSTALL_TYPES_DISABLE_ATA, new String[] { "true", "true" }, 0);
	}
	
	private void createEnableTelementryField(Composite parent) {
		// Create "Enable telemetry" checkbox
		enableTelemetry = addCheckBox(parent, TypeScriptUIMessages.ATAConfigurationBlock_enableTelemetry_checkbox_label,
				PREF_INSTALL_TYPES_ENABLE_TELEMETRY, new String[] { "true", "true" }, 0);
	}

	/*protected void createBrowseButtons(final Composite parent, final Combo filePathCombo) {
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
	}*/

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
