
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
package ts.eclipse.ide.internal.ui.preferences;

import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

import ts.eclipse.ide.core.TypeScriptCorePlugin;
import ts.eclipse.ide.core.preferences.TypeScriptCorePreferenceConstants;
import ts.eclipse.ide.core.resources.UseSalsa;
import ts.eclipse.ide.internal.ui.TypeScriptUIMessages;
import ts.eclipse.ide.ui.TypeScriptUIImageResource;

/**
 * Default preference page for TypeScript
 */
public class TypeScriptMainPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	public static final String PROP_ID = "ts.eclipse.ide.ui.preference.TypeScriptMainPreferencePage";

	public TypeScriptMainPreferencePage() {
		super(GRID);
		IScopeContext scope = DefaultScope.INSTANCE;
		setPreferenceStore(new ScopedPreferenceStore(scope, TypeScriptCorePlugin.PLUGIN_ID));
		// setDescription(Messages.js_debug_pref_page_desc);
		setImageDescriptor(TypeScriptUIImageResource.getImageDescriptor(TypeScriptUIImageResource.IMG_LOGO));
	}

	@Override
	public void init(IWorkbench workbench) {
	}

	@Override
	public void createControl(Composite parent) {
		super.createControl(parent);
		// PlatformUI.getWorkbench().getHelpSystem().setHelp(parent,
		// IHelpContextIds.DEBUG_PREFERENCE_PAGE);
	}

	@Override
	protected Control createContents(Composite parent) {
		Composite comp = new Composite(parent, SWT.NONE);
		comp.setFont(parent.getFont());
		comp.setLayout(new GridLayout());
		comp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		String[][] uses = new String[3][2];
		uses[0][0] = TypeScriptUIMessages.TypeScriptMainPreferencePage_useSalsa_Never;
		uses[0][1] = UseSalsa.Never.name();
		uses[1][0] = TypeScriptUIMessages.TypeScriptMainPreferencePage_useSalsa_EveryTime;
		uses[1][1] = UseSalsa.EveryTime.name();
		uses[2][0] = TypeScriptUIMessages.TypeScriptMainPreferencePage_useSalsa_WhenNoJSDTNature;
		uses[2][1] = UseSalsa.WhenNoJSDTNature.name();

		ComboFieldEditor ternServerEditor = new ComboFieldEditor(
				TypeScriptCorePreferenceConstants.USE_SALSA_AS_JS_INFERENCE,
				TypeScriptUIMessages.TypeScriptMainPreferencePage_useSalsa, uses, comp);
		initEditor(ternServerEditor, getPreferenceStore());

		Group refactoringGroup = new Group(comp, SWT.NONE);
		refactoringGroup.setLayout(new GridLayout());
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = 2;
		refactoringGroup.setLayoutData(data);
		refactoringGroup.setText(TypeScriptUIMessages.TypeScriptMainPreferencePage_refactoring_title);

		BooleanFieldEditor refactoringAutoSave = new BooleanFieldEditor(
				TypeScriptCorePreferenceConstants.REFACTOR_SAVE_ALL_EDITORS,
				TypeScriptUIMessages.TypeScriptMainPreferencePage_refactoring_auto_save, refactoringGroup);
		initEditor(refactoringAutoSave, getPreferenceStore());

		BooleanFieldEditor refactoringLightweight = new BooleanFieldEditor(
				TypeScriptCorePreferenceConstants.REFACTOR_LIGHTWEIGHT,
				TypeScriptUIMessages.TypeScriptMainPreferencePage_refactoring_lightweight, refactoringGroup);
		initEditor(refactoringLightweight, getPreferenceStore());
		initGroup(refactoringGroup);

		return comp;
	}

	/**
	 * Refreshes the specified group to re-set the default spacings that have
	 * been trashed by the field editors
	 * 
	 * @param group
	 */
	void initGroup(Group group) {
		GridData gd = (GridData) group.getLayoutData();
		gd.grabExcessHorizontalSpace = true;
		GridLayout lo = (GridLayout) group.getLayout();
		lo.marginWidth = 5;
		lo.marginHeight = 5;
	}

	/**
	 * Initializes and sets up the given editor
	 * 
	 * @param editor
	 * @param store
	 */
	void initEditor(FieldEditor editor, IPreferenceStore store) {
		addField(editor);
		editor.setPage(this);
		editor.setPropertyChangeListener(this);
		editor.setPreferenceStore(store);
		editor.load();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.preference.FieldEditorPreferencePage#createFieldEditors
	 * ()
	 */
	protected void createFieldEditors() {
	}
}
