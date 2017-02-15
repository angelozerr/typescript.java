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
import ts.eclipse.ide.core.utils.PreferencesHelper;
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
		return PreferencesHelper.getStringPreferencesValue(key, def, getProjectPreferences(), getWorkspacePreferences(),
				getWorkspaceDefaultPreferences());
	}

	public Boolean getBooleanPreferencesValue(String key, Boolean def) {
		return PreferencesHelper.getBooleanPreferencesValue(key, def, getProjectPreferences(),
				getWorkspacePreferences(), getWorkspaceDefaultPreferences());
	}

	public Integer getIntegerPreferencesValue(String key, Integer def) {
		return PreferencesHelper.getIntegerPreferencesValue(key, def, getProjectPreferences(),
				getWorkspacePreferences(), getWorkspaceDefaultPreferences());
	}

	protected IEclipsePreferences getProjectPreferences() {
		return projectScope.getNode(pluginId);
	}

	protected IEclipsePreferences getWorkspacePreferences() {
		return PreferencesHelper.getWorkspacePreferences(pluginId);
	}

	protected IEclipsePreferences getWorkspaceDefaultPreferences() {
		return PreferencesHelper.getWorkspaceDefaultPreferences(pluginId);
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
		return resolvePath(path, getProject());
	}

	protected static File resolvePath(String path, IProject project) {
		if (!StringUtils.isEmpty(path)) {
			IPath p = TypeScriptCorePlugin.getTypeScriptRepositoryManager().getPath(path, project);
			return p != null ? p.toFile() : new File(path);
		}
		return null;
	}
}
