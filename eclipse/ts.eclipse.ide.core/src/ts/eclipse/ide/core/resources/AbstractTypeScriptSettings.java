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

import java.io.File;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener;

import ts.eclipse.ide.core.TypeScriptCorePlugin;
import ts.utils.StringUtils;

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
		getWorkspaceDefaultPreferences().addPreferenceChangeListener(this);
	}

	public String getStringPreferencesValue(String key, String def) {
		String value = getProjectPreferencesValue(key, def);
		if (value != null) {
			return value;
		}
		value = getStringWorkspacePreferencesValue(key, def);
		if (value != null) {
			return value;
		}
		return getStringWorkspaceDefaultPreferencesValue(key, def);
	}

	public String getProjectPreferencesValue(String key, String def) {
		IEclipsePreferences node = getProjectPreferences();
		return node != null ? node.get(key, def) : null;
	}

	public String getStringWorkspacePreferencesValue(String key, String def) {
		IEclipsePreferences node = getWorkspacePreferences();
		return node != null ? node.get(key, def) : null;
	}

	public String getStringWorkspaceDefaultPreferencesValue(String key, String def) {
		IEclipsePreferences node = getWorkspaceDefaultPreferences();
		return node != null ? node.get(key, def) : null;
	}

	public Boolean getBooleanPreferencesValue(String key, Boolean def) {
		Boolean value = getProjectPreferencesValue(key, (Boolean) null);
		if (value != null) {
			return value;
		}
		value = getBooleanWorkspacePreferencesValue(key, (Boolean) null);
		if (value != null) {
			return value;
		}
		return getBooleanWorkspaceDefaultPreferencesValue(key, def);
	}

	public Boolean getProjectPreferencesValue(String key, Boolean def) {
		IEclipsePreferences node = getProjectPreferences();
		return getBoolean(node, key, def);
	}

	public Boolean getBooleanWorkspacePreferencesValue(String key, Boolean def) {
		IEclipsePreferences node = getWorkspacePreferences();
		return getBoolean(node, key, def);
	}

	public Boolean getBooleanWorkspaceDefaultPreferencesValue(String key, Boolean def) {
		IEclipsePreferences node = getWorkspaceDefaultPreferences();
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
		value = getIntegerWorkspacePreferencesValue(key, (Integer) null);
		if (value != null) {
			return value;
		}
		return getIntegerWorkspaceDefaultPreferencesValue(key, def);
	}

	public Integer getProjectPreferencesValue(String key, Integer def) {
		IEclipsePreferences node = getProjectPreferences();
		return getInteger(node, key, def);
	}

	public Integer getIntegerWorkspacePreferencesValue(String key, Integer def) {
		IEclipsePreferences node = getWorkspacePreferences();
		return getInteger(node, key, def);
	}

	public Integer getIntegerWorkspaceDefaultPreferencesValue(String key, Integer def) {
		IEclipsePreferences node = getWorkspaceDefaultPreferences();
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

	protected IEclipsePreferences getWorkspaceDefaultPreferences() {
		return WorkspaceTypeScriptSettingsHelper.getWorkspaceDefaultPreferences(pluginId);
	}

	public void dispose() {
		getProjectPreferences().removePreferenceChangeListener(this);
		getWorkspacePreferences().removePreferenceChangeListener(this);
		getWorkspaceDefaultPreferences().removePreferenceChangeListener(this);
	}

	public IProject getProject() {
		return project;
	}

	protected File resolvePath(String path) {
		if (!StringUtils.isEmpty(path)) {
			IPath p = TypeScriptCorePlugin.getTypeScriptRepositoryManager().getPath(path, getProject());
			return p != null ? p.toFile() : new File(path);
		}
		return null;
	}
}
