/**
 *  Copyright (c) 2015-2016 Angelo ZERR.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 *  Lorenzo Dalla Vecchia <lorenzo.dallavecchia@webratio.com> - added reconcileControls hook
 */
package ts.eclipse.ide.internal.ui.preferences;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.dialogs.ControlEnableState;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.preferences.IWorkbenchPreferenceContainer;

import ts.eclipse.ide.core.preferences.TypeScriptCorePreferenceConstants;
import ts.eclipse.ide.internal.ui.TypeScriptUIMessages;
import ts.eclipse.ide.ui.preferences.OptionsConfigurationBlock;
import ts.eclipse.ide.ui.preferences.ScrolledPageContent;
import ts.eclipse.ide.ui.widgets.IStatusChangeListener;

/**
 * Formatter configuration block.
 *
 */
public class FormatterConfigurationBlock extends OptionsConfigurationBlock {

	// Editor Options
	private static final Key PREF_EDITOR_OPTIONS_CONVERT_TABS_TO_SPACES = getTypeScriptCoreKey(
			TypeScriptCorePreferenceConstants.EDITOR_OPTIONS_CONVERT_TABS_TO_SPACES);
	private static final Key PREF_EDITOR_OPTIONS_INDENT_SIZE = getTypeScriptCoreKey(
			TypeScriptCorePreferenceConstants.EDITOR_OPTIONS_INDENT_SIZE);
	private static final Key PREF_EDITOR_OPTIONS_TAB_SIZE = getTypeScriptCoreKey(
			TypeScriptCorePreferenceConstants.EDITOR_OPTIONS_TAB_SIZE);

	// Fomat Options
	private static final Key PREF_FORMAT_OPTIONS_INSERT_SPACE_AFTER_COMMA_DELIMITER = getTypeScriptCoreKey(
			TypeScriptCorePreferenceConstants.FORMAT_OPTIONS_INSERT_SPACE_AFTER_COMMA_DELIMITER);
	private static final Key PREF_FORMAT_OPTIONS_INSERT_SPACE_AFTER_SEMICOLON_IN_FOR_STATEMENTS = getTypeScriptCoreKey(
			TypeScriptCorePreferenceConstants.FORMAT_OPTIONS_INSERT_SPACE_AFTER_SEMICOLON_IN_FOR_STATEMENTS);
	private static final Key PREF_FORMAT_OPTIONS_INSERT_SPACE_BEFORE_AND_AFTER_BINARY_OPERATORS = getTypeScriptCoreKey(
			TypeScriptCorePreferenceConstants.FORMAT_OPTIONS_INSERT_SPACE_BEFORE_AND_AFTER_BINARY_OPERATORS);
	private static final Key PREF_FORMAT_OPTIONS_INSERT_SPACE_AFTER_KEYWORDS_IN_CONTROL_FLOW_STATEMENTS = getTypeScriptCoreKey(
			TypeScriptCorePreferenceConstants.FORMAT_OPTIONS_INSERT_SPACE_AFTER_KEYWORDS_IN_CONTROL_FLOW_STATEMENTS);
	private static final Key PREF_FORMAT_OPTIONS_INSERT_SPACE_AFTER_FUNCTION_KEYWORD_FOR_ANONYMOUS_FUNCTIONS = getTypeScriptCoreKey(
			TypeScriptCorePreferenceConstants.FORMAT_OPTIONS_INSERT_SPACE_AFTER_FUNCTION_KEYWORD_FOR_ANONYMOUS_FUNCTIONS);
	private static final Key PREF_FORMAT_OPTIONS_INSERT_SPACE_AFTER_OPENING_AND_BEFORE_CLOSING_NONEMPTY_PARENTHESIS = getTypeScriptCoreKey(
			TypeScriptCorePreferenceConstants.FORMAT_OPTIONS_INSERT_SPACE_AFTER_OPENING_AND_BEFORE_CLOSING_NONEMPTY_PARENTHESIS);
	private static final Key PREF_FORMAT_OPTIONS_INSERT_SPACE_AFTER_OPENING_AND_BEFORE_CLOSING_NONEMPTY_BRACKETS = getTypeScriptCoreKey(
			TypeScriptCorePreferenceConstants.FORMAT_OPTIONS_INSERT_SPACE_AFTER_OPENING_AND_BEFORE_CLOSING_NONEMPTY_BRACKETS);
	private static final Key PREF_FORMAT_OPTIONS_PLACE_OPEN_BRACE_ON_NEW_LINE_FOR_FUNCTIONS = getTypeScriptCoreKey(
			TypeScriptCorePreferenceConstants.FORMAT_OPTIONS_PLACE_OPEN_BRACE_ON_NEW_LINE_FOR_FUNCTIONS);
	private static final Key PREF_FORMAT_OPTIONS_PLACE_OPEN_BRACE_ON_NEW_LINE_FOR_CONTROL_BLOCKS = getTypeScriptCoreKey(
			TypeScriptCorePreferenceConstants.FORMAT_OPTIONS_PLACE_OPEN_BRACE_ON_NEW_LINE_FOR_CONTROL_BLOCKS);
	private Composite controlsComposite;
	private ControlEnableState blockEnableState;

