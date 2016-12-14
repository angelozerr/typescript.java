/**
 *  Copyright (c) 2013-2016 Angelo ZERR.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package ts.eclipse.ide.internal.ui.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;

import ts.eclipse.ide.core.resources.WorkspaceTypeScriptSettingsHelper;
import ts.eclipse.ide.ui.TypeScriptUIPlugin;
import ts.eclipse.ide.ui.preferences.TypeScriptUIPreferenceConstants;

/**
 * Eclipse preference initializer for TypeScript UI.
 * 
 */
public class TypeScriptUIPreferenceInitializer extends AbstractPreferenceInitializer {

	@Override
	public void initializeDefaultPreferences() {
		IEclipsePreferences node = WorkspaceTypeScriptSettingsHelper
				.getWorkspaceDefaultPreferences(TypeScriptUIPlugin.PLUGIN_ID);

		// initialize properties for TextMate using
		initializeTextMatePreferences(node);

	}

	/**
	 * initialize properties for TextMate using
	 * 
	 * @param node
	 */
	private void initializeTextMatePreferences(IEclipsePreferences node) {
		node.putBoolean(TypeScriptUIPreferenceConstants.USE_TEXMATE_FOR_SYNTAX_COLORING, false);
	}
}