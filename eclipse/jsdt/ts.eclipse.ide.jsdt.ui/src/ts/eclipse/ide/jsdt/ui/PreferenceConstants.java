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
package ts.eclipse.ide.jsdt.ui;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.ui.PlatformUI;

import ts.eclipse.ide.jsdt.internal.ui.ITypeScriptThemeConstants;
import ts.eclipse.ide.jsdt.internal.ui.JSDTTypeScriptUIPlugin;
import ts.eclipse.ide.jsdt.internal.ui.text.ITypeScriptColorConstants;
import ts.eclipse.ide.jsdt.internal.ui.text.jsx.IJSXColorConstants;

/**
 * Preferences constants for JSDT TypeScript UI.
 *
 */
public class PreferenceConstants {

	/**
	 * Preference key suffix for bold text style preference keys.
	 * 
	 * 
	 */
	public static final String EDITOR_BOLD_SUFFIX = org.eclipse.wst.jsdt.ui.PreferenceConstants.EDITOR_BOLD_SUFFIX;

	/**
	 * Preference key suffix for italic text style preference keys.
	 * 
	 * 
	 */
	public static final String EDITOR_ITALIC_SUFFIX = org.eclipse.wst.jsdt.ui.PreferenceConstants.EDITOR_ITALIC_SUFFIX;

	/**
	 * A named preference that holds the color used to render JSX tag border.
	 * <p>
	 * Value is of type <code>String</code>. A RGB color value encoded as a
	 * string using class <code>PreferenceConverter</code>
	 * </p>
	 * 
	 * @see org.eclipse.jface.resource.StringConverter
	 * @see org.eclipse.jface.preference.PreferenceConverter
	 */
	public final static String EDITOR_JSX_TAG_BORDER_COLOR = IJSXColorConstants.TAG_BORDER;

	/**
	 * A named preference that controls whether JSX tag border are rendered in
	 * bold.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 */
	public final static String EDITOR_JSX_TAG_BORDER_BOLD = IJSXColorConstants.TAG_BORDER + EDITOR_BOLD_SUFFIX;

	/**
	 * A named preference that controls whether JSX tag border are rendered in
	 * italic.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 * 
	 * 
	 */
	public final static String EDITOR_JSX_TAG_BORDER_ITALIC = IJSXColorConstants.TAG_BORDER + EDITOR_ITALIC_SUFFIX;

	/**
	 * A named preference that holds the color used to render JSX tag name.
	 * <p>
	 * Value is of type <code>String</code>. A RGB color value encoded as a
	 * string using class <code>PreferenceConverter</code>
	 * </p>
	 * 
	 * @see org.eclipse.jface.resource.StringConverter
	 * @see org.eclipse.jface.preference.PreferenceConverter
	 */
	public final static String EDITOR_JSX_TAG_NAME_COLOR = IJSXColorConstants.TAG_NAME;

	/**
	 * A named preference that controls whether JSX tag name are rendered in
	 * bold.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 */
	public final static String EDITOR_JSX_TAG_NAME_BOLD = IJSXColorConstants.TAG_NAME + EDITOR_BOLD_SUFFIX;

	/**
	 * A named preference that controls whether JSX tag name are rendered in
	 * italic.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 * 
	 * 
	 */
	public final static String EDITOR_JSX_TAG_NAME_ITALIC = IJSXColorConstants.TAG_NAME + EDITOR_ITALIC_SUFFIX;

	/**
	 * A named preference that holds the color used to render JSX tag attribute
	 * name.
	 * <p>
	 * Value is of type <code>String</code>. A RGB color value encoded as a
	 * string using class <code>PreferenceConverter</code>
	 * </p>
	 * 
	 * @see org.eclipse.jface.resource.StringConverter
	 * @see org.eclipse.jface.preference.PreferenceConverter
	 */
	public final static String EDITOR_JSX_TAG_ATTRIBUTE_NAME_COLOR = IJSXColorConstants.TAG_ATTRIBUTE_NAME;

