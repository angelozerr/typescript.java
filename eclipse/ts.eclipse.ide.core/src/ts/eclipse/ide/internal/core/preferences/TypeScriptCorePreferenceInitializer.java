/**
 *  Copyright (c) 2013-2016 Angelo ZERR.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package ts.eclipse.ide.internal.core.preferences;

import java.io.File;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;

import ts.eclipse.ide.core.TypeScriptCorePlugin;
import ts.eclipse.ide.core.nodejs.IDENodejsProcessHelper;
import ts.eclipse.ide.core.nodejs.IEmbeddedNodejs;
import ts.eclipse.ide.core.nodejs.INodejsInstallManager;
import ts.eclipse.ide.core.preferences.TypeScriptCorePreferenceConstants;
import ts.eclipse.ide.internal.core.Trace;
import ts.eclipse.ide.internal.core.resources.IDETypeScriptProjectSettings;
import ts.repository.ITypeScriptRepository;

/**
 * Eclipse preference initializer for TypeScript Core.
 * 
 */
public class TypeScriptCorePreferenceInitializer extends AbstractPreferenceInitializer {

	@Override
	public void initializeDefaultPreferences() {
		IEclipsePreferences node = IDETypeScriptProjectSettings.getWorkspacePreferences(TypeScriptCorePlugin.PLUGIN_ID);

		// initialize properties for direct access of node.js server (start an
		// internal process)
		initializeNodejsPreferences(node);

		try {
			File tsRepositoryBaseDir = FileLocator.getBundleFile(Platform.getBundle("ts.repository"));
			ITypeScriptRepository defaultRepository = TypeScriptCorePlugin.getTypeScriptRepositoryManager()
					.createDefaultRepository(tsRepositoryBaseDir);

			// Initialize tsc preferences
			initializeTscPreferences(node, defaultRepository);
			// Initialize tsserver preferences
			initializeTsserverPreferences(node, defaultRepository);
		} catch (Exception e) {
			Trace.trace(Trace.SEVERE, "Error while getting the default TypeScript repository", e);
		}
	}

	/**
	 * initialize properties for direct access of node.js server (start an
	 * internal process)
	 * 
	 * @param node
	 */
	private void initializeNodejsPreferences(IEclipsePreferences node) {
		// By default use the embedded Node.js install (if exists)
		if (!useBundledNodeJsEmbedded(node)) {
			// Use native node.js install in case there is no embedded install.
			node.putBoolean(TypeScriptCorePreferenceConstants.USE_NODEJS_EMBEDDED, false);
			node.put(TypeScriptCorePreferenceConstants.NODEJS_PATH, IDENodejsProcessHelper.getNodejsPath());
		}
	}

	private static boolean useBundledNodeJsEmbedded(IEclipsePreferences node) {
		INodejsInstallManager installManager = TypeScriptCorePlugin.getNodejsInstallManager();
		IEmbeddedNodejs[] installs = installManager.getNodejsInstalls();
		for (IEmbeddedNodejs install : installs) {
			node.putBoolean(TypeScriptCorePreferenceConstants.USE_NODEJS_EMBEDDED, true);
			node.put(TypeScriptCorePreferenceConstants.NODEJS_EMBEDDED, install.getId());
			return true;
		}
		return false;
	}

	private void initializeTscPreferences(IEclipsePreferences node, ITypeScriptRepository defaultRepository) {
		node.put(TypeScriptCorePreferenceConstants.TSC_REPOSITORY, defaultRepository.getName());
	}

	private void initializeTsserverPreferences(IEclipsePreferences node, ITypeScriptRepository defaultRepository) {
		node.put(TypeScriptCorePreferenceConstants.TSSERVER_REPOSITORY, defaultRepository.getName());
		node.putBoolean(TypeScriptCorePreferenceConstants.TRACE_ON_CONSOLE, true);
	}

}