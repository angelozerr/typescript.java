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
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.preferences.IWorkbenchPreferenceContainer;

import ts.eclipse.ide.core.utils.TypeScriptResourceUtil;
import ts.eclipse.ide.internal.ui.TypeScriptUIMessages;
import ts.eclipse.ide.ui.preferences.PropertyAndPreferencePage;

/**
 * tsc preferences page
 *
 */
public class CompilerPreferencePage extends PropertyAndPreferencePage {

	public static final String PREF_ID = "ts.eclipse.ide.ui.preference.CompilerPreferencePage"; //$NON-NLS-1$
	public static final String PROP_ID = "ts.eclipse.ide.ui.property.CompilerPreferencePage"; //$NON-NLS-1$

	private CompilerConfigurationBlock configurationBlock;

	public CompilerPreferencePage() {
	}

	@Override
	public void createControl(Composite parent) {
		IWorkbenchPreferenceContainer container = (IWorkbenchPreferenceContainer) getContainer();
		configurationBlock = new CompilerConfigurationBlock(getNewStatusChangedListener(), getProject(), container);
		super.createControl(parent);
	}

	@Override
	protected Control createPreferenceHeaderContent(Composite parent) {
		final IProject project = getProject();
		if (project != null) {
			GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
			gd.horizontalSpan = 3;

			final Button enableBuilderCheckbox = new Button(parent, SWT.CHECK);
			enableBuilderCheckbox.setFont(JFaceResources.getDialogFont());
			enableBuilderCheckbox
					.setText(TypeScriptUIMessages.CompilerConfigurationBlock_enable_builder_checkbox_label);
			enableBuilderCheckbox.setLayoutData(gd);
			enableBuilderCheckbox.setSelection(TypeScriptResourceUtil.hasTypeScriptBuilder(project));
			enableBuilderCheckbox.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent event) {
					if (TypeScriptResourceUtil.hasTypeScriptBuilder(project)) {
						try {
							TypeScriptResourceUtil.removeTypeScriptBuilder(project);
						} catch (CoreException e) {
							ErrorDialog.openError(getShell(), TypeScriptUIMessages.TypeScriptBuilder_Error_title,
									TypeScriptUIMessages.TypeScriptBuilder_disable_Error_message, e.getStatus());
						}
					} else {
						try {
							TypeScriptResourceUtil.addTypeScriptBuilder(project);
						} catch (CoreException e) {
							ErrorDialog.openError(getShell(), TypeScriptUIMessages.TypeScriptBuilder_Error_title,
									TypeScriptUIMessages.TypeScriptBuilder_enable_Error_message, e.getStatus());
						}
					}
				}
			});

			new Label(parent, SWT.NONE).setLayoutData(new GridData());
		}
		return null;
	}

	@Override
	protected Control createPreferenceBodyContent(Composite composite) {
		return configurationBlock.createContents(composite);
	}

	@Override
	protected boolean hasProjectSpecificOptions(IProject project) {
		return configurationBlock.hasProjectSpecificOptions(project);
	}

	@Override
	protected String getPreferencePageID() {
		return PREF_ID;
	}

	@Override
	protected String getPropertyPageID() {
		return PROP_ID;
	}

	@Override
	protected void enablePreferenceContent(boolean enable) {
		if (configurationBlock != null) {
			configurationBlock.enablePreferenceContent(enable);
		}
	}

	@Override
	protected void enableProjectSpecificSettings(boolean useProjectSpecificSettings) {
		super.enableProjectSpecificSettings(useProjectSpecificSettings);
		if (configurationBlock != null) {
			configurationBlock.useProjectSpecificSettings(useProjectSpecificSettings);
		}
	}

	@Override
	public void dispose() {
		if (configurationBlock != null) {
			configurationBlock.dispose();
		}
		super.dispose();
	}

	protected void performDefaults() {
		super.performDefaults();
		if (configurationBlock != null) {
			configurationBlock.performDefaults();
		}
	}

	@Override
	public boolean performOk() {
		if (configurationBlock != null && !configurationBlock.performOk()) {
			return false;
		}
		return super.performOk();
	}

	@Override
	public void performApply() {
		if (configurationBlock != null) {
			configurationBlock.performApply();
		}
	}

}
