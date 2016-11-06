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
package ts.eclipse.ide.core.resources;

import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.PreferenceChangeEvent;
import org.eclipse.core.runtime.preferences.InstanceScope;

import ts.eclipse.ide.core.TypeScriptCorePlugin;
import ts.eclipse.ide.core.preferences.TypeScriptCorePreferenceConstants;

/**
 * Workspace TypeScript settings.
 *
 */
public class WorkspaceTypeScriptSettingsHelper {

	private static UseSalsa useSalsa;

	static {
		IEclipsePreferences preferences = WorkspaceTypeScriptSettingsHelper
				.getWorkspacePreferences(TypeScriptCorePlugin.PLUGIN_ID);
		String name = preferences.get(TypeScriptCorePreferenceConstants.USE_SALSA_AS_JS_INFERENCE,
				UseSalsa.WhenNoJSDTNature.name());
		useSalsa = UseSalsa.valueOf(name);
		preferences.addPreferenceChangeListener(new IPreferenceChangeListener() {

			public void preferenceChange(PreferenceChangeEvent event) {
				if (TypeScriptCorePreferenceConstants.USE_SALSA_AS_JS_INFERENCE.equals(event.getKey())) {
					try {
						useSalsa = UseSalsa.valueOf(event.getNewValue().toString());
					} catch (Throwable e) {

					}
				}
			}
		});
	}

	public static IEclipsePreferences getWorkspacePreferences(String pluginId) {
		return InstanceScope.INSTANCE.getNode(pluginId);
	}

	public static IEclipsePreferences getWorkspaceDefaultPreferences(String pluginId) {
		return DefaultScope.INSTANCE.getNode(pluginId);
	}

	public static UseSalsa getUseSalsa() {
		if (useSalsa == null) {
			return UseSalsa.WhenNoJSDTNature;
		}
		return useSalsa;
	}

}
