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
package ts.eclipse.ide.core.utils;

import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.PreferenceChangeEvent;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.osgi.service.prefs.BackingStoreException;

import ts.eclipse.ide.core.TypeScriptCorePlugin;
import ts.eclipse.ide.core.preferences.TypeScriptCorePreferenceConstants;
import ts.eclipse.ide.core.resources.UseSalsa;

/**
 * Workspace TypeScript settings.
 *
 */
public class PreferencesHelper {

	private static UseSalsa useSalsa;

	static {
		IEclipsePreferences preferences = PreferencesHelper.getWorkspacePreferences(TypeScriptCorePlugin.PLUGIN_ID);
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

	/**
	 * Returns the String preference value from the given key and list of
	 * Eclipse preferences.
	 * 
	 * @param key
	 *            the preference name.
	 * @param def
	 *            the default value if not found.
	 * @param preferences
	 *            the list of Eclipse preferences.
	 * @return the String preference value from the given key and list of
	 *         Eclipse preferences.
	 */
	public static String getStringPreferencesValue(String key, String def, IEclipsePreferences... preferences) {
		String value = null;
		for (int i = 0; i < preferences.length; i++) {
			value = getStringPreferencesValue(preferences[i], key, (String) null);
			if (value != null) {
				return value;
			}
		}
		return def;
	}

	/**
	 * Returns the String preference value from the given key and Eclipse
	 * preferences.
	 * 
	 * @param key
	 *            the preference name.
	 * @param def
	 *            the default value if not found.
	 * @param preferences
	 *            the Eclipse preferences.
	 * @return the String preference value from the given key and Eclipse
	 *         preferences.
	 */
	private static String getStringPreferencesValue(IEclipsePreferences preferences, String key, String def) {
		if (preferences == null) {
			return def;
		}
		String result = preferences.get(key, null);
		return result != null ? result : def;
	}

	/**
	 * Returns the boolean preference value from the given key and list of
	 * Eclipse preferences.
	 * 
	 * @param key
	 *            the preference name.
	 * @param def
	 *            the default value if not found.
	 * @param preferences
	 *            the list of Eclipse preferences.
	 * @return the boolean preference value from the given key and list of
	 *         Eclipse preferences.
	 */
	public static Boolean getBooleanPreferencesValue(String key, Boolean def, IEclipsePreferences... preferences) {
		Boolean value = null;
		for (int i = 0; i < preferences.length; i++) {
			value = getBooleanPreferencesValue(preferences[i], key, (Boolean) null);
			if (value != null) {
				return value;
			}
		}
		return def;
	}

	/**
	 * Returns the boolean preference value from the given key and Eclipse
	 * preferences.
	 * 
	 * @param key
	 *            the preference name.
	 * @param def
	 *            the default value if not found.
	 * @param preferences
	 *            the Eclipse preferences.
	 * @return the boolean preference value from the given key and Eclipse
	 *         preferences.
	 */
	private static Boolean getBooleanPreferencesValue(IEclipsePreferences preferences, String key, Boolean def) {
		if (preferences == null) {
			return def;
		}
		String result = preferences.get(key, null);
		if (result == null) {
			return def;
		}
		try {
			return Boolean.parseBoolean(result);
		} catch (Throwable e) {
			return def;
		}
	}

	/**
	 * Returns the Integer preference value from the given key and list of
	 * Eclipse preferences.
	 * 
	 * @param key
	 *            the preference name.
	 * @param def
	 *            the default value if not found.
	 * @param preferences
	 *            the list of Eclipse preferences.
	 * @return the Integer preference value from the given key and list of
	 *         Eclipse preferences.
	 */
	public static Integer getIntegerPreferencesValue(String key, Integer def, IEclipsePreferences... preferences) {
		Integer value = null;
		for (int i = 0; i < preferences.length; i++) {
			value = getIntegerPreferencesValue(preferences[i], key, (Integer) null);
			if (value != null) {
				return value;
			}
		}
		return def;
	}

	/**
	 * Returns the Integer preference value from the given key and Eclipse
	 * preferences.
	 * 
	 * @param key
	 *            the preference name.
	 * @param def
	 *            the default value if not found.
	 * @param preferences
	 *            the Eclipse preferences.
	 * @return the Integer preference value from the given key and Eclipse
	 *         preferences.
	 */
	private static Integer getIntegerPreferencesValue(IEclipsePreferences preferences, String key, Integer def) {
		if (preferences == null) {
			return def;
		}
		String result = preferences.get(key, null);
		if (result == null) {
			return def;
		}
		try {
			return Integer.parseInt(result);
		} catch (Throwable e) {
			return def;
		}
	}
}
