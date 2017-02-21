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
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.preferences.IWorkbenchPreferenceContainer;

import ts.cmd.tslint.TslintSettingsStrategy;
import ts.eclipse.ide.core.preferences.TypeScriptCorePreferenceConstants;
import ts.eclipse.ide.internal.ui.TypeScriptUIMessages;
import ts.eclipse.ide.internal.ui.dialogs.IStatusChangeListener;
import ts.eclipse.ide.ui.preferences.BrowseButtonsComposite;
import ts.repository.ITypeScriptRepository;

/**
 * tslint configuration block.
 *
 */
// deprecated since now tslint is managed with tslint-language-service
@Deprecated
public class ValidationConfigurationBlock extends AbstractTypeScriptRepositoryConfigurationBlock {

	private static final String[] DEFAULT_PATHS = new String[] { "${project_loc:node_modules/tslint}" };
	private static final String[] DEFAULT_TSLINT_JSON = new String[] { "${project_loc:tslint.json}" };

	// tslint
	private static final Key PREF_TSLINT_USE_EMBEDDED_TYPESCRIPT = getTypeScriptCoreKey(
			TypeScriptCorePreferenceConstants.TSLINT_USE_EMBEDDED_TYPESCRIPT);
	private static final Key PREF_TSLINT_TYPESCRIPT_EMBEDDED = getTypeScriptCoreKey(
			TypeScriptCorePreferenceConstants.TSLINT_EMBEDDED_TYPESCRIPT_ID);
	private static final Key PREF_TSLINT_TYPESCRIPT_PATH = getTypeScriptCoreKey(
			TypeScriptCorePreferenceConstants.TSLINT_INSTALLED_TYPESCRIPT_PATH);

	// tslint.json
	private static final Key PRE_TSLINT_STRATEGY = getTypeScriptCoreKey(
			TypeScriptCorePreferenceConstants.TSLINT_STRATEGY);
	private static final Key PREF_TSLINT_USE_CUSTOM_TSLINTJSON_FILE = getTypeScriptCoreKey(
			TypeScriptCorePreferenceConstants.TSLINT_USE_CUSTOM_TSLINTJSON_FILE);

	private Button disableTslint;
	private Button useDefaultTslintJson;
	private Button useCustomTslintJson;
	private Button searchForTslintJson;
	private Combo customTslintJsonComboBox;
	private BrowseButtonsComposite browseButtons;

	public ValidationConfigurationBlock(IStatusChangeListener context, IProject project,
			IWorkbenchPreferenceContainer container) {
		super(context, project, getKeys(), container);
	}

