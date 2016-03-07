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
import ts.eclipse.ide.core.resources.TypeScriptSettingsHelper;
import ts.eclipse.ide.core.resources.UseSalsa;
import ts.eclipse.ide.internal.core.Trace;
import ts.repository.ITypeScriptRepository;

/**
 * Eclipse preference initializer for TypeScript Core.
 * 
 */
public class TypeScriptCorePreferenceInitializer extends AbstractPreferenceInitializer {

	@Override
	public void initializeDefaultPreferences() {
		IEclipsePreferences node = TypeScriptSettingsHelper.getWorkspacePreferences(TypeScriptCorePlugin.PLUGIN_ID);

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

		// initialize Salsa (use TypeScript Language Service for JavaSCript
		// files)
		initializeSalsa(node);

		// Initialize default path for TypeScript/Salsa nature
		// node.put(TypeScriptCorePreferenceConstants.NATURE_TYPESCRIPT_PATHS,
		// TypeScriptCorePreferenceConstants.DEFAULT_NATURE_TYPESCRIPT_PATHS);
		// node.put(TypeScriptCorePreferenceConstants.NATURE_SALSA_PATHS,
		// TypeScriptCorePreferenceConstants.DEFAULT_NATURE_SALSA_PATHS);
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
			// Use installed node.js in case there is no embedded install.
			node.putBoolean(TypeScriptCorePreferenceConstants.USE_NODEJS_EMBEDDED, false);
			node.put(TypeScriptCorePreferenceConstants.NODEJS_PATH, IDENodejsProcessHelper.getNodejsPath());
		} else {
			node.putBoolean(TypeScriptCorePreferenceConstants.USE_NODEJS_EMBEDDED, true);
			node.put(TypeScriptCorePreferenceConstants.NODEJS_PATH, "");
		}
	}

	private static boolean useBundledNodeJsEmbedded(IEclipsePreferences node) {
		INodejsInstallManager installManager = TypeScriptCorePlugin.getNodejsInstallManager();
		IEmbeddedNodejs[] installs = installManager.getNodejsInstalls();
		for (IEmbeddedNodejs install : installs) {
			node.put(TypeScriptCorePreferenceConstants.NODEJS_EMBEDDED_ID, install.getId());
			return true;
		}
		return false;
	}

	private void initializeTscPreferences(IEclipsePreferences node, ITypeScriptRepository defaultRepository) {
		node.put(TypeScriptCorePreferenceConstants.TSC_EMBEDDED_TYPESCRIPT_ID, defaultRepository.getName());
		node.putBoolean(TypeScriptCorePreferenceConstants.TSC_USE_EMBEDDED_TYPESCRIPT, true);
		node.put(TypeScriptCorePreferenceConstants.TSC_INSTALLED_TYPESCRIPT_PATH, "");
	}

	private void initializeTsserverPreferences(IEclipsePreferences node, ITypeScriptRepository defaultRepository) {
		node.put(TypeScriptCorePreferenceConstants.TSSERVER_EMBEDDED_TYPESCRIPT_ID, defaultRepository.getName());
		node.putBoolean(TypeScriptCorePreferenceConstants.TSSERVER_USE_EMBEDDED_TYPESCRIPT, true);
		node.put(TypeScriptCorePreferenceConstants.TSSERVER_INSTALLED_TYPESCRIPT_PATH, "");
		node.putBoolean(TypeScriptCorePreferenceConstants.TSSERVER_TRACE_ON_CONSOLE, false);
	}

	private void initializeSalsa(IEclipsePreferences node) {
		node.put(TypeScriptCorePreferenceConstants.USE_SALSA_AS_JS_INFERENCE, UseSalsa.WhenNoJSDTNature.name());
	}

}