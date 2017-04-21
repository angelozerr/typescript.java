/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package ts.eclipse.ide.jsdt.internal.ui.refactoring;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.wst.jsdt.internal.ui.text.PreferencesAdapter;

import ts.eclipse.ide.core.TypeScriptCorePlugin;
import ts.eclipse.ide.core.preferences.TypeScriptCorePreferenceConstants;

public class RefactoringSavePreferences {

	public static final String PREF_SAVE_ALL_EDITORS = TypeScriptCorePreferenceConstants.REFACTOR_SAVE_ALL_EDITORS;

	public static boolean getSaveAllEditors() {
		IPreferenceStore store = new PreferencesAdapter(TypeScriptCorePlugin.getDefault().getPluginPreferences());
		return store.getBoolean(PREF_SAVE_ALL_EDITORS);
	}

	public static void setSaveAllEditors(boolean save) {
		IPreferenceStore store = new PreferencesAdapter(TypeScriptCorePlugin.getDefault().getPluginPreferences());
		store.setValue(PREF_SAVE_ALL_EDITORS, save);
	}
}