	public FormatterConfigurationBlock(IStatusChangeListener context, IProject project,
			IWorkbenchPreferenceContainer container) {
		super(context, project, getKeys(), container);
		blockEnableState = null;
	}

	private static Key[] getKeys() {
		return new Key[] { PREF_EDITOR_OPTIONS_CONVERT_TABS_TO_SPACES, PREF_EDITOR_OPTIONS_INDENT_SIZE,
				PREF_EDITOR_OPTIONS_TAB_SIZE, PREF_FORMAT_OPTIONS_INSERT_SPACE_AFTER_COMMA_DELIMITER,
				PREF_FORMAT_OPTIONS_INSERT_SPACE_AFTER_SEMICOLON_IN_FOR_STATEMENTS,
				PREF_FORMAT_OPTIONS_INSERT_SPACE_BEFORE_AND_AFTER_BINARY_OPERATORS,
				PREF_FORMAT_OPTIONS_INSERT_SPACE_AFTER_KEYWORDS_IN_CONTROL_FLOW_STATEMENTS,
				PREF_FORMAT_OPTIONS_INSERT_SPACE_AFTER_FUNCTION_KEYWORD_FOR_ANONYMOUS_FUNCTIONS,
				PREF_FORMAT_OPTIONS_INSERT_SPACE_AFTER_OPENING_AND_BEFORE_CLOSING_NONEMPTY_PARENTHESIS,
				PREF_FORMAT_OPTIONS_INSERT_SPACE_AFTER_OPENING_AND_BEFORE_CLOSING_NONEMPTY_BRACKETS,
				PREF_FORMAT_OPTIONS_PLACE_OPEN_BRACE_ON_NEW_LINE_FOR_FUNCTIONS,
				PREF_FORMAT_OPTIONS_PLACE_OPEN_BRACE_ON_NEW_LINE_FOR_CONTROL_BLOCKS };
	}

	public void enablePreferenceContent(boolean enable) {
		if (controlsComposite != null && !controlsComposite.isDisposed()) {
			if (enable) {
				if (blockEnableState != null) {
					blockEnableState.restore();
					blockEnableState = null;
				}
			} else {
				if (blockEnableState == null) {
					blockEnableState = ControlEnableState.disable(controlsComposite);
				}
			}
		}
	}

