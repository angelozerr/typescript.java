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
package ts.eclipse.ide.jsdt.ui.actions;

import org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds;

/**
 * Definition ids for TypeScript editor actions.
 *
 */
public interface ITypeScriptEditorActionDefinitionIds extends ITextEditorActionDefinitionIds {

	/**
	 * Action definition ID of the source -> indent action (value
	 * <code>"ts.eclipse.ide.jsdt.ui.edit.text.java.indent"</code>).
	 */
	public static final String INDENT = "ts.eclipse.ide.jsdt.ui.edit.text.java.indent"; //$NON-NLS-1$

	/**
	 * Action definition ID of the source -> format action (value
	 * <code>"ts.eclipse.ide.ui.edit.text.java.format"</code>).
	 */
	public static final String FORMAT = "ts.eclipse.ide.jsdt.ui.edit.text.java.format"; //$NON-NLS-1$

	/**
	 * Action definition ID of the source -> comment action (value
	 * <code>"ts.eclipse.ide.jsdt.ui.edit.text.java.comment"</code>).
	 */
	public static final String COMMENT = "ts.eclipse.ide.jsdt.ui.edit.text.java.comment"; //$NON-NLS-1$

	/**
	 * Action definition ID of the source -> uncomment action (value
	 * <code>"ts.eclipse.ide.jsdt.ui.edit.text.java.uncomment"</code>).
	 */
	public static final String UNCOMMENT = "ts.eclipse.ide.jsdt.ui.edit.text.java.uncomment"; //$NON-NLS-1$

	/**
	 * Action definition ID of the source -> toggle comment action (value
	 * <code>"ts.eclipse.ide.jsdt.ui.edit.text.java.toggle.comment"</code>).
	 * 
	 */
	public static final String TOGGLE_COMMENT = "ts.eclipse.ide.jsdt.ui.edit.text.java.toggle.comment"; //$NON-NLS-1$

	/**
	 * Action definition ID of the source -> add block comment action (value
	 * <code>"ts.eclipse.ide.jsdt.ui.edit.text.java.add.block.comment"</code>).
	 * 
	 */
	public static final String ADD_BLOCK_COMMENT = "ts.eclipse.ide.jsdt.ui.edit.text.java.add.block.comment"; //$NON-NLS-1$

	/**
	 * Action definition ID of the source -> remove block comment action (value
	 * <code>"ts.eclipse.ide.jsdt.ui.edit.text.java.remove.block.comment"</code>).
	 * 
	 */
	public static final String REMOVE_BLOCK_COMMENT = "ts.eclipse.ide.jsdt.ui.edit.text.java.remove.block.comment"; //$NON-NLS-1$

	/**
	 * Action definition ID of the search -> references in project action (value
	 * <code>"ts.eclipse.ide.jsdt.ui.edit.text.java.search.references.in.project"</code>
	 * ).
	 */
	public static final String SEARCH_REFERENCES_IN_PROJECT = "ts.eclipse.ide.jsdt.ui.edit.text.java.search.references.in.project"; //$NON-NLS-1$

	/**
	 * Action definition ID of the edit -> go to matching bracket action (value
	 * <code>"ts.eclipse.ide.jsdt.ui.edit.text.java.goto.matching.bracket"</code>).
	 *
	 * 
	 */
	public static final String GOTO_MATCHING_BRACKET = "ts.eclipse.ide.jsdt.ui.edit.text.java.goto.matching.bracket"; //$NON-NLS-1$

	/**
	 * Action definition ID of the navigate -> Show Outline action (value
	 * <code>"ts.eclipse.ide.jsdt.ui.edit.text.java.show.outline"</code>).
	 * 
	 * 
	 */
	public static final String SHOW_OUTLINE = "ts.eclipse.ide.jsdt.ui.edit.text.java.show.outline"; //$NON-NLS-1$

	/**
	 * Action definition ID of the navigate -> Show Implementation action (value
	 * <code>"ts.eclipse.ide.jsdt.ui.edit.text.java.open.implementation"</code>).
	 * 
	 * 
	 */
	public static final String OPEN_IMPLEMENTATION = "ts.eclipse.ide.jsdt.ui.edit.text.java.open.implementation"; //$NON-NLS-1$

	/**
	 * Action definition ID of the navigate -> open action (value
	 * <code>"ts.eclipse.ide.jsdt.ui.edit.text.java.open.editor"</code>).
	 */
	public static final String OPEN_EDITOR = "ts.eclipse.ide.jsdt.ui.edit.text.java.open.editor"; //$NON-NLS-1$

	/**
	 * Action definition ID of the refactor -> rename element action (value
	 * <code>"ts.eclipse.ide.jsdt.ui.edit.text.java.rename.element"</code>).
	 */
	public static final String RENAME_ELEMENT = "ts.eclipse.ide.jsdt.ui.edit.text.java.rename.element"; //$NON-NLS-1$

}
