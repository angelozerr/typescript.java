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

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener;
import org.eclipse.core.runtime.preferences.InstanceScope;

/**
 * Abstract class for TypeScript settings which search preferences from the
 * given Eclipse Project Preferences and if not found from the workspace Eclipse
 * Preferences.
 *
 */
public abstract class AbstractTypeScriptSettings implements IPreferenceChangeListener {

	private final IProject project;
	private final ProjectScope projectScope;
	private final String pluginId;

	public AbstractTypeScriptSettings(IProject project, String pluginId) {
		this.project = project;
		this.projectScope = new ProjectScope(project);
		this.pluginId = pluginId;
		getProjectPreferences().addPreferenceChangeListener(this);
		getWorkspacePreferences().addPreferenceChangeListener(this);
		InstanceScope.INSTANCE.getNode(pluginId).addPreferenceChangeListener(this);
	}

	public String getStringPreferencesValue(String key, String def) {
		String value = getProjectPreferencesValue(key, def);
		if (value == null) {
			return getStringWorkspacePreferencesValue(key, def);
		}
		return value;
	}

	public String getProjectPreferencesValue(String key, String def) {
		IEclipsePreferences node = getProjectPreferences();
		return node != null ? node.get(key, def) : null;
	}

	public String getStringWorkspacePreferencesValue(String key, String def) {
		IEclipsePreferences node = getWorkspacePreferences();
		return node != null ? node.get(key, def) : null;
	}

	public Boolean getBooleanPreferencesValue(String key, Boolean def) {
		Boolean value = getProjectPreferencesValue(key, (Boolean) null);
		if (value != null) {
			return value;
		}
		return getBooleanWorkspacePreferencesValue(key, def);
	}

	public Boolean getProjectPreferencesValue(String key, Boolean def) {
		IEclipsePreferences node = getProjectPreferences();
		return getBoolean(node, key, def);
	}

	public Boolean getBooleanWorkspacePreferencesValue(String key, Boolean def) {
		IEclipsePreferences node = getWorkspacePreferences();
		return getBoolean(node, key, def);
	}

	private Boolean getBoolean(IEclipsePreferences preferences, String key, Boolean def) {
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

	public Integer getIntegerPreferencesValue(String key, Integer def) {
		Integer value = getProjectPreferencesValue(key, (Integer) null);
		if (value != null) {
			return value;
		}
		return getIntegerWorkspacePreferencesValue(key, def);
	}

	public Integer getProjectPreferencesValue(String key, Integer def) {
		IEclipsePreferences node = getProjectPreferences();
		return getInteger(node, key, def);
	}

	public Integer getIntegerWorkspacePreferencesValue(String key, Integer def) {
		IEclipsePreferences node = getWorkspacePreferences();
		return getInteger(node, key, def);
	}

	private Integer getInteger(IEclipsePreferences preferences, String key, Integer def) {
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

	protected IEclipsePreferences getProjectPreferences() {
		return projectScope.getNode(pluginId);
	}

	protected IEclipsePreferences getWorkspacePreferences() {
		return WorkspaceTypeScriptSettingsHelper.getWorkspacePreferences(pluginId);
	}

	public void dispose() {
		getProjectPreferences().removePreferenceChangeListener(this);
		getWorkspacePreferences().removePreferenceChangeListener(this);
		InstanceScope.INSTANCE.getNode(pluginId).removePreferenceChangeListener(this);
	}

	public IProject getProject() {
		return project;
	}
}
