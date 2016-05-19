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
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.preferences.IWorkbenchPreferenceContainer;

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
