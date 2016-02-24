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

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;

import ts.eclipse.ide.core.TypeScriptCorePlugin;
import ts.eclipse.ide.core.nodejs.IDENodejsProcessHelper;
import ts.eclipse.ide.core.nodejs.INodejsInstall;
import ts.eclipse.ide.core.nodejs.INodejsInstallManager;
import ts.eclipse.ide.internal.core.nodejs.NodejsInstall;
import ts.eclipse.ide.internal.core.resources.IDETypeScriptProjectSettings;

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
		// Initialize tsserver
		initializeTsserverPreferences(node);
	}

	/**
	 * initialize properties for direct access of node.js server (start an
	 * internal process)
	 * 
	 * @param node
	 */
	private void initializeNodejsPreferences(IEclipsePreferences node) {
		// By default use the embedded Node.js install (if exists)
		if (!useBundledNodeJsInstall(node)) {
			// Use native node.js install in case there is no embedded install.
			node.put(TypeScriptCorePreferenceConstants.NODEJS_INSTALL, NodejsInstall.NODE_NATIVE);
			node.put(TypeScriptCorePreferenceConstants.NODEJS_PATH, IDENodejsProcessHelper.getNodejsPath());
		}
		// timeout to start node.js
		// node.putLong(TypeScriptCoreConstants.NODEJS_TIMEOUT,
		// NodejsTypeScriptHelper.DEFAULT_TIMEOUT);
		// test number to start node.js
		// node.putInt(TypeScriptCoreConstants.NODEJS_TEST_NUMBER,
		// NodejsTypeScriptHelper.DEFAULT_TEST_NUMBER);
	}

	private static boolean useBundledNodeJsInstall(IEclipsePreferences node) {
		INodejsInstallManager installManager = TypeScriptCorePlugin.getNodejsInstallManager();
		INodejsInstall[] installs = installManager.getNodejsInstalls();
		for (INodejsInstall install : installs) {
			if (!install.isNative()) {
				node.put(TypeScriptCorePreferenceConstants.NODEJS_INSTALL, install.getId());
				return true;
			}
		}
		return false;
	}

	private void initializeTsserverPreferences(IEclipsePreferences node) {
		node.putBoolean(TypeScriptCorePreferenceConstants.TRACE_ON_CONSOLE, true);
	}

}