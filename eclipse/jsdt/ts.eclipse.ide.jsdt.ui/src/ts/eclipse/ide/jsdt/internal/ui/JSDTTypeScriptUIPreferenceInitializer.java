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
package ts.eclipse.ide.jsdt.internal.ui;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import ts.eclipse.ide.jsdt.ui.PreferenceConstants;
import ts.eclipse.ide.ui.TypeScriptUIPlugin;
import ts.eclipse.ide.ui.outline.TypeScriptContentOutlinePage;

/**
 * Preferences initializer for JSX.
 *
 */
public class JSDTTypeScriptUIPreferenceInitializer extends AbstractPreferenceInitializer {

	@Override
	public void initializeDefaultPreferences() {
		IPreferenceStore store = PreferenceConstants.getPreferenceStore();
		PreferenceConstants.initializeDefaultValues(store);

		// Link Editor with Outline
		TypeScriptUIPlugin.getDefault().getPreferenceStore()
				.setValue(TypeScriptContentOutlinePage.EDITOR_SYNC_OUTLINE_ON_CURSOR_MOVE, true);
	}

}