	@Override
	protected Composite createUI(Composite parent) {
		final ScrolledPageContent pageContent = new ScrolledPageContent(parent);
		Composite composite = pageContent.getBody();
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		composite.setLayout(layout);

		controlsComposite = new Composite(composite, SWT.NONE);
		controlsComposite.setFont(composite.getFont());
		controlsComposite.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, false));

		layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.numColumns = 1;
		controlsComposite.setLayout(layout);

		// editor options
		createEditorOptions(controlsComposite);
		// format options
		createFormatOptions(controlsComposite);
		return pageContent;
	}

	/**
	 * Create editor options.
	 * 
	 * @param parent
	 */
	private void createEditorOptions(Composite parent) {

		Group group = new Group(parent, SWT.NONE);
		group.setText(TypeScriptUIMessages.FormatterConfigurationBlock_editorOptions_group_label);

		GridLayout layout = new GridLayout();
		layout.numColumns = 3;
		group.setLayout(layout);
		group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		// Convert tabs to spaces
		addCheckBox(group, TypeScriptUIMessages.FormatterPreferencePage_editorOptions_convertTabsToSpaces,
				PREF_EDITOR_OPTIONS_CONVERT_TABS_TO_SPACES, new String[] { "true", "false" }, 0);
		// Indent size
		addTextField(group, TypeScriptUIMessages.FormatterPreferencePage_editorOptions_indentSize,
				PREF_EDITOR_OPTIONS_INDENT_SIZE, 0, 0);
		// Tab size
		addTextField(group, TypeScriptUIMessages.FormatterPreferencePage_editorOptions_tabSize,
				PREF_EDITOR_OPTIONS_TAB_SIZE, 0, 0);
	}

	/**
	 * Create format options.
	 * 
	 * @param parent
	 */
	private void createFormatOptions(Composite parent) {

		Group group = new Group(parent, SWT.NONE);
		group.setText(TypeScriptUIMessages.FormatterConfigurationBlock_formatOptions_group_label);

		GridLayout layout = new GridLayout();
		layout.numColumns = 3;
		group.setLayout(layout);
		group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		// Defines space handling after a comma delimiter.
		addCheckBox(group,
				TypeScriptUIMessages.FormatterConfigurationBlock_formatOptions_insertSpaceAfterCommaDelimiter,
				PREF_FORMAT_OPTIONS_INSERT_SPACE_AFTER_COMMA_DELIMITER, new String[] { "true", "false" }, 0);

		// Defines space handling after a semicolon in a for statement.
		addCheckBox(group,
				TypeScriptUIMessages.FormatterConfigurationBlock_formatOptions_insertSpaceAfterSemicolonInForStatements,
				PREF_FORMAT_OPTIONS_INSERT_SPACE_AFTER_SEMICOLON_IN_FOR_STATEMENTS, new String[] { "true", "false" },
				0);

		// Defines space handling after a binary operator.
		addCheckBox(group,
				TypeScriptUIMessages.FormatterConfigurationBlock_formatOptions_insertSpaceBeforeAndAfterBinaryOperators,
				PREF_FORMAT_OPTIONS_INSERT_SPACE_BEFORE_AND_AFTER_BINARY_OPERATORS, new String[] { "true", "false" },
				0);

		// Defines space handling after keywords in control flow statement.
		addCheckBox(group,
				TypeScriptUIMessages.FormatterConfigurationBlock_formatOptions_insertSpaceAfterKeywordsInControlFlowStatements,
				PREF_FORMAT_OPTIONS_INSERT_SPACE_AFTER_KEYWORDS_IN_CONTROL_FLOW_STATEMENTS,
				new String[] { "true", "false" }, 0);

		// Defines space handling after function keyword for anonymous
		// functions.
		addCheckBox(group,
				TypeScriptUIMessages.FormatterConfigurationBlock_formatOptions_insertSpaceAfterFunctionKeywordForAnonymousFunctions,
				PREF_FORMAT_OPTIONS_INSERT_SPACE_AFTER_FUNCTION_KEYWORD_FOR_ANONYMOUS_FUNCTIONS,
				new String[] { "true", "false" }, 0);

		// Defines space handling after opening and before closing non empty
		// parenthesis.
		addCheckBox(group,
				TypeScriptUIMessages.FormatterConfigurationBlock_formatOptions_insertSpaceAfterOpeningAndBeforeClosingNonemptyParenthesis,
				PREF_FORMAT_OPTIONS_INSERT_SPACE_AFTER_OPENING_AND_BEFORE_CLOSING_NONEMPTY_PARENTHESIS,
				new String[] { "true", "false" }, 0);

		// Defines whether an open brace is put onto a new line for functions or
		// not.
		addCheckBox(group,
				TypeScriptUIMessages.FormatterConfigurationBlock_formatOptions_insertSpaceAfterOpeningAndBeforeClosingNonemptyBrackets,
				PREF_FORMAT_OPTIONS_INSERT_SPACE_AFTER_OPENING_AND_BEFORE_CLOSING_NONEMPTY_BRACKETS,
				new String[] { "true", "false" }, 0);

		//  Defines whether an open brace is put onto a new line for functions or not.
		addCheckBox(group,
				TypeScriptUIMessages.FormatterConfigurationBlock_formatOptions_placeOpenBraceOnNewLineForFunctions,
				PREF_FORMAT_OPTIONS_PLACE_OPEN_BRACE_ON_NEW_LINE_FOR_FUNCTIONS, new String[] { "true", "false" }, 0);

		// Defines whether an open brace is put onto a new line for control
		// blocks or not.
		addCheckBox(group,
				TypeScriptUIMessages.FormatterConfigurationBlock_formatOptions_placeOpenBraceOnNewLineForControlBlocks,
				PREF_FORMAT_OPTIONS_PLACE_OPEN_BRACE_ON_NEW_LINE_FOR_CONTROL_BLOCKS, new String[] { "true", "false" },
				0);

	}

	@Override
	protected void validateSettings(Key changedKey, String oldValue, String newValue) {
		if (!areSettingsEnabled()) {
			return;
		}
		if (changedKey != null) {

		}
	}

	@Override
	protected String[] getFullBuildDialogStrings(boolean workspaceSettings) {
		return null;
	}

}
