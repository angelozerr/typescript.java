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
package ts.eclipse.ide.internal.core;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.osgi.util.NLS;

/**
 * TypeScript Core messages.
 *
 */
public class TypeScriptCoreMessages extends NLS {

	private static final String BUNDLE_NAME = "ts.eclipse.ide.internal.core.TypeScriptCoreMessages"; //$NON-NLS-1$

	private static ResourceBundle fResourceBundle;

	// Job
	public static String SaveProjectPreferencesJob_name;
	public static String SaveProjectPreferencesJob_taskName;

	// tsconfig.json errors while compilation on save
	public static String tsconfig_compileOnSave_disable_error;
	public static String tsconfig_compilation_context_error;
	public static String tsconfig_cannot_use_compileOnSave_with_outFile_error;
	public static String tsconfig_cannot_use_compileOnSave_with_path_mapping_error;

	// Launch
	public static String TypeScriptCompilerLaunchConfigurationDelegate_invalidBuildPath;
	
	public static ResourceBundle getResourceBundle() {
		try {
			if (fResourceBundle == null)
				fResourceBundle = ResourceBundle.getBundle(BUNDLE_NAME);
		} catch (MissingResourceException x) {
			fResourceBundle = null;
		}
		return fResourceBundle;
	}

	static {
		NLS.initializeMessages(BUNDLE_NAME, TypeScriptCoreMessages.class);
	}
}