	/**
	 * A named preference that controls whether JSX tag attribute name are
	 * rendered in bold.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 */
	public final static String EDITOR_JSX_TAG_ATTRIBUTE_NAME_BOLD = IJSXColorConstants.TAG_ATTRIBUTE_NAME
			+ EDITOR_BOLD_SUFFIX;

	/**
	 * A named preference that controls whether JSX tag attribute name rendered
	 * in italic.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 * 
	 */
	public final static String EDITOR_JSX_TAG_ATTRIBUTE_NAME_ITALIC = IJSXColorConstants.TAG_ATTRIBUTE_NAME
			+ EDITOR_ITALIC_SUFFIX;

	/**
	 * A named preference that holds the color used to render JSX tag attribute
	 * value.
	 * <p>
	 * Value is of type <code>String</code>. A RGB color value encoded as a
	 * string using class <code>PreferenceConverter</code>
	 * </p>
	 * 
	 * @see org.eclipse.jface.resource.StringConverter
	 * @see org.eclipse.jface.preference.PreferenceConverter
	 */
	public final static String EDITOR_JSX_TAG_ATTRIBUTE_VALUE_COLOR = IJSXColorConstants.TAG_ATTRIBUTE_VALUE;

	/**
	 * A named preference that controls whether JSX tag attribute value are
	 * rendered in bold.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 */
	public final static String EDITOR_JSX_TAG_ATTRIBUTE_VALUE_BOLD = IJSXColorConstants.TAG_ATTRIBUTE_VALUE
			+ EDITOR_BOLD_SUFFIX;

	/**
	 * A named preference that controls whether JSX tag attribute value rendered
	 * in italic.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 * 
	 * 
	 */
	public final static String EDITOR_JSX_TAG_ATTRIBUTE_VALUE_ITALIC = IJSXColorConstants.TAG_ATTRIBUTE_VALUE
			+ EDITOR_ITALIC_SUFFIX;

	/**
	 * A named preference that holds the color used to render TypeScript
	 * decorator.
	 * 
	 * <p>
	 * Value is of type <code>String</code>. A RGB color value encoded as a
	 * string using class <code>PreferenceConverter</code>
	 * </p>
	 * 
	 * @see org.eclipse.jface.resource.StringConverter
	 * @see org.eclipse.jface.preference.PreferenceConverter
	 */
	public final static String EDITOR_TYPESCRIPT_DECORATOR_COLOR = ITypeScriptColorConstants.DECORATOR;

	/**
	 * A named preference that controls whether TypeScript decorator are
	 * rendered in bold.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 */
	public final static String EDITOR_TYPESCRIPT_DECORATOR_BOLD = ITypeScriptColorConstants.DECORATOR
			+ EDITOR_BOLD_SUFFIX;

	/**
	 * A named preference that controls whether TypeScript decorator rendered in
	 * italic.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 * 
	 * 
	 */
	public final static String EDITOR_TYPESCRIPT_DECORATOR_ITALIC = ITypeScriptColorConstants.DECORATOR
			+ EDITOR_ITALIC_SUFFIX;

