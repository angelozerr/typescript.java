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
package ts.eclipse.ide.jsdt.internal.ui;

import ts.eclipse.ide.jsdt.ui.PreferenceConstants;

/**
 * Defines the constants used in the <code>org.eclipse.ui.themes</code>
 * extension contributed by this plug-in.
 * 
 * 
 */
public interface ITypeScriptThemeConstants {

	String ID_PREFIX = JSDTTypeScriptUIPlugin.PLUGIN_ID + "."; //$NON-NLS-1$

	/**
	 * A theme constant that holds the color used to render JSX tag border
	 * constants.
	 */
	public final String EDITOR_TYPESCRIPT_DECORATOR_COLOR = ID_PREFIX
			+ PreferenceConstants.EDITOR_TYPESCRIPT_DECORATOR_COLOR;

	/**
	 * A theme constant that holds the color used to render JSX tag border
	 * constants.
	 */
	public final String EDITOR_JSX_TAG_BORDER_COLOR = ID_PREFIX + PreferenceConstants.EDITOR_JSX_TAG_BORDER_COLOR;

	/**
	 * A theme constant that holds the color used to render JSX tag name
	 * constants.
	 */
	public final String EDITOR_JSX_TAG_NAME_COLOR = ID_PREFIX + PreferenceConstants.EDITOR_JSX_TAG_NAME_COLOR;

	/**
	 * A theme constant that holds the color used to render JSX tag attribute
	 * name constants.
	 */
	public final String EDITOR_JSX_TAG_ATTRIBUTE_NAME_COLOR = ID_PREFIX
			+ PreferenceConstants.EDITOR_JSX_TAG_ATTRIBUTE_NAME_COLOR;

	/**
	 * A theme constant that holds the color used to render JSX tag attribute
	 * value constants.
	 */
	public final String EDITOR_JSX_TAG_ATTRIBUTE_VALUE_COLOR = ID_PREFIX
			+ PreferenceConstants.EDITOR_JSX_TAG_ATTRIBUTE_VALUE_COLOR;

}
