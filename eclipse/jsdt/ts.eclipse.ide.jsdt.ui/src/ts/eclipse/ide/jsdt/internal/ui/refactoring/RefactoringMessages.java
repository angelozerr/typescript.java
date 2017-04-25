/**
 *  Copyright (c) 2015-2017 Angelo ZERR.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package ts.eclipse.ide.jsdt.internal.ui.refactoring;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.osgi.util.NLS;

/**
 * Refactoring UI messages.
 *
 */
public class RefactoringMessages extends NLS {

	private static final String BUNDLE_NAME = "ts.eclipse.ide.jsdt.internal.ui.refactoring.RefactoringMessages"; //$NON-NLS-1$

	private static ResourceBundle fResourceBundle;

	// Refactoring
	public static String RefactorMenu_label;
	public static String RenameAction_text;
	public static String RefactorActionGroup_no_refactoring_available;
	public static String RenameInformationPopup_delayJobName;
	public static String RenameInformationPopup_EnterNewName;
	public static String RenameInformationPopup_menu;
	public static String RenameInformationPopup_OpenDialog;
	public static String RenameInformationPopup_OptionsLink;
	public static String RenameInformationPopup_preferences;
	public static String RenameInformationPopup_Preview;
	public static String RenameInformationPopup_RenameInWorkspace;
	public static String RenameInformationPopup_snap_bottom_right;
	public static String RenameInformationPopup_snap_over_left;
	public static String RenameInformationPopup_snap_over_right;
	public static String RenameInformationPopup_snap_under_left;
	public static String RenameInformationPopup_snap_under_right;
	public static String RenameInformationPopup_SnapTo;

	// Rename support
	public static String RenameSupport_not_available;
	public static String RenameSupport_dialog_title;

	// Wizard
	public static String RenameInputWizardPage_new_name;
	public static String RenameRefactoringWizard_internal_error;
	public static String RenameInputWizardPage_findInComments;
	public static String RenameInputWizardPage_findInStrings;
	
	// Actions
	public static String RenameTypeScriptElementAction_name;
	public static String RenameTypeScriptElementAction_exception;

	// TypeScript rename processor.
	public static String TypeScriptRenameProcessor_name;
	public static String TypeScriptRenameProcessor_change_name;

	
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
		NLS.initializeMessages(BUNDLE_NAME, RefactoringMessages.class);
	}
}
