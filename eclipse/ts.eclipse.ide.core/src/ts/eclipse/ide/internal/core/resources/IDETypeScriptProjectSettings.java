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

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.PreferenceChangeEvent;

import ts.client.completions.ICompletionEntryMatcher;
import ts.client.format.FormatOptions;
import ts.cmd.tslint.TslintSettingsStrategy;
import ts.eclipse.ide.core.TypeScriptCorePlugin;
import ts.eclipse.ide.core.nodejs.IEmbeddedNodejs;
import ts.eclipse.ide.core.preferences.TypeScriptCorePreferenceConstants;
import ts.eclipse.ide.core.resources.AbstractTypeScriptSettings;
import ts.eclipse.ide.core.resources.IIDETypeScriptProject;
import ts.eclipse.ide.core.resources.IIDETypeScriptProjectSettings;
import ts.eclipse.ide.core.resources.buildpath.ITypeScriptBuildPath;
import ts.eclipse.ide.internal.core.preferences.TypeScriptCorePreferenceInitializer;
import ts.eclipse.ide.internal.core.repository.IDETypeScriptRepositoryManager;
import ts.eclipse.ide.internal.core.resources.buildpath.TypeScriptBuildPath;
import ts.nodejs.NodejsProcess;
import ts.repository.ITypeScriptRepository;
import ts.resources.SynchStrategy;
import ts.utils.StringUtils;

/**
 * IDE TypeScript project settings.
 *
 */
public class IDETypeScriptProjectSettings extends AbstractTypeScriptSettings implements IIDETypeScriptProjectSettings {

	private final IDETypeScriptProject tsProject;
	private SaveProjectPreferencesJob savePreferencesJob;
	private boolean updatingBuildPath;
	private TslintSettingsStrategy tslintStrategy;
	private FormatOptions formatOptions;

