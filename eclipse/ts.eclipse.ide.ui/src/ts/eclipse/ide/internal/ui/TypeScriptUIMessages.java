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
package ts.eclipse.ide.internal.ui;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.osgi.util.NLS;

/**
 * TypeScript UI messages.
 *
 */
public class TypeScriptUIMessages extends NLS {

	private static final String BUNDLE_NAME = "ts.eclipse.ide.internal.ui.TypeScriptUIMessages"; //$NON-NLS-1$

	private static ResourceBundle fResourceBundle;

	// Buttons
	public static String Browse_FileSystem_button;
	public static String Browse_Workspace_button;

	// Errors
	public static String TypeScriptUIPlugin_internal_error;

	// Hyperlink
	public static String TypeScriptHyperlink_typeLabel;
	public static String TypeScriptHyperlink_text;

	// Console
	public static String TypeScriptConsoleJob_name;
	public static String ConsoleTerminateAction_tooltipText;

	// Hover
	public static String TypeScriptHover_openDeclaration;

	// Preferences
	public static String TypeScriptMainPreferencePage_useSalsa;
	public static String TypeScriptMainPreferencePage_useSalsa_Never;
	public static String TypeScriptMainPreferencePage_useSalsa_EveryTime;
	public static String TypeScriptMainPreferencePage_useSalsa_WhenNoJSDTNature;

	public static String PropertyAndPreferencePage_useprojectsettings_label;
	public static String PropertyAndPreferencePage_useworkspacesettings_change;
	public static String PropertyAndPreferencePage_showprojectspecificsettings_label;

	public static String NodejsConfigurationBlock_nodejs_group_label;
	public static String NodejsConfigurationBlock_embedded_checkbox_label;
	public static String NodejsConfigurationBlock_installed_checkbox_label;

	public static String ServerConfigurationBlock_typescript_group_label;
	public static String ServerConfigurationBlock_embedded_checkbox_label;
	public static String ServerConfigurationBlock_installed_checkbox_label;
	public static String ServerConfigurationBlock_traceOnConsole_label;

	public static String CompilerConfigurationBlock_typescript_group_label;
	public static String CompilerConfigurationBlock_embedded_checkbox_label;
	public static String CompilerConfigurationBlock_installed_checkbox_label;

	// Search
	public static String TypeScriptSearchQuery_label;
	public static String TypeScriptSearchQuery_result;

	// Build path
	public static String TypeScriptResources;
	public static String DiscoverBuildPathDialog_title;
	public static String DiscoverBuildPathDialog_message;
	public static String DiscoverBuildPathDialog_SearchBuildPathJob_name;

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
		NLS.initializeMessages(BUNDLE_NAME, TypeScriptUIMessages.class);
	}
}
