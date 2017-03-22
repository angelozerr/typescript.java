/**
 *  Copyright (c) 2015-2016 Angelo ZERR.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Lorenzo Dalla Vecchia <lorenzo.dallavecchia@webratio.com> - initial API and implementation
 */
package ts.eclipse.ide.internal.ui.preferences;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.dialogs.ControlEnableState;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.preferences.IWorkbenchPreferenceContainer;

import ts.eclipse.ide.internal.ui.TypeScriptUIMessages;
import ts.eclipse.ide.ui.preferences.OptionsConfigurationBlock;
import ts.eclipse.ide.ui.preferences.ScrolledPageContent;
import ts.eclipse.ide.ui.preferences.TypeScriptUIPreferenceConstants;
import ts.eclipse.ide.ui.widgets.IStatusChangeListener;

/**
 * Editor save actions configuration block.
 *
 */
public class SaveActionsConfigurationBlock extends OptionsConfigurationBlock {

	// Editor Save Actions
	private static final Key PREF_EDITOR_SAVE_ACTIONS = getTypeScriptUIKey(
			TypeScriptUIPreferenceConstants.EDITOR_SAVE_ACTIONS);
	private static final Key PREF_EDITOR_SAVE_ACTIONS_FORMAT = getTypeScriptUIKey(
			TypeScriptUIPreferenceConstants.EDITOR_SAVE_ACTIONS_FORMAT);

	private Composite controlsComposite;
	private ControlEnableState blockEnableState;
	private Button saveActionsButton;
	private Composite saveActionsContainer;
	private ControlEnableState saveActionsContainerEnableState;

	public SaveActionsConfigurationBlock(IStatusChangeListener context, IProject project,
			IWorkbenchPreferenceContainer container) {
		super(context, project, getKeys(), container);
		blockEnableState = null;
	}

	private static Key[] getKeys() {
		return new Key[] { PREF_EDITOR_SAVE_ACTIONS, PREF_EDITOR_SAVE_ACTIONS_FORMAT };
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
		controlsComposite.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, false));

		layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.numColumns = 1;
		controlsComposite.setLayout(layout);

		// save actions
		createSaveActions(controlsComposite);
		return pageContent;
	}

	/**
	 * Create save actions.
	 * 
	 * @param parent
	 */
	private void createSaveActions(Composite parent) {

		// Perform the selected actions on save
		saveActionsButton = addCheckBox(parent,
				TypeScriptUIMessages.SaveActionsPreferencePage_performTheSelectedActionsOnSave,
				PREF_EDITOR_SAVE_ACTIONS, new String[] { "true", "false" }, 0);

		saveActionsContainer = new Composite(parent, SWT.NONE);
		{
			GridData data = new GridData(SWT.FILL, SWT.TOP, true, false);
			data.horizontalIndent = 10;
			saveActionsContainer.setLayoutData(data);
			GridLayout layout = new GridLayout();
			layout.marginHeight = 0;
			layout.marginWidth = 0;
			layout.numColumns = 1;
			saveActionsContainer.setLayout(layout);
		}

		// Format source code
		addCheckBox(saveActionsContainer, TypeScriptUIMessages.SaveActionsPreferencePage_formatSourceCode,
				PREF_EDITOR_SAVE_ACTIONS_FORMAT, new String[] { "true", "false" }, 0);
	}

	@Override
	protected void reconcileControls() {
		if (saveActionsButton.getSelection()) {
			if (saveActionsContainerEnableState != null) {
				saveActionsContainerEnableState.restore();
				saveActionsContainerEnableState = null;
			}
		} else {
			if (saveActionsContainerEnableState == null) {
				saveActionsContainerEnableState = ControlEnableState.disable(saveActionsContainer);
			}
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
