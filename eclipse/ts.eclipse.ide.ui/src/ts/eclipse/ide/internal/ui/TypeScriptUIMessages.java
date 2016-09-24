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
	public static String Button_newFolder;
	
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

	// Main
	public static String TypeScriptMainPropertyPage_enable_builder_checkbox_label;
	public static String TypeScriptMainPropertyPage_enable_builder_checkbox_description;
	public static String TypeScriptMainPreferencePage_useSalsa;
	public static String TypeScriptMainPreferencePage_useSalsa_Never;
	public static String TypeScriptMainPreferencePage_useSalsa_EveryTime;
	public static String TypeScriptMainPreferencePage_useSalsa_WhenNoJSDTNature;

	public static String PropertyAndPreferencePage_useprojectsettings_label;
	public static String PropertyAndPreferencePage_useworkspacesettings_change;
	public static String PropertyAndPreferencePage_showprojectspecificsettings_label;

	// nodejs
	public static String NodejsConfigurationBlock_nodejs_group_label;
	public static String NodejsConfigurationBlock_embedded_checkbox_label;
	public static String NodejsConfigurationBlock_installed_checkbox_label;
	public static String NodejsConfigurationBlock_nodePath_label;
	// tsserver
	public static String ServerConfigurationBlock_typescript_group_label;
	public static String ServerConfigurationBlock_embedded_checkbox_label;
	public static String ServerConfigurationBlock_installed_checkbox_label;
	public static String ServerConfigurationBlock_traceOnConsole_label;

	// Formatter
	public static String FormatterConfigurationBlock_editorOptions_group_label;
	public static String FormatterPreferencePage_editorOptions_tabSize;
	public static String FormatterPreferencePage_editorOptions_indentSize;
	public static String FormatterPreferencePage_editorOptions_newLineCharacter;
	public static String FormatterPreferencePage_editorOptions_convertTabsToSpaces;
	public static String FormatterConfigurationBlock_formatOptions_group_label;
	public static String FormatterConfigurationBlock_formatOptions_insertSpaceAfterCommaDelimiter;
	public static String FormatterConfigurationBlock_formatOptions_insertSpaceAfterSemicolonInForStatements;
	public static String FormatterConfigurationBlock_formatOptions_insertSpaceBeforeAndAfterBinaryOperators;
	public static String FormatterConfigurationBlock_formatOptions_insertSpaceAfterKeywordsInControlFlowStatements;
	public static String FormatterConfigurationBlock_formatOptions_insertSpaceAfterFunctionKeywordForAnonymousFunctions;
	public static String FormatterConfigurationBlock_formatOptions_insertSpaceAfterOpeningAndBeforeClosingNonemptyParenthesis;
	public static String FormatterConfigurationBlock_formatOptions_insertSpaceAfterOpeningAndBeforeClosingNonemptyBrackets;
	public static String FormatterConfigurationBlock_formatOptions_placeOpenBraceOnNewLineForFunctions;
	public static String FormatterConfigurationBlock_formatOptions_placeOpenBraceOnNewLineForControlBlocks;

	// tsc
	public static String CompilerConfigurationBlock_typescript_group_label;
	public static String CompilerConfigurationBlock_embedded_checkbox_label;
	public static String CompilerConfigurationBlock_installed_checkbox_label;

	public static String ValidationConfigurationBlock_tslintjson_group_label;
	public static String ValidationConfigurationBlock_tslintjson_strategy_DisableTslint;
	public static String ValidationConfigurationBlock_tslintjson_strategy_UseDefaultTslintJson;
	public static String ValidationConfigurationBlock_tslintjson_strategy_UseCustomTslintJson;
	public static String ValidationConfigurationBlock_tslintjson_strategy_SearchForTslintJson;

	public static String ValidationConfigurationBlock_tslint_group_label;
	public static String ValidationConfigurationBlock_embedded_checkbox_label;
	public static String ValidationConfigurationBlock_installed_checkbox_label;

	// Search
	public static String TypeScriptSearchQuery_label;
	public static String TypeScriptSearchQuery_result;

	// Build path
	public static String TypeScriptResources;
	public static String DiscoverBuildPathDialog_title;
	public static String DiscoverBuildPathDialog_message;
	public static String DiscoverBuildPathDialog_SearchBuildPathJob_name;

	// TypeScript builder
	public static String TypeScriptBuilder_Error_title;
	public static String TypeScriptBuilder_enable_Error_message;
	public static String TypeScriptBuilder_disable_Error_message;

	// Outline
	public static String TypeScriptContentOutlinePage_CollapseAllAction_label;
	public static String TypeScriptContentOutlinePage_CollapseAllAction_description;
	public static String TypeScriptContentOutlinePage_CollapseAllAction_tooltip;
	public static String TypeScriptContentOutlinePage_ToggleLinkingAction_label;
	public static String TypeScriptContentOutlinePage_ToggleLinkingAction_description;
	public static String TypeScriptContentOutlinePage_ToggleLinkingAction_tooltip;

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
