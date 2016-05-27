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
package ts.eclipse.ide.internal.core.resources;

import java.io.File;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.PreferenceChangeEvent;

import ts.client.completions.ICompletionEntryMatcher;
import ts.cmd.tslint.TslintSettingsStrategy;
import ts.eclipse.ide.core.TypeScriptCorePlugin;
import ts.eclipse.ide.core.nodejs.IEmbeddedNodejs;
import ts.eclipse.ide.core.preferences.TypeScriptCorePreferenceConstants;
import ts.eclipse.ide.core.resources.AbstractTypeScriptSettings;
import ts.eclipse.ide.core.resources.IIDETypeScriptProjectSettings;
import ts.eclipse.ide.core.resources.buildpath.ITypeScriptBuildPath;
import ts.eclipse.ide.internal.core.repository.IDETypeScriptRepositoryManager;
import ts.eclipse.ide.internal.core.resources.buildpath.TypeScriptBuildPath;
import ts.repository.ITypeScriptRepository;
import ts.resources.SynchStrategy;
import ts.utils.StringUtils;

/**
 * IDE TypeScript project settings.
 *
 */
public class IDETypeScriptProjectSettings extends AbstractTypeScriptSettings implements IIDETypeScriptProjectSettings {

	private final IDETypeScriptProject tsProject;
	private SaveProjectPreferencesJob savePreferencesJob;
	private boolean updatingBuildPath;
	private TslintSettingsStrategy tslintStrategy;

	public IDETypeScriptProjectSettings(IDETypeScriptProject tsProject) {
		super(tsProject.getProject(), TypeScriptCorePlugin.PLUGIN_ID);
		this.tsProject = tsProject;
	}

	/**
	 * Returns true if JSON request/response can be traced inside Eclipse
	 * console and false otherwise.
	 * 
	 * @param project
	 * @return true if JSON request/response can be traced inside Eclipse
	 *         console and false otherwise.
	 */
	@Override
	public boolean isTraceOnConsole() {
		return super.getBooleanPreferencesValue(TypeScriptCorePreferenceConstants.TSSERVER_TRACE_ON_CONSOLE, false);
	}

	@Override
	public IEmbeddedNodejs getEmbeddedNodejs() {
		String id = super.getStringPreferencesValue(TypeScriptCorePreferenceConstants.NODEJS_EMBEDDED_ID, null);
		return TypeScriptCorePlugin.getNodejsInstallManager().findNodejsInstall(id);
	}

	@Override
	public File getNodejsInstallPath() {
		if (super.getBooleanPreferencesValue(TypeScriptCorePreferenceConstants.USE_NODEJS_EMBEDDED, false)) {
			// Use Embedded nodejs.
			IEmbeddedNodejs embed = getEmbeddedNodejs();
			return embed != null ? embed.getPath() : null;
		}

		// Use Installed node.js
		String path = super.getStringPreferencesValue(TypeScriptCorePreferenceConstants.NODEJS_PATH, null);
		return resolvePath(path);
	}

	@Override
	public SynchStrategy getSynchStrategy() {
		return SynchStrategy.CHANGE;
	}

	@Override
	public File getTscFile() {
		if (super.getBooleanPreferencesValue(TypeScriptCorePreferenceConstants.TSC_USE_EMBEDDED_TYPESCRIPT, false)) {
			// Use TypeScript Repository.
			ITypeScriptRepository repository = getRepository(
					TypeScriptCorePreferenceConstants.TSC_EMBEDDED_TYPESCRIPT_ID);
			return (repository != null) ? repository.getTscFile() : null;
		}

		// Use Installed TypScript
		String path = super.getStringPreferencesValue(TypeScriptCorePreferenceConstants.TSC_INSTALLED_TYPESCRIPT_PATH,
				null);
		File resolvedPath = resolvePath(path);
		return resolvedPath != null ? IDETypeScriptRepositoryManager.getTscFile(resolvedPath) : null;
	}

