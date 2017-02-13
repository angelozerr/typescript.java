/**
 *  Copyright (c) 2013-2016 Angelo ZERR.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 *  Lorenzo Dalla Vecchia <lorenzo.dallavecchia@webratio.com> - added save actions
 */
package ts.eclipse.ide.ui.preferences;

/**
 * TypeScript preferences UI constants.
 * 
 */
public class TypeScriptUIPreferenceConstants {

	// TextMate

	/**
	 * True is TextMate must be used to colorize TypeScript, JSX files and false otherwise.
	 */
	public final static String USE_TEXMATE_FOR_SYNTAX_COLORING = "useTextMateForSyntaxColoring";
	
	// Editor Save Actions

	/**
	 * True if saving an editor should automatically perform some actions.
	 */
	public static final String EDITOR_SAVE_ACTIONS = "editorSaveActions"; // $NON-NLS-1$

	/**
	 * True if the editor save actions should include formatting the source
	 * code.
	 */
	public static final String EDITOR_SAVE_ACTIONS_FORMAT = "editorSaveActions.format"; // $NON-NLS-1$

	private TypeScriptUIPreferenceConstants() {
	}
}