	/**
	 * Initializes the given preference store with the default values.
	 * 
	 * @param store
	 *            the preference store to be initialized
	 * 
	 */
	public static void initializeDefaultValues(IPreferenceStore store) {
		ColorRegistry registry = PlatformUI.getWorkbench().getThemeManager().getCurrentTheme().getColorRegistry();

		// JSX tag border
		setDefaultAndFireEvent(store, PreferenceConstants.EDITOR_TYPESCRIPT_DECORATOR_COLOR,
				findRGB(registry, ITypeScriptThemeConstants.EDITOR_TYPESCRIPT_DECORATOR_COLOR, new RGB(100, 100, 100)));
		store.setDefault(PreferenceConstants.EDITOR_TYPESCRIPT_DECORATOR_BOLD, false);
		store.setDefault(PreferenceConstants.EDITOR_TYPESCRIPT_DECORATOR_ITALIC, false);

		// JSX tag border
		setDefaultAndFireEvent(store, PreferenceConstants.EDITOR_JSX_TAG_BORDER_COLOR,
				findRGB(registry, ITypeScriptThemeConstants.EDITOR_JSX_TAG_BORDER_COLOR, new RGB(0, 128, 128)));
		store.setDefault(PreferenceConstants.EDITOR_JSX_TAG_BORDER_BOLD, false);
		store.setDefault(PreferenceConstants.EDITOR_JSX_TAG_BORDER_ITALIC, false);

		// JSX tag name
		setDefaultAndFireEvent(store, PreferenceConstants.EDITOR_JSX_TAG_NAME_COLOR,
				findRGB(registry, ITypeScriptThemeConstants.EDITOR_JSX_TAG_NAME_COLOR, new RGB(63, 127, 127)));
		store.setDefault(PreferenceConstants.EDITOR_JSX_TAG_NAME_BOLD, false);
		store.setDefault(PreferenceConstants.EDITOR_JSX_TAG_NAME_ITALIC, false);

		// JSX tag attribute name
		setDefaultAndFireEvent(store, PreferenceConstants.EDITOR_JSX_TAG_ATTRIBUTE_NAME_COLOR,
				findRGB(registry, ITypeScriptThemeConstants.EDITOR_JSX_TAG_ATTRIBUTE_NAME_COLOR, new RGB(127, 0, 127)));
		store.setDefault(PreferenceConstants.EDITOR_JSX_TAG_ATTRIBUTE_NAME_BOLD, false);
		store.setDefault(PreferenceConstants.EDITOR_JSX_TAG_ATTRIBUTE_NAME_ITALIC, false);

		// JSX tag attribute value
		setDefaultAndFireEvent(store, PreferenceConstants.EDITOR_JSX_TAG_ATTRIBUTE_VALUE_COLOR,
				findRGB(registry, ITypeScriptThemeConstants.EDITOR_JSX_TAG_ATTRIBUTE_VALUE_COLOR, new RGB(42, 0, 255)));
		store.setDefault(PreferenceConstants.EDITOR_JSX_TAG_ATTRIBUTE_VALUE_BOLD, false);
		store.setDefault(PreferenceConstants.EDITOR_JSX_TAG_ATTRIBUTE_VALUE_ITALIC, true);
	}

	/**
	 * Sets the default value and fires a property change event if necessary.
	 * 
	 * @param store
	 *            the preference store
	 * @param key
	 *            the preference key
	 * @param newValue
	 *            the new value
	 * 
	 */
	private static void setDefaultAndFireEvent(IPreferenceStore store, String key, RGB newValue) {
		RGB oldValue = null;
		if (store.isDefault(key))
			oldValue = PreferenceConverter.getDefaultColor(store, key);

		PreferenceConverter.setDefault(store, key, newValue);

		if (oldValue != null && !oldValue.equals(newValue))
			store.firePropertyChangeEvent(key, oldValue, newValue);
	}

	/**
	 * Returns the RGB for the given key in the given color registry.
	 * 
	 * @param registry
	 *            the color registry
	 * @param key
	 *            the key for the constant in the registry
	 * @param defaultRGB
	 *            the default RGB if no entry is found
	 * @return RGB the RGB
	 * 
	 */
	private static RGB findRGB(ColorRegistry registry, String key, RGB defaultRGB) {
		RGB rgb = registry.getRGB(key);
		if (rgb != null)
			return rgb;
		return defaultRGB;
	}

	/**
	 * Returns the JDT-UI preference store.
	 * 
	 * @return the JDT-UI preference store
	 */
	public static IPreferenceStore getPreferenceStore() {
		return JSDTTypeScriptUIPlugin.getDefault().getPreferenceStore();
	}
}