	@Override
	public File getTsserverFile() {
		if (super.getBooleanPreferencesValue(TypeScriptCorePreferenceConstants.TSSERVER_USE_EMBEDDED_TYPESCRIPT,
				false)) {
			// Use TypeScript Repository.
			ITypeScriptRepository repository = getRepository(
					TypeScriptCorePreferenceConstants.TSSERVER_EMBEDDED_TYPESCRIPT_ID);
			return (repository != null) ? repository.getTsserverFile() : null;
		}

		// Use Installed TypScript
		String path = super.getStringPreferencesValue(
				TypeScriptCorePreferenceConstants.TSSERVER_INSTALLED_TYPESCRIPT_PATH, null);
		File resolvedPath = resolvePath(path);
		return resolvedPath != null ? IDETypeScriptRepositoryManager.getTsserverFile(resolvedPath) : null;
	}

	private File resolvePath(String path) {
		if (!StringUtils.isEmpty(path)) {
			IPath p = TypeScriptCorePlugin.getTypeScriptRepositoryManager().getPath(path, super.getProject());
			return p != null ? p.toFile() : new File(path);
		}
		return null;
	}

	private ITypeScriptRepository getRepository(String preferenceName) {
		String name = super.getStringPreferencesValue(preferenceName, null);
		if (StringUtils.isEmpty(name)) {
			return null;
		}
		return TypeScriptCorePlugin.getTypeScriptRepositoryManager().getRepository(name);
	}

	@Override
	public void preferenceChange(PreferenceChangeEvent event) {
		if (isNodejsPreferencesChanged(event)) {
			getTypeScriptProject().disposeServer();
			getTypeScriptProject().disposeCompiler();
		} else if (isTsserverPreferencesChanged(event)) {
			getTypeScriptProject().disposeServer();
		} else if (isTscPreferencesChanged(event)) {
			getTypeScriptProject().disposeCompiler();
		} else if (isTypeScriptBuildPathPreferencesChanged(event) && !updatingBuildPath) {
			getTypeScriptProject().disposeBuildPath();
		} else if (isTslintPreferencesChanged(event)) {
			this.tslintStrategy = null;
			getTypeScriptProject().disposeTslint();
		}
	}

	private boolean isNodejsPreferencesChanged(PreferenceChangeEvent event) {
		return TypeScriptCorePreferenceConstants.USE_NODEJS_EMBEDDED.equals(event.getKey())
				|| TypeScriptCorePreferenceConstants.NODEJS_EMBEDDED_ID.equals(event.getKey())
				|| TypeScriptCorePreferenceConstants.NODEJS_PATH.equals(event.getKey());
	}

	private boolean isTsserverPreferencesChanged(PreferenceChangeEvent event) {
		return TypeScriptCorePreferenceConstants.TSSERVER_USE_EMBEDDED_TYPESCRIPT.equals(event.getKey())
				|| TypeScriptCorePreferenceConstants.TSSERVER_EMBEDDED_TYPESCRIPT_ID.equals(event.getKey())
				|| TypeScriptCorePreferenceConstants.TSSERVER_INSTALLED_TYPESCRIPT_PATH.equals(event.getKey())
				|| TypeScriptCorePreferenceConstants.TSSERVER_TRACE_ON_CONSOLE.equals(event.getKey());
	}

	private boolean isTscPreferencesChanged(PreferenceChangeEvent event) {
		return TypeScriptCorePreferenceConstants.TSC_USE_EMBEDDED_TYPESCRIPT.equals(event.getKey())
				|| TypeScriptCorePreferenceConstants.TSC_EMBEDDED_TYPESCRIPT_ID.equals(event.getKey())
				|| TypeScriptCorePreferenceConstants.TSC_INSTALLED_TYPESCRIPT_PATH.equals(event.getKey());
	}

	private boolean isTslintPreferencesChanged(PreferenceChangeEvent event) {
		return TypeScriptCorePreferenceConstants.TSLINT_STRATEGY.equals(event.getKey())
				|| TypeScriptCorePreferenceConstants.TSLINT_USE_CUSTOM_TSLINTJSON_FILE.equals(event.getKey())
				|| TypeScriptCorePreferenceConstants.TSLINT_USE_EMBEDDED_TYPESCRIPT.equals(event.getKey())
				|| TypeScriptCorePreferenceConstants.TSLINT_EMBEDDED_TYPESCRIPT_ID.equals(event.getKey())
				|| TypeScriptCorePreferenceConstants.TSLINT_INSTALLED_TYPESCRIPT_PATH.equals(event.getKey());
	}

	private boolean isTypeScriptBuildPathPreferencesChanged(PreferenceChangeEvent event) {
		return TypeScriptCorePreferenceConstants.TYPESCRIPT_BUILD_PATH.equals(event.getKey());
	}

