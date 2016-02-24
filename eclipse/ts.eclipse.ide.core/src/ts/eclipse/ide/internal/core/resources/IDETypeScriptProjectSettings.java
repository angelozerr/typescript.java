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
import java.io.IOException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;

import ts.eclipse.ide.core.TypeScriptCorePlugin;
import ts.eclipse.ide.core.nodejs.INodejsInstall;
import ts.eclipse.ide.core.resources.AbstractTypeScriptSettings;
import ts.eclipse.ide.core.resources.IIDETypeScriptProjectSettings;
import ts.eclipse.ide.internal.core.preferences.TypeScriptCorePreferenceConstants;
import ts.resources.SynchStrategy;
import ts.utils.StringUtils;

/**
 * IDE TypeScript project settings.
 *
 */
public class IDETypeScriptProjectSettings extends AbstractTypeScriptSettings implements IIDETypeScriptProjectSettings {

	public IDETypeScriptProjectSettings(IProject project) {
		super(project, TypeScriptCorePlugin.PLUGIN_ID);
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
		return super.getBooleanPreferencesValue(TypeScriptCorePreferenceConstants.TRACE_ON_CONSOLE, false);
	}

	@Override
	public INodejsInstall getNodejsInstall() {
		String id = super.getStringPreferencesValue(TypeScriptCorePreferenceConstants.NODEJS_INSTALL, null);
		return TypeScriptCorePlugin.getNodejsInstallManager().findNodejsInstall(id);
	}

	@Override
	public File getNodejsInstallPath() {
		INodejsInstall install = getNodejsInstall();
		if (install != null) {
			if (install.isNative()) {
				String path = super.getStringPreferencesValue(TypeScriptCorePreferenceConstants.NODEJS_PATH, null);
				if (!StringUtils.isEmpty(path)) {
					return new File(path);
				}
			} else {
				return install.getPath();
			}
		}
		return null;
	}

	@Override
	public SynchStrategy getSynchStrategy() {
		return SynchStrategy.CHANGE;
	}

	@Override
	public File getTsserverFile() {
		try {
			File tsRepositoryFile = FileLocator.getBundleFile(Platform.getBundle("ts.repository"));
			File tsserverFile = new File(tsRepositoryFile, "node_modules/typescript/bin/tsserver");
			return tsserverFile;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

}
