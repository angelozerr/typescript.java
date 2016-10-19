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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.dialogs.ControlEnableState;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.preferences.IWorkbenchPreferenceContainer;

import ts.eclipse.ide.core.TypeScriptCorePlugin;
import ts.eclipse.ide.internal.ui.dialogs.IStatusChangeListener;
import ts.eclipse.ide.ui.preferences.BrowseButtonsComposite;
import ts.eclipse.ide.ui.preferences.OptionsConfigurationBlock;
import ts.eclipse.ide.ui.preferences.ScrolledPageContent;
import ts.repository.ITypeScriptRepository;

/**
 * Server configuration block.
 *
 */
public abstract class AbstractTypeScriptRepositoryConfigurationBlock extends OptionsConfigurationBlock {

	private Composite controlsComposite;
	private ControlEnableState blockEnableState;
	private Combo embeddedComboBox;
	private Combo installedComboBox;
	private Button useEmbedded;

	private BrowseButtonsComposite browseButtons;

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
		group.setFont(parent.getFont());
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
		List<String> values = new ArrayList<String>();
		List<String> labels = new ArrayList<String>();
		String label = null;
		for (ITypeScriptRepository repository : respositories) {
			label = getRepositoryLabel(repository);
			if (label != null) {
				values.add(repository.getName());
				labels.add(label);
			}
		}
		embeddedComboBox = newComboControl(parent, getEmbeddedTypescriptKey(),
				values.toArray(new String[values.size()]), labels.toArray(new String[labels.size()]));
		embeddedComboBox.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	}

	protected String getRepositoryLabel(ITypeScriptRepository repository) {
		return repository.getName();
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

		installedComboBox = newComboControl(parent, getInstalledTypescriptPathKey(), getDefaultPaths(),
				getDefaultPaths(), false);
		installedComboBox.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		// Create Browse buttons.
		browseButtons = new BrowseButtonsComposite(parent, installedComboBox, getProject(), SWT.NONE);
	}

	protected abstract String[] getDefaultPaths();

	private void updateComboBoxes() {
		boolean embedded = useEmbedded.getSelection();
		embeddedComboBox.setEnabled(embedded);
		installedComboBox.setEnabled(!embedded);
		browseButtons.setEnabled(!embedded);
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