	private IDETypeScriptProject getTypeScriptProject() {
		return tsProject;
	}

	public void updateBuildPath(ITypeScriptBuildPath buildPath) {
		IEclipsePreferences preferences = getProjectPreferences();
		preferences.put(TypeScriptCorePreferenceConstants.TYPESCRIPT_BUILD_PATH, buildPath.toString());
		try {
			this.updatingBuildPath = true;
			save();
		} finally {
			this.updatingBuildPath = false;
		}
	}

	private void save() {
		IProject project = getTypeScriptProject().getProject();
		if (savePreferencesJob == null) {
			savePreferencesJob = new SaveProjectPreferencesJob(getProjectPreferences(), project);
		}
		savePreferencesJob.setRule(project.getWorkspace().getRoot());
		savePreferencesJob.schedule();
	}

	public ITypeScriptBuildPath getTypeScriptBuildPath() {
		String buildPaths = getStringPreferencesValue(TypeScriptCorePreferenceConstants.TYPESCRIPT_BUILD_PATH,
				TypeScriptCorePreferenceConstants.DEFAULT_TYPESCRIPT_BUILD_PATH);
		return TypeScriptBuildPath.load(getTypeScriptProject().getProject(), buildPaths);
	}

	@Override
	public ICompletionEntryMatcher getCompletionEntryMatcher() {
		// TODO: support entry matcher with preferences.
		return ICompletionEntryMatcher.LCS;
	}

	public boolean isUseCodeSnippetsOnMethodSuggest() {
		return true;
	}

	// -------------tslint

	@Override
	public File getTslintFile() {
		if (super.getBooleanPreferencesValue(TypeScriptCorePreferenceConstants.TSLINT_USE_EMBEDDED_TYPESCRIPT, false)) {
			// Use TypeScript Repository.
			ITypeScriptRepository repository = getRepository(
					TypeScriptCorePreferenceConstants.TSLINT_EMBEDDED_TYPESCRIPT_ID);
			return (repository != null) ? repository.getTslintFile() : null;
		}

		// Use Installed tslint
		String path = super.getStringPreferencesValue(
				TypeScriptCorePreferenceConstants.TSLINT_INSTALLED_TYPESCRIPT_PATH, null);
		File resolvedPath = resolvePath(path);
		return resolvedPath != null ? IDETypeScriptRepositoryManager.getTslintFile(resolvedPath) : null;
	}

	@Override
	public TslintSettingsStrategy getTslintStrategy() {
		if (tslintStrategy == null) {
			String strategy = super.getStringPreferencesValue(TypeScriptCorePreferenceConstants.TSLINT_STRATEGY,
					TslintSettingsStrategy.DisableTslint.name());
			try {
				tslintStrategy = TslintSettingsStrategy.valueOf(strategy);
			} catch (Throwable e) {
				tslintStrategy = TslintSettingsStrategy.DisableTslint;
			}
		}
		return tslintStrategy;
	}

	@Override
	public File getCustomTslintJsonFile() {
		String path = super.getStringPreferencesValue(
				TypeScriptCorePreferenceConstants.TSLINT_USE_CUSTOM_TSLINTJSON_FILE, null);
		return resolvePath(path);
	}

	@Override
	public boolean isEditorOptionsConvertTabsToSpaces() {
		return super.getBooleanPreferencesValue(TypeScriptCorePreferenceConstants.EDITOR_OPTIONS_CONVERT_TABS_TO_SPACES,
				TypeScriptCorePreferenceConstants.EDITOR_OPTIONS_CONVERT_TABS_TO_SPACES_DEFAULT);
	}

	@Override
	public int getEditorOptionsIndentSize() {
		return super.getIntegerPreferencesValue(TypeScriptCorePreferenceConstants.EDITOR_OPTIONS_INDENT_SIZE,
				TypeScriptCorePreferenceConstants.EDITOR_OPTIONS_INDENT_SIZE_DEFAULT);
	}

	@Override
	public int getEditorOptionsTabSize() {
		return super.getIntegerPreferencesValue(TypeScriptCorePreferenceConstants.EDITOR_OPTIONS_TAB_SIZE,
				TypeScriptCorePreferenceConstants.EDITOR_OPTIONS_TAB_SIZE_DEFAULT);

	}

}
