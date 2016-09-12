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

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.osgi.util.NLS;

/**
 * TypeScript UI messages.
 *
 */
public class JSDTTypeScriptUIMessages extends NLS {

	private static final String BUNDLE_NAME = "ts.eclipse.ide.jsdt.internal.ui.JSDTTypeScriptUIMessages"; //$NON-NLS-1$

	private static ResourceBundle fResourceBundle;

	// Action
	public static String GotoMatchingBracket_label;
	public static String GotoMatchingBracket_error_invalidSelection;
	public static String GotoMatchingBracket_error_noMatchingBracket;
	public static String GotoMatchingBracket_error_bracketOutsideSelectedElement;

	public static String FindReferencesInProjectAction_error;
	public static String FindReferencesInProjectAction_error_title;

	public static String TypeScriptEditor_markOccurrences_job_name;

	public static String TypeScriptContentFormatter_Error_title;
	public static String TypeScriptContentFormatter_Error_message;

	// Templates Preferences
	public static String CodeTemplatesPreferencePage_title;

	public static String CodeTemplateBlock_link_tooltip;
	public static String CodeTemplateBlock_templates_comment_node;
	public static String CodeTemplateBlock_templates_code_node;
	public static String CodeTemplateBlock_catchblock_label;
	public static String CodeTemplateBlock_methodstub_label;
	public static String CodeTemplateBlock_constructorstub_label;
	public static String CodeTemplateBlock_newtype_label;
	public static String CodeTemplateBlock_classbody_label;
	public static String CodeTemplateBlock_interfacebody_label;
	public static String CodeTemplateBlock_enumbody_label;
	public static String CodeTemplateBlock_annotationbody_label;
	public static String CodeTemplateBlock_typecomment_label;
	public static String CodeTemplateBlock_fieldcomment_label;
	public static String CodeTemplateBlock_filecomment_label;
	public static String CodeTemplateBlock_methodcomment_label;
	public static String CodeTemplateBlock_overridecomment_label;
	public static String CodeTemplateBlock_delegatecomment_label;
	public static String CodeTemplateBlock_constructorcomment_label;
	public static String CodeTemplateBlock_gettercomment_label;
	public static String CodeTemplateBlock_settercomment_label;
	public static String CodeTemplateBlock_getterstub_label;
	public static String CodeTemplateBlock_setterstub_label;
	public static String CodeTemplateBlock_templates_edit_button;
	public static String CodeTemplateBlock_templates_import_button;
	public static String CodeTemplateBlock_templates_export_button;
	public static String CodeTemplateBlock_templates_exportall_button;
	public static String CodeTemplateBlock_createcomment_label;
	public static String CodeTemplateBlock_templates_label;
	public static String CodeTemplateBlock_preview;
	public static String CodeTemplateBlock_import_title;
	public static String CodeTemplateBlock_import_extension;
	public static String CodeTemplateBlock_export_title;
	public static String CodeTemplateBlock_export_filename;
	public static String CodeTemplateBlock_export_extension;
	public static String CodeTemplateBlock_export_exists_title;
	public static String CodeTemplateBlock_export_exists_message;
	public static String CodeTemplateBlock_error_read_title;
	public static String CodeTemplateBlock_error_read_message;
	public static String CodeTemplateBlock_error_parse_message;
	public static String CodeTemplateBlock_error_write_title;
	public static String CodeTemplateBlock_error_write_message;
	public static String CodeTemplateBlock_export_error_title;
	public static String CodeTemplateBlock_export_error_hidden;
	public static String CodeTemplateBlock_export_error_canNotWrite;
	
	public static String EditTemplateDialog_autoinsert;
	public static String EditTemplateDialog_error_noname;
	public static String EditTemplateDialog_error_spaces;
	public static String EditTemplateDialog_title_new;
	public static String EditTemplateDialog_title_edit;
	public static String EditTemplateDialog_name;
	public static String EditTemplateDialog_description;
	public static String EditTemplateDialog_context;
	public static String EditTemplateDialog_pattern;
	public static String EditTemplateDialog_insert_variable;
	public static String EditTemplateDialog_undo;
	public static String EditTemplateDialog_cut;
	public static String EditTemplateDialog_copy;
	public static String EditTemplateDialog_paste;
	public static String EditTemplateDialog_select_all;
	public static String EditTemplateDialog_content_assist;

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
		NLS.initializeMessages(BUNDLE_NAME, JSDTTypeScriptUIMessages.class);
	}
}
