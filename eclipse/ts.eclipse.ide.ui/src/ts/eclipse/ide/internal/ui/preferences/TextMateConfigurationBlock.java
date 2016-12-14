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
import org.eclipse.jface.dialogs.ControlEnableState;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.preferences.IWorkbenchPreferenceContainer;

import ts.eclipse.ide.internal.ui.TypeScriptUIMessages;
import ts.eclipse.ide.internal.ui.dialogs.IStatusChangeListener;
import ts.eclipse.ide.ui.preferences.OptionsConfigurationBlock;
import ts.eclipse.ide.ui.preferences.ScrolledPageContent;
import ts.eclipse.ide.ui.preferences.TypeScriptUIPreferenceConstants;

/**
 * TextMate configuration block.
 *
 */
public class TextMateConfigurationBlock extends OptionsConfigurationBlock {

	// Editor Options
	private static final Key PREF_USE_TEXMATE_FOR_SYNTAX_COLORING = getTypeScriptUIKey(
			TypeScriptUIPreferenceConstants.USE_TEXMATE_FOR_SYNTAX_COLORING);

	private Composite controlsComposite;
	private ControlEnableState blockEnableState;

	public TextMateConfigurationBlock(IStatusChangeListener context, IProject project,
			IWorkbenchPreferenceContainer container) {
		super(context, project, getKeys(), container);
		blockEnableState = null;
	}

	private static Key[] getKeys() {
		return new Key[] { PREF_USE_TEXMATE_FOR_SYNTAX_COLORING };
	}

	public void enablePreferenceContent(boolean enable) {
		if (controlsComposite != null && !controlsComposite.isDisposed()) {
			if (enable) {
				if (blockEnableState != null) {
					blockEnableState.restore();
					blockEnableState = null;
				}
			} else {
				if (blockEnableState == null) {
					blockEnableState = ControlEnableState.disable(controlsComposite);
				}
			}
		}
	}

	@Override
	protected Control createContents(Composite parent) {
		Composite nodejsComposite = createUI(parent);
		validateSettings(null, null, null);
		return nodejsComposite;
	}

	private Composite createUI(Composite parent) {
		final ScrolledPageContent pageContent = new ScrolledPageContent(parent);
		Composite composite = pageContent.getBody();
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		composite.setLayout(layout);

		controlsComposite = new Composite(composite, SWT.NONE);
		controlsComposite.setFont(composite.getFont());
		controlsComposite.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, false));

		layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.numColumns = 1;
		controlsComposite.setLayout(layout);

		// TextMate options
		createTextMateOptions(controlsComposite);
		return pageContent;
	}

	/**
	 * Create editor options.
	 * 
	 * @param parent
	 */
	private void createTextMateOptions(Composite parent) {

		Group group = new Group(parent, SWT.NONE);
		group.setText(TypeScriptUIMessages.TextMateConfigurationBlock_textmate_group_label);

		GridLayout layout = new GridLayout();
		layout.numColumns = 3;
		group.setLayout(layout);
		group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		// Use TextMate for syntax coloring
		addCheckBox(group, TypeScriptUIMessages.TextMateConfigurationBlock_textmate_SyntaxColoring,
				PREF_USE_TEXMATE_FOR_SYNTAX_COLORING, new String[] { "true", "false" }, 0);
	}

	@Override
	protected void validateSettings(Key changedKey, String oldValue, String newValue) {
		if (!areSettingsEnabled()) {
			return;
		}
		if (changedKey != null) {

		}
	}

	@Override
	protected String[] getFullBuildDialogStrings(boolean workspaceSettings) {
		return null;
	}

}
