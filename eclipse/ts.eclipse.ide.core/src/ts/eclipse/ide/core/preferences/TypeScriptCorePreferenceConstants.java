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
package ts.eclipse.ide.core.preferences;

import ts.eclipse.ide.internal.core.resources.buildpath.DefaultTypeScriptBuildPath;

/**
 * TypeScript preferences core constants.
 * 
 */
public class TypeScriptCorePreferenceConstants {

	// Node.js

	public static final String USE_NODEJS_EMBEDDED = "useNodeJSEmbedded"; //$NON-NLS-1$

	public static final String NODEJS_EMBEDDED_ID = "nodeJSEmbedded"; //$NON-NLS-1$

	public static final String NODEJS_PATH = "nodeJSPath"; //$NON-NLS-1$

	// tsserver

	public static final String TSSERVER_USE_EMBEDDED_TYPESCRIPT = "tsserverUseEmbeddedTypeScript"; //$NON-NLS-1$

	public static final String TSSERVER_EMBEDDED_TYPESCRIPT_ID = "tsserverEmbeddedTypeScriptid"; //$NON-NLS-1$

	public static final String TSSERVER_INSTALLED_TYPESCRIPT_PATH = "tsserverInstalledTypeScriptPath"; //$NON-NLS-1$

	public static final String TSSERVER_TRACE_ON_CONSOLE = "tsserverTraceOnConsole"; //$NON-NLS-1$

	// tsc

	public static final String TSC_USE_EMBEDDED_TYPESCRIPT = "tscUseEmbeddedTypeScript"; //$NON-NLS-1$

	public static final String TSC_EMBEDDED_TYPESCRIPT_ID = "tscEmbeddedTypeScriptId"; //$NON-NLS-1$

	public static final String TSC_INSTALLED_TYPESCRIPT_PATH = "tscInstalledTypeScriptPath"; //$NON-NLS-1$

	// Salsa

	public static final String USE_SALSA_AS_JS_INFERENCE = "useSalsaAsJSInference"; //$NON-NLS-1$

	// TypeScript/Salsa nature paths

	// public static final String NATURE_TYPESCRIPT_PATHS =
	// "natureTypescriptPaths"; //$NON-NLS-1$
	//
	// public static final String DEFAULT_NATURE_TYPESCRIPT_PATHS =
	// TSCONFIG_JSON + ",src/" + TSCONFIG_JSON; //$NON-NLS-1$
	//
	// public static final String NATURE_SALSA_PATHS = "natureSalsaPaths";
	// //$NON-NLS-1$
	//
	// public static final String DEFAULT_NATURE_SALSA_PATHS = JSCONFIG_JSON +
	// ",src/" + JSCONFIG_JSON; //$NON-NLS-1$

	public static final String TYPESCRIPT_BUILD_PATH = "typeScriptBuildPath"; //$NON-NLS-1$

	public static final String DEFAULT_TYPESCRIPT_BUILD_PATH = new DefaultTypeScriptBuildPath().toString(); // $NON-NLS-1$

	private TypeScriptCorePreferenceConstants() {
	}
}
