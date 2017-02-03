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

	public static final String TSSERVER_TRACE_ON_CONSOLE = "tsserverTraceOnConsole"; //$NON-NLS-1$

	public static final String TSSERVER_EMULATE_PLUGINS = "tsserverEmulatePlugins"; //$NON-NLS-1$

	// Install @types

	public static final String INSTALL_TYPES_ENABLE_TELEMETRY = "installTypes.enableTelemetry"; //$NON-NLS-1$

	public static final String INSTALL_TYPES_DISABLE_ATA = "installTypes.disableAutomaticTypingAcquisition"; //$NON-NLS-1$

	// tsc

	public static final String USE_EMBEDDED_TYPESCRIPT = "useEmbeddedTypeScript"; //$NON-NLS-1$

	public static final String EMBEDDED_TYPESCRIPT_ID = "embeddedTypeScriptId"; //$NON-NLS-1$

	public static final String INSTALLED_TYPESCRIPT_PATH = "installedTypeScriptPath"; //$NON-NLS-1$

	// tslint

	public static final String TSLINT_STRATEGY = "tslintStrategy"; //$NON-NLS-1$

	public static final String TSLINT_USE_CUSTOM_TSLINTJSON_FILE = "tslintUseCustomTslingJsonFile"; //$NON-NLS-1$

	public static final String TSLINT_USE_EMBEDDED_TYPESCRIPT = "tslintUseEmbeddedTypeScript"; //$NON-NLS-1$

	public static final String TSLINT_EMBEDDED_TYPESCRIPT_ID = "tslintEmbeddedTypeScriptId"; //$NON-NLS-1$

	public static final String TSLINT_INSTALLED_TYPESCRIPT_PATH = "tslintInstalledTypeScriptPath"; //$NON-NLS-1$

	// Salsa

	public static final String USE_SALSA_AS_JS_INFERENCE = "useSalsaAsJSInference"; //$NON-NLS-1$

	// TypeScript build path

	public static final String TYPESCRIPT_BUILD_PATH = "typeScriptBuildPath"; //$NON-NLS-1$

	public static final String DEFAULT_TYPESCRIPT_BUILD_PATH = new DefaultTypeScriptBuildPath().toString(); // $NON-NLS-1$

	// Editor Options

	public static final String EDITOR_OPTIONS_CONVERT_TABS_TO_SPACES = "editorOptionsConvertTabsToSpaces"; // $NON-NLS-1$

	public static final boolean EDITOR_OPTIONS_CONVERT_TABS_TO_SPACES_DEFAULT = true; // $NON-NLS-1$

	public static final String EDITOR_OPTIONS_INDENT_SIZE = "editorOptionsIndentSize"; // $NON-NLS-1$

	public static final int EDITOR_OPTIONS_INDENT_SIZE_DEFAULT = 4; // $NON-NLS-1$

	public static final String EDITOR_OPTIONS_TAB_SIZE = "editorOptionsTabSize"; // $NON-NLS-1$

	public static final int EDITOR_OPTIONS_TAB_SIZE_DEFAULT = 4; // $NON-NLS-1$

	// Format Options

	public static final String FORMAT_OPTIONS_INSERT_SPACE_AFTER_COMMA_DELIMITER = "insertSpaceAfterCommaDelimiter"; // $NON-NLS-1$

	public static final boolean FORMAT_OPTIONS_INSERT_SPACE_AFTER_COMMA_DELIMITER_DEFAULT = true; // $NON-NLS-1$

	public static final String FORMAT_OPTIONS_INSERT_SPACE_AFTER_SEMICOLON_IN_FOR_STATEMENTS = "insertSpaceAfterSemicolonInForStatements"; // $NON-NLS-1$

	public static final boolean FORMAT_OPTIONS_INSERT_SPACE_AFTER_SEMICOLON_IN_FOR_STATEMENTS_DEFAULT = true; // $NON-NLS-1$

	public static final String FORMAT_OPTIONS_INSERT_SPACE_BEFORE_AND_AFTER_BINARY_OPERATORS = "insertSpaceBeforeAndAfterBinaryOperators"; // $NON-NLS-1$

	public static final boolean FORMAT_OPTIONS_INSERT_SPACE_BEFORE_AND_AFTER_BINARY_OPERATORS_DEFAULT = true; // $NON-NLS-1$

	public static final String FORMAT_OPTIONS_INSERT_SPACE_AFTER_KEYWORDS_IN_CONTROL_FLOW_STATEMENTS = "insertSpaceAfterKeywordsInControlFlowStatements"; // $NON-NLS-1$

	public static final boolean FORMAT_OPTIONS_INSERT_SPACE_AFTER_KEYWORDS_IN_CONTROL_FLOW_STATEMENTS_DEFAULT = true; // $NON-NLS-1$

	public static final String FORMAT_OPTIONS_INSERT_SPACE_AFTER_FUNCTION_KEYWORD_FOR_ANONYMOUS_FUNCTIONS = "insertSpaceAfterFunctionKeywordForAnonymousFunctions"; // $NON-NLS-1$

	public static final boolean FORMAT_OPTIONS_INSERT_SPACE_AFTER_FUNCTION_KEYWORD_FOR_ANONYMOUS_FUNCTIONS_DEFAULT = false; // $NON-NLS-1$

	public static final String FORMAT_OPTIONS_INSERT_SPACE_AFTER_OPENING_AND_BEFORE_CLOSING_NONEMPTY_PARENTHESIS = "insertSpaceAfterOpeningAndBeforeClosingNonemptyParenthesis"; // $NON-NLS-1$

	public static final boolean FORMAT_OPTIONS_INSERT_SPACE_AFTER_OPENING_AND_BEFORE_CLOSING_NONEMPTY_PARENTHESIS_DEFAULT = true; // $NON-NLS-1$

	public static final String FORMAT_OPTIONS_INSERT_SPACE_AFTER_OPENING_AND_BEFORE_CLOSING_NONEMPTY_BRACKETS = "insertSpaceAfterOpeningAndBeforeClosingNonemptyBrackets"; // $NON-NLS-1$

	public static final boolean FORMAT_OPTIONS_INSERT_SPACE_AFTER_OPENING_AND_BEFORE_CLOSING_NONEMPTY_BRACKETS_DEFAULT = false; // $NON-NLS-1$

	public static final String FORMAT_OPTIONS_PLACE_OPEN_BRACE_ON_NEW_LINE_FOR_FUNCTIONS = "placeOpenBraceOnNewLineForFunctions"; // $NON-NLS-1$

	public static final boolean FORMAT_OPTIONS_PLACE_OPEN_BRACE_ON_NEW_LINE_FOR_FUNCTIONS_DEFAULT = false; // $NON-NLS-1$

	public static final String FORMAT_OPTIONS_PLACE_OPEN_BRACE_ON_NEW_LINE_FOR_CONTROL_BLOCKS = "placeOpenBraceOnNewLineForControlBlocks"; // $NON-NLS-1$

	public static final boolean FORMAT_OPTIONS_PLACE_OPEN_BRACE_ON_NEW_LINE_FOR_CONTROL_BLOCKS_DEFAULT = false; // $NON-NLS-1$

	private TypeScriptCorePreferenceConstants() {
	}
}
