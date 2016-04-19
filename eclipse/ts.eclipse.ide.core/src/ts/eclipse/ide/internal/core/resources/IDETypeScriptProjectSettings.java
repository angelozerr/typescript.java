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

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.PreferenceChangeEvent;

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
		} else if (isTypeScriptBuildPathPreferencesChanged(event)) {
			getTypeScriptProject().disposeBuildPath();
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

	private boolean isTypeScriptBuildPathPreferencesChanged(PreferenceChangeEvent event) {
		return TypeScriptCorePreferenceConstants.TYPESCRIPT_BUILD_PATH.equals(event.getKey());
	}

//	@Override
//	public boolean canValidate(IResource resource) {
//		// TODO: add a preferences to customize path to exclude for validation.
//		// today we exclude validation for files which are hosted inside
//		// node_modules.
//		IPath location = resource.getProjectRelativePath();
//		for (int i = 0; i < location.segmentCount(); i++) {
//			if ("node_modules".equals(location.segment(i))) {
//				return false;
//			}
//		}
//		return true;
//	}

	private IDETypeScriptProject getTypeScriptProject() {
		return tsProject;
	}

	public ITypeScriptBuildPath getTypeScriptBuildPath() {
		String buildPaths = getStringPreferencesValue(TypeScriptCorePreferenceConstants.TYPESCRIPT_BUILD_PATH,
				TypeScriptCorePreferenceConstants.DEFAULT_TYPESCRIPT_BUILD_PATH);
		return TypeScriptBuildPath.load(getTypeScriptProject().getProject(), buildPaths);
	}
}
