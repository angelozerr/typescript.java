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
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

import ts.eclipse.ide.core.TypeScriptCorePlugin;
import ts.eclipse.ide.core.preferences.TypeScriptCorePreferenceConstants;
import ts.eclipse.ide.core.resources.UseSalsa;
import ts.eclipse.ide.internal.ui.TypeScriptUIMessages;
import ts.eclipse.ide.ui.TypeScriptUIImageResource;

/**
 * TypeScript Main page for global preferences.
 * 
 */
public class TypeScriptMainPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	public static final String PROP_ID = "ts.eclipse.ide.ui.preference.TypeScriptMainPreferencePage";

	public TypeScriptMainPreferencePage() {
		setImageDescriptor(TypeScriptUIImageResource.getImageDescriptor(TypeScriptUIImageResource.IMG_LOGO));
	}

	@Override
	protected void createFieldEditors() {

		String[][] uses = new String[3][2];
		uses[0][0] = TypeScriptUIMessages.TypeScriptMainPreferencePage_useSalsa_Never;
		uses[0][1] = UseSalsa.Never.name();
		uses[1][0] = TypeScriptUIMessages.TypeScriptMainPreferencePage_useSalsa_EveryTime;
		uses[1][1] = UseSalsa.EveryTime.name();
		uses[2][0] = TypeScriptUIMessages.TypeScriptMainPreferencePage_useSalsa_WhenNoJSDTNature;
		uses[2][1] = UseSalsa.WhenNoJSDTNature.name();

		ComboFieldEditor ternServerEditor = new ComboFieldEditor(
				TypeScriptCorePreferenceConstants.USE_SALSA_AS_JS_INFERENCE,
				TypeScriptUIMessages.TypeScriptMainPreferencePage_useSalsa, uses, getFieldEditorParent());
		addField(ternServerEditor);
	}

	@Override
	public void init(IWorkbench workbench) {

	}

	@Override
	protected IPreferenceStore doGetPreferenceStore() {
		IScopeContext scope = DefaultScope.INSTANCE;
		return new ScopedPreferenceStore(scope, TypeScriptCorePlugin.PLUGIN_ID);
	}

}
