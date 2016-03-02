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
import org.eclipse.ui.preferences.IWorkbenchPreferenceContainer;

import ts.eclipse.ide.core.preferences.TypeScriptCorePreferenceConstants;
import ts.eclipse.ide.internal.ui.TypeScriptUIMessages;
import ts.eclipse.ide.internal.ui.dialogs.IStatusChangeListener;

/**
 * Compiler configuration block.
 *
 */
public class CompilerConfigurationBlock extends AbstractTypeScriptRepositoryConfigurationBlock {

	private static final Key PREF_TSC_USE_EMBEDDED_TYPESCRIPT = getTypeScriptCoreKey(
			TypeScriptCorePreferenceConstants.TSC_USE_EMBEDDED_TYPESCRIPT);
	private static final Key PREF_TSC_TYPESCRIPT_EMBEDDED = getTypeScriptCoreKey(
			TypeScriptCorePreferenceConstants.TSC_EMBEDDED_TYPESCRIPT_ID);
	private static final Key PREF_TSC_TYPESCRIPT_PATH = getTypeScriptCoreKey(
			TypeScriptCorePreferenceConstants.TSC_INSTALLED_TYPESCRIPT_PATH);

	public CompilerConfigurationBlock(IStatusChangeListener context, IProject project,
			IWorkbenchPreferenceContainer container) {
		super(context, project, getKeys(), container);
	}

	private static Key[] getKeys() {
		return new Key[] { PREF_TSC_USE_EMBEDDED_TYPESCRIPT, PREF_TSC_TYPESCRIPT_EMBEDDED, PREF_TSC_TYPESCRIPT_PATH };
	}

	@Override
	protected String getTypeScriptGroupLabel() {
		return TypeScriptUIMessages.CompilerConfigurationBlock_typescript_group_label;
	}

	@Override
	protected String getEmbeddedCheckboxLabel() {
		return TypeScriptUIMessages.CompilerConfigurationBlock_embedded_checkbox_label;
	}

	@Override
	protected String getInstalledCheckboxLabel() {
		return TypeScriptUIMessages.CompilerConfigurationBlock_installed_checkbox_label;
	}

	@Override
	protected Key getUseEmbeddedTypescriptKey() {
		return PREF_TSC_USE_EMBEDDED_TYPESCRIPT;
	}

	@Override
	protected Key getEmbeddedTypescriptKey() {
		return PREF_TSC_TYPESCRIPT_EMBEDDED;
	}

	@Override
	protected Key getInstalledTypescriptPathKey() {
		return PREF_TSC_TYPESCRIPT_PATH;
	}

}
