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

import org.eclipse.core.resources.IProject;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.preferences.IWorkbenchPreferenceContainer;

import ts.eclipse.ide.core.preferences.TypeScriptCorePreferenceConstants;
import ts.eclipse.ide.internal.ui.dialogs.IStatusChangeListener;
import ts.eclipse.ide.ui.preferences.OptionsConfigurationBlock;

/**
 * tsc configuration block.
 *
 */
public class CompilerConfigurationBlock extends OptionsConfigurationBlock {

	private static final Key PREF_TYPESCRIPT_REPOSITORY = getTypeScriptCoreKey(
			TypeScriptCorePreferenceConstants.TYPESCRIPT_REPOSITORY);

	public CompilerConfigurationBlock(IStatusChangeListener context, IProject project,
			IWorkbenchPreferenceContainer container) {
		super(context, project, getKeys(), container);
	}

	private static Key[] getKeys() {
		return new Key[] { PREF_TYPESCRIPT_REPOSITORY };
	}

	@Override
	protected Control createContents(Composite parent) {
		Composite contents = new Composite(parent, SWT.NONE);
		return contents;
	}

	@Override
	protected void validateSettings(Key changedKey, String oldValue, String newValue) {

	}

	@Override
	protected String[] getFullBuildDialogStrings(boolean workspaceSettings) {
		return null;
	}

	public void enablePreferenceContent(boolean enable) {

	}

}
