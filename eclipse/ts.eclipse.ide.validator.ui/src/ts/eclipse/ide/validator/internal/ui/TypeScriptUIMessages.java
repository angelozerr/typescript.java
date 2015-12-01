package ts.eclipse.ide.validator.internal.ui;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.osgi.util.NLS;

public class TypeScriptUIMessages extends NLS {

	private static final String BUNDLE_NAME = "ts.eclipse.ide.internal.ui.TypeScriptUIMessages"; //$NON-NLS-1$

	private static ResourceBundle fResourceBundle;

	// Hyperlink
	public static String TypeScriptHyperlink_typeLabel;
	public static String TypeScriptHyperlink_text;

	// Console
	public static String TypeScriptConsoleJob_name;
	public static String ConsoleTerminateAction_tooltipText;

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