	@Override
	protected void createBody(Composite parent) {
		// create tslint (json) config
		int nColumns = 2;
		GridLayout layout = new GridLayout();
		layout.numColumns = nColumns;

		Group group = new Group(parent, SWT.NONE);
		group.setFont(parent.getFont());
		group.setText(TypeScriptUIMessages.ValidationConfigurationBlock_tslintjson_group_label);
		group.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, false));
		group.setLayout(layout);

		createDisableTslintField(group);
		createUseDefaultTslintJsonField(group);
		createSearchForTslintJsonField(group);
		createUseCustomTslintJsonField(group);

		// updateTslintComboBoxes();

		// create tslint
		super.createBody(parent);

	}

	private void createDisableTslintField(Composite parent) {
		// Create "Disable tslint" checkbox
		disableTslint = addRadioBox(parent,
				TypeScriptUIMessages.ValidationConfigurationBlock_tslintjson_strategy_DisableTslint,
				PRE_TSLINT_STRATEGY, new String[] { TslintSettingsStrategy.DisableTslint.name(),
						TslintSettingsStrategy.DisableTslint.name() },
				0);
		disableTslint.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// updateTslintComboBoxes();
			}
		});
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = 2;
		disableTslint.setLayoutData(data);
	}

	private void createUseDefaultTslintJsonField(Composite parent) {
		// Create "Use the default 'tslint.json'." checkbox
		useDefaultTslintJson = addRadioBox(parent,
				TypeScriptUIMessages.ValidationConfigurationBlock_tslintjson_strategy_UseDefaultTslintJson,
				PRE_TSLINT_STRATEGY, new String[] { TslintSettingsStrategy.UseDefaultTslintJson.name(),
						TslintSettingsStrategy.UseDefaultTslintJson.name() },
				0);
		useDefaultTslintJson.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateTslintComboBoxes();
			}
		});
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = 2;
		useDefaultTslintJson.setLayoutData(data);
	}

	private void createSearchForTslintJsonField(Composite parent) {
		// Create "Search for 'tslint.json' from the folder (and parent folder
		// if not found)" checkbox
		searchForTslintJson = addRadioBox(parent,
				TypeScriptUIMessages.ValidationConfigurationBlock_tslintjson_strategy_SearchForTslintJson,
				PRE_TSLINT_STRATEGY, new String[] { TslintSettingsStrategy.SearchForTslintJson.name(),
						TslintSettingsStrategy.SearchForTslintJson.name() },
				0);
		searchForTslintJson.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateTslintComboBoxes();
			}
		});
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = 2;
		searchForTslintJson.setLayoutData(data);
	}

	private void createUseCustomTslintJsonField(Composite parent) {
		// Create "Use the given custom 'tslint.json''." checkbox
		useCustomTslintJson = addRadioBox(parent,
				TypeScriptUIMessages.ValidationConfigurationBlock_tslintjson_strategy_UseCustomTslintJson,
				PRE_TSLINT_STRATEGY, new String[] { TslintSettingsStrategy.UseCustomTslintJson.name(),
						TslintSettingsStrategy.UseCustomTslintJson.name() },
				0);
		useCustomTslintJson.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateTslintComboBoxes();
			}
		});
		customTslintJsonComboBox = newComboControl(parent, PREF_TSLINT_USE_CUSTOM_TSLINTJSON_FILE, DEFAULT_TSLINT_JSON,
				DEFAULT_TSLINT_JSON, false);
		customTslintJsonComboBox.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		// Create Browse buttons.
		browseButtons = new BrowseButtonsComposite(parent, customTslintJsonComboBox, getProject(), SWT.NONE);
	}
	
	private void updateTslintComboBoxes() {
		boolean custom = useCustomTslintJson.getSelection();
		customTslintJsonComboBox.setEnabled(custom);
		browseButtons.setEnabled(custom);
	}

	private static Key[] getKeys() {
		return new Key[] { PREF_TSLINT_USE_EMBEDDED_TYPESCRIPT, PREF_TSLINT_TYPESCRIPT_EMBEDDED,
				PREF_TSLINT_TYPESCRIPT_PATH, PRE_TSLINT_STRATEGY, PREF_TSLINT_USE_CUSTOM_TSLINTJSON_FILE };
	}

	protected String getRepositoryLabel(ITypeScriptRepository repository) {
		return repository.getTslintName();
	}

	@Override
	protected String getTypeScriptGroupLabel() {
		return TypeScriptUIMessages.ValidationConfigurationBlock_tslint_group_label;
	}

	@Override
	protected String getEmbeddedCheckboxLabel() {
		return TypeScriptUIMessages.ValidationConfigurationBlock_embedded_checkbox_label;
	}

	@Override
	protected String getInstalledCheckboxLabel() {
		return TypeScriptUIMessages.ValidationConfigurationBlock_installed_checkbox_label;
	}

	@Override
	protected Key getUseEmbeddedTypescriptKey() {
		return PREF_TSLINT_USE_EMBEDDED_TYPESCRIPT;
	}

	@Override
	protected Key getEmbeddedTypescriptKey() {
		return PREF_TSLINT_TYPESCRIPT_EMBEDDED;
	}

	@Override
	protected Key getInstalledTypescriptPathKey() {
		return PREF_TSLINT_TYPESCRIPT_PATH;
	}

	@Override
	protected String[] getDefaultPaths() {
		return DEFAULT_PATHS;
	}
}
