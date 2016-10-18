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
import org.eclipse.ui.preferences.IWorkbenchPreferenceContainer;

import ts.eclipse.ide.core.preferences.TypeScriptCorePreferenceConstants;
import ts.eclipse.ide.internal.ui.TypeScriptUIMessages;
import ts.eclipse.ide.internal.ui.dialogs.IStatusChangeListener;

/**
 * Compiler configuration block.
 *
 */
public class TypeScriptRuntimeConfigurationBlock extends AbstractTypeScriptRepositoryConfigurationBlock {

	private static final String[] DEFAULT_PATHS = new String[] { "${project_loc:node_modules/typescript}" };

	private static final Key PREF_USE_EMBEDDED_TYPESCRIPT = getTypeScriptCoreKey(
			TypeScriptCorePreferenceConstants.USE_EMBEDDED_TYPESCRIPT);
	private static final Key PREF_TYPESCRIPT_EMBEDDED = getTypeScriptCoreKey(
			TypeScriptCorePreferenceConstants.EMBEDDED_TYPESCRIPT_ID);
	private static final Key PREF_TYPESCRIPT_PATH = getTypeScriptCoreKey(
			TypeScriptCorePreferenceConstants.INSTALLED_TYPESCRIPT_PATH);
	private static final Key PREF_TSSERVER_TRACE_ON_CONSOLE = getTypeScriptCoreKey(
			TypeScriptCorePreferenceConstants.TSSERVER_TRACE_ON_CONSOLE);

	public TypeScriptRuntimeConfigurationBlock(IStatusChangeListener context, IProject project,
			IWorkbenchPreferenceContainer container) {
		super(context, project, getKeys(), container);
	}

	@Override
	protected void createBody(Composite parent) {
		super.createBody(parent);
		super.addCheckBox(parent, TypeScriptUIMessages.TypeScriptRuntimeConfigurationBlock_traceOnConsole_label,
				PREF_TSSERVER_TRACE_ON_CONSOLE, new String[] { "true", "false" }, 0);
	}

	private static Key[] getKeys() {
		return new Key[] { PREF_USE_EMBEDDED_TYPESCRIPT, PREF_TYPESCRIPT_EMBEDDED, PREF_TYPESCRIPT_PATH,
				PREF_TSSERVER_TRACE_ON_CONSOLE };
	}

	@Override
	protected String getTypeScriptGroupLabel() {
		return TypeScriptUIMessages.TypeScriptRuntimeConfigurationBlock_typescript_group_label;
	}

	@Override
	protected String getEmbeddedCheckboxLabel() {
		return TypeScriptUIMessages.TypeScriptRuntimeConfigurationBlock_embedded_checkbox_label;
	}

	@Override
	protected String getInstalledCheckboxLabel() {
		return TypeScriptUIMessages.TypeScriptRuntimeConfigurationBlock_installed_checkbox_label;
	}

	@Override
	protected Key getUseEmbeddedTypescriptKey() {
		return PREF_USE_EMBEDDED_TYPESCRIPT;
	}

	@Override
	protected Key getEmbeddedTypescriptKey() {
		return PREF_TYPESCRIPT_EMBEDDED;
	}

	@Override
	protected Key getInstalledTypescriptPathKey() {
		return PREF_TYPESCRIPT_PATH;
	}

	@Override
	protected String[] getDefaultPaths() {
		return DEFAULT_PATHS;
	}
}