	public IDETypeScriptProjectSettings(IDETypeScriptProject tsProject) {
		super(tsProject.getProject(), TypeScriptCorePlugin.PLUGIN_ID);
		this.tsProject = tsProject;
		// Fix embedded TypeScript id preference
		// See https://github.com/angelozerr/typescript.java/issues/121
		TypeScriptCorePreferenceInitializer.fixEmbeddedTypeScriptIdPreference(getProjectPreferences());
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
	public String getNodeVersion() {
		File nodejsFile = getNodejsInstallPath();
		return NodejsProcess.getNodeVersion(nodejsFile);
	}

	@Override
	public SynchStrategy getSynchStrategy() {
		return SynchStrategy.CHANGE;
	}

	@Override
	public File getTscFile() {
		if (super.getBooleanPreferencesValue(TypeScriptCorePreferenceConstants.USE_EMBEDDED_TYPESCRIPT, false)) {
			// Use TypeScript Repository.
			ITypeScriptRepository repository = getRepository(TypeScriptCorePreferenceConstants.EMBEDDED_TYPESCRIPT_ID);
			return (repository != null) ? repository.getTscFile() : null;
		}

		// Use Installed TypScript
		String path = super.getStringPreferencesValue(TypeScriptCorePreferenceConstants.INSTALLED_TYPESCRIPT_PATH,
				null);
		File resolvedPath = resolvePath(path);
		return resolvedPath != null ? IDETypeScriptRepositoryManager.getTscFile(resolvedPath) : null;
	}

	@Override
	public String getTypeScriptVersion() {
		if (super.getBooleanPreferencesValue(TypeScriptCorePreferenceConstants.USE_EMBEDDED_TYPESCRIPT, false)) {
			// Use TypeScript Repository.
			ITypeScriptRepository repository = getRepository(TypeScriptCorePreferenceConstants.EMBEDDED_TYPESCRIPT_ID);
			return (repository != null) ? repository.getTypesScriptVersion() : null;
		}

		// Use Installed TypScript
		String path = super.getStringPreferencesValue(TypeScriptCorePreferenceConstants.INSTALLED_TYPESCRIPT_PATH,
				null);
		File resolvedPath = resolvePath(path);
		File tscFile = resolvedPath != null ? IDETypeScriptRepositoryManager.getTscFile(resolvedPath) : null;
		return tscFile != null
				? IDETypeScriptRepositoryManager.getPackageJsonVersion(tscFile.getParentFile().getParentFile()) : null;
	}

	@Override
	public File getTsserverFile() {
		if (super.getBooleanPreferencesValue(TypeScriptCorePreferenceConstants.USE_EMBEDDED_TYPESCRIPT, false)) {
			// Use TypeScript Repository.
			ITypeScriptRepository repository = getRepository(TypeScriptCorePreferenceConstants.EMBEDDED_TYPESCRIPT_ID);
			return (repository != null) ? repository.getTsserverFile() : null;
		}

		// Use Installed TypScript
		String path = super.getStringPreferencesValue(TypeScriptCorePreferenceConstants.INSTALLED_TYPESCRIPT_PATH,
				null);
		File resolvedPath = resolvePath(path);
		return resolvedPath != null ? IDETypeScriptRepositoryManager.getTsserverFile(resolvedPath) : null;
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
		if (isFormatPreferencesChanged(event)) {
			this.formatOptions = null;
		} else if (isNodejsPreferencesChanged(event)) {
			getTypeScriptProject().disposeServer();
			getTypeScriptProject().disposeCompiler();
			IIDETypeScriptProject tsProject = getTypeScriptProject();
			IDEResourcesManager.getInstance().fireTypeScriptVersionChanged(tsProject, null, getNodeVersion());
		} else if (isTypeScriptRuntimePreferencesChanged(event)) {
			tsProject.disposeCompiler();
			tsProject.disposeServer();
			IIDETypeScriptProject tsProject = getTypeScriptProject();
			IDEResourcesManager.getInstance().fireTypeScriptVersionChanged(tsProject, null, getTypeScriptVersion());
		} else if (isTypeScriptBuildPathPreferencesChanged(event) && !updatingBuildPath) {
			getTypeScriptProject().disposeBuildPath();
		} else if (isTslintPreferencesChanged(event)) {
			this.tslintStrategy = null;
			getTypeScriptProject().disposeTslint();
		}
	}

	private boolean isFormatPreferencesChanged(PreferenceChangeEvent event) {
		return TypeScriptCorePreferenceConstants.EDITOR_OPTIONS_CONVERT_TABS_TO_SPACES.equals(event.getKey())
				|| TypeScriptCorePreferenceConstants.EDITOR_OPTIONS_INDENT_SIZE.equals(event.getKey())
				|| TypeScriptCorePreferenceConstants.EDITOR_OPTIONS_TAB_SIZE.equals(event.getKey())
				|| TypeScriptCorePreferenceConstants.FORMAT_OPTIONS_INSERT_SPACE_AFTER_COMMA_DELIMITER
						.equals(event.getKey())
				|| TypeScriptCorePreferenceConstants.FORMAT_OPTIONS_INSERT_SPACE_AFTER_SEMICOLON_IN_FOR_STATEMENTS
						.equals(event.getKey())
				|| TypeScriptCorePreferenceConstants.FORMAT_OPTIONS_INSERT_SPACE_BEFORE_AND_AFTER_BINARY_OPERATORS
						.equals(event.getKey())
				|| TypeScriptCorePreferenceConstants.FORMAT_OPTIONS_INSERT_SPACE_AFTER_KEYWORDS_IN_CONTROL_FLOW_STATEMENTS
						.equals(event.getKey())
				|| TypeScriptCorePreferenceConstants.FORMAT_OPTIONS_INSERT_SPACE_AFTER_FUNCTION_KEYWORD_FOR_ANONYMOUS_FUNCTIONS
						.equals(event.getKey())
				|| TypeScriptCorePreferenceConstants.FORMAT_OPTIONS_INSERT_SPACE_AFTER_OPENING_AND_BEFORE_CLOSING_NONEMPTY_PARENTHESIS
						.equals(event.getKey())
				|| TypeScriptCorePreferenceConstants.FORMAT_OPTIONS_INSERT_SPACE_AFTER_OPENING_AND_BEFORE_CLOSING_NONEMPTY_BRACKETS
						.equals(event.getKey())
				|| TypeScriptCorePreferenceConstants.FORMAT_OPTIONS_PLACE_OPEN_BRACE_ON_NEW_LINE_FOR_FUNCTIONS
						.equals(event.getKey())
				|| TypeScriptCorePreferenceConstants.FORMAT_OPTIONS_PLACE_OPEN_BRACE_ON_NEW_LINE_FOR_CONTROL_BLOCKS
						.equals(event.getKey());
	}

	private boolean isNodejsPreferencesChanged(PreferenceChangeEvent event) {
		return TypeScriptCorePreferenceConstants.USE_NODEJS_EMBEDDED.equals(event.getKey())
				|| TypeScriptCorePreferenceConstants.NODEJS_EMBEDDED_ID.equals(event.getKey())
				|| TypeScriptCorePreferenceConstants.NODEJS_PATH.equals(event.getKey());
	}

	private boolean isTypeScriptRuntimePreferencesChanged(PreferenceChangeEvent event) {
		return TypeScriptCorePreferenceConstants.USE_EMBEDDED_TYPESCRIPT.equals(event.getKey())
				|| TypeScriptCorePreferenceConstants.EMBEDDED_TYPESCRIPT_ID.equals(event.getKey())
				|| TypeScriptCorePreferenceConstants.INSTALLED_TYPESCRIPT_PATH.equals(event.getKey())
				|| TypeScriptCorePreferenceConstants.TSSERVER_TRACE_ON_CONSOLE.equals(event.getKey());
	}

	private boolean isTslintPreferencesChanged(PreferenceChangeEvent event) {
		return TypeScriptCorePreferenceConstants.TSLINT_STRATEGY.equals(event.getKey())
				|| TypeScriptCorePreferenceConstants.TSLINT_USE_CUSTOM_TSLINTJSON_FILE.equals(event.getKey())
				|| TypeScriptCorePreferenceConstants.TSLINT_USE_EMBEDDED_TYPESCRIPT.equals(event.getKey())
				|| TypeScriptCorePreferenceConstants.TSLINT_EMBEDDED_TYPESCRIPT_ID.equals(event.getKey())
				|| TypeScriptCorePreferenceConstants.TSLINT_INSTALLED_TYPESCRIPT_PATH.equals(event.getKey());
	}

	private boolean isTypeScriptBuildPathPreferencesChanged(PreferenceChangeEvent event) {
		return TypeScriptCorePreferenceConstants.TYPESCRIPT_BUILD_PATH.equals(event.getKey());
	}

	private IDETypeScriptProject getTypeScriptProject() {
		return tsProject;
	}

	public void updateBuildPath(ITypeScriptBuildPath buildPath) {
		IEclipsePreferences preferences = getProjectPreferences();
		preferences.put(TypeScriptCorePreferenceConstants.TYPESCRIPT_BUILD_PATH, buildPath.toString());
		try {
			this.updatingBuildPath = true;
			save();
		} finally {
			this.updatingBuildPath = false;
		}
	}

	private void save() {
		IProject project = getTypeScriptProject().getProject();
		if (savePreferencesJob == null) {
			savePreferencesJob = new SaveProjectPreferencesJob(getProjectPreferences(), project);
		}
		savePreferencesJob.setRule(project.getWorkspace().getRoot());
		savePreferencesJob.schedule();
	}

	public ITypeScriptBuildPath getTypeScriptBuildPath() {
		String buildPaths = getStringPreferencesValue(TypeScriptCorePreferenceConstants.TYPESCRIPT_BUILD_PATH,
				TypeScriptCorePreferenceConstants.DEFAULT_TYPESCRIPT_BUILD_PATH);
		return TypeScriptBuildPath.load(getTypeScriptProject().getProject(), buildPaths);
	}

	@Override
	public ICompletionEntryMatcher getCompletionEntryMatcher() {
		// TODO: support entry matcher with preferences.
		return ICompletionEntryMatcher.LCS;
	}

	public boolean isUseCodeSnippetsOnMethodSuggest() {
		return true;
	}

	// -------------tslint

	@Override
	public File getTslintFile() {
		if (super.getBooleanPreferencesValue(TypeScriptCorePreferenceConstants.TSLINT_USE_EMBEDDED_TYPESCRIPT, false)) {
			// Use TypeScript Repository.
			ITypeScriptRepository repository = getRepository(
					TypeScriptCorePreferenceConstants.TSLINT_EMBEDDED_TYPESCRIPT_ID);
			return (repository != null) ? repository.getTslintFile() : null;
		}

		// Use Installed tslint
		String path = super.getStringPreferencesValue(
				TypeScriptCorePreferenceConstants.TSLINT_INSTALLED_TYPESCRIPT_PATH, null);
		File resolvedPath = resolvePath(path);
		return resolvedPath != null ? IDETypeScriptRepositoryManager.getTslintFile(resolvedPath) : null;
	}

	@Override
	public TslintSettingsStrategy getTslintStrategy() {
		if (tslintStrategy == null) {
			String strategy = super.getStringPreferencesValue(TypeScriptCorePreferenceConstants.TSLINT_STRATEGY,
					TslintSettingsStrategy.DisableTslint.name());
			try {
				tslintStrategy = TslintSettingsStrategy.valueOf(strategy);
			} catch (Throwable e) {
				tslintStrategy = TslintSettingsStrategy.DisableTslint;
			}
		}
		return tslintStrategy;
	}

	@Override
	public File getCustomTslintJsonFile() {
		String path = super.getStringPreferencesValue(
				TypeScriptCorePreferenceConstants.TSLINT_USE_CUSTOM_TSLINTJSON_FILE, null);
		return resolvePath(path);
	}

	@Override
	public FormatOptions getFormatOptions() {
		if (formatOptions != null) {
			return formatOptions;
		}
		formatOptions = new FormatOptions();
		// Editor options
		formatOptions.setConvertTabsToSpaces(super.getBooleanPreferencesValue(
				TypeScriptCorePreferenceConstants.EDITOR_OPTIONS_CONVERT_TABS_TO_SPACES,
				TypeScriptCorePreferenceConstants.EDITOR_OPTIONS_CONVERT_TABS_TO_SPACES_DEFAULT));
		formatOptions.setIndentSize(
				super.getIntegerPreferencesValue(TypeScriptCorePreferenceConstants.EDITOR_OPTIONS_INDENT_SIZE,
						TypeScriptCorePreferenceConstants.EDITOR_OPTIONS_INDENT_SIZE_DEFAULT));
		formatOptions
				.setTabSize(super.getIntegerPreferencesValue(TypeScriptCorePreferenceConstants.EDITOR_OPTIONS_TAB_SIZE,
						TypeScriptCorePreferenceConstants.EDITOR_OPTIONS_TAB_SIZE_DEFAULT));
		// Format Options
		formatOptions.setInsertSpaceAfterCommaDelimiter(super.getBooleanPreferencesValue(
				TypeScriptCorePreferenceConstants.FORMAT_OPTIONS_INSERT_SPACE_AFTER_COMMA_DELIMITER,
				TypeScriptCorePreferenceConstants.FORMAT_OPTIONS_INSERT_SPACE_AFTER_COMMA_DELIMITER_DEFAULT));
		formatOptions.setInsertSpaceAfterSemicolonInForStatements(super.getBooleanPreferencesValue(
				TypeScriptCorePreferenceConstants.FORMAT_OPTIONS_INSERT_SPACE_AFTER_SEMICOLON_IN_FOR_STATEMENTS,
				TypeScriptCorePreferenceConstants.FORMAT_OPTIONS_INSERT_SPACE_AFTER_SEMICOLON_IN_FOR_STATEMENTS_DEFAULT));
		formatOptions.setInsertSpaceBeforeAndAfterBinaryOperators(super.getBooleanPreferencesValue(
				TypeScriptCorePreferenceConstants.FORMAT_OPTIONS_INSERT_SPACE_BEFORE_AND_AFTER_BINARY_OPERATORS,
				TypeScriptCorePreferenceConstants.FORMAT_OPTIONS_INSERT_SPACE_BEFORE_AND_AFTER_BINARY_OPERATORS_DEFAULT));
		formatOptions.setInsertSpaceAfterKeywordsInControlFlowStatements(super.getBooleanPreferencesValue(
				TypeScriptCorePreferenceConstants.FORMAT_OPTIONS_INSERT_SPACE_AFTER_KEYWORDS_IN_CONTROL_FLOW_STATEMENTS,
				TypeScriptCorePreferenceConstants.FORMAT_OPTIONS_INSERT_SPACE_AFTER_KEYWORDS_IN_CONTROL_FLOW_STATEMENTS_DEFAULT));
		formatOptions.setInsertSpaceAfterFunctionKeywordForAnonymousFunctions(super.getBooleanPreferencesValue(
				TypeScriptCorePreferenceConstants.FORMAT_OPTIONS_INSERT_SPACE_AFTER_FUNCTION_KEYWORD_FOR_ANONYMOUS_FUNCTIONS,
				TypeScriptCorePreferenceConstants.FORMAT_OPTIONS_INSERT_SPACE_AFTER_FUNCTION_KEYWORD_FOR_ANONYMOUS_FUNCTIONS_DEFAULT));
		formatOptions.setInsertSpaceAfterOpeningAndBeforeClosingNonemptyParenthesis(super.getBooleanPreferencesValue(
				TypeScriptCorePreferenceConstants.FORMAT_OPTIONS_INSERT_SPACE_AFTER_OPENING_AND_BEFORE_CLOSING_NONEMPTY_PARENTHESIS,
				TypeScriptCorePreferenceConstants.FORMAT_OPTIONS_INSERT_SPACE_AFTER_OPENING_AND_BEFORE_CLOSING_NONEMPTY_PARENTHESIS_DEFAULT));
		formatOptions.setInsertSpaceAfterOpeningAndBeforeClosingNonemptyBrackets(super.getBooleanPreferencesValue(
				TypeScriptCorePreferenceConstants.FORMAT_OPTIONS_INSERT_SPACE_AFTER_OPENING_AND_BEFORE_CLOSING_NONEMPTY_BRACKETS,
				TypeScriptCorePreferenceConstants.FORMAT_OPTIONS_INSERT_SPACE_AFTER_OPENING_AND_BEFORE_CLOSING_NONEMPTY_BRACKETS_DEFAULT));
		formatOptions.setPlaceOpenBraceOnNewLineForFunctions(super.getBooleanPreferencesValue(
				TypeScriptCorePreferenceConstants.FORMAT_OPTIONS_PLACE_OPEN_BRACE_ON_NEW_LINE_FOR_FUNCTIONS,
				TypeScriptCorePreferenceConstants.FORMAT_OPTIONS_PLACE_OPEN_BRACE_ON_NEW_LINE_FOR_FUNCTIONS_DEFAULT));
		formatOptions.setPlaceOpenBraceOnNewLineForControlBlocks(super.getBooleanPreferencesValue(
				TypeScriptCorePreferenceConstants.FORMAT_OPTIONS_PLACE_OPEN_BRACE_ON_NEW_LINE_FOR_CONTROL_BLOCKS,
				TypeScriptCorePreferenceConstants.FORMAT_OPTIONS_PLACE_OPEN_BRACE_ON_NEW_LINE_FOR_CONTROL_BLOCKS_DEFAULT));
		return formatOptions;
	}
}
