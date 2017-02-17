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
package ts.eclipse.ide.json.ui.internal.tsconfig;

import org.eclipse.ui.forms.IManagedForm;

import ts.eclipse.ide.json.ui.AbstractFormPage;

/**
 * Plugins page for tsconfig.json editor.
 *
 */
public class PluginsPage2 extends AbstractFormPage {

	private static final String ID = "plugins";
	private PluginsBlock pluginsBlock;
	
	public PluginsPage2(TsconfigEditor editor) {
		super(editor, ID, TsconfigEditorMessages.PluginsPage_title);
		this.pluginsBlock = new PluginsBlock();
	}

	@Override
	protected String getFormTitleText() {
		return TsconfigEditorMessages.PluginsPage_title;
	}
	
	@Override
	protected void createUI(IManagedForm managedForm) {
		this.pluginsBlock.createContent(managedForm);
	}

}
