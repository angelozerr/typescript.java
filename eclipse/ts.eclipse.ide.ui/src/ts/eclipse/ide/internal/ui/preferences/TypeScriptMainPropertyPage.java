/**
 *  Copyright (c) 2013-2016 Angelo ZERR.
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
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Link;
import org.eclipse.ui.IWorkbenchPropertyPage;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.dialogs.PropertyPage;

import ts.eclipse.ide.core.utils.TypeScriptResourceUtil;
import ts.eclipse.ide.internal.ui.TypeScriptUIMessages;
import ts.eclipse.ide.ui.TypeScriptUIImageResource;

/**
 * TypeScript Main page for project properties.
 * 
 */
public class TypeScriptMainPropertyPage extends PropertyPage implements IWorkbenchPropertyPage {

	private static final String COMPILE_LINK = "compile";
	public static final String PROP_ID = "ts.eclipse.ide.ui.property.TypeScriptMainPropertyPage";

	public TypeScriptMainPropertyPage() {
		setImageDescriptor(TypeScriptUIImageResource.getImageDescriptor(TypeScriptUIImageResource.IMG_LOGO));
	}

	protected Control createContents(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		composite.setLayout(new GridLayout());

		final IProject project = (IProject) getElement().getAdapter(IResource.class);
		if (project != null) {
			GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
			gd.horizontalSpan = 3;

			final Button enableBuilderCheckbox = new Button(composite, SWT.CHECK);
			enableBuilderCheckbox.setFont(JFaceResources.getDialogFont());
			enableBuilderCheckbox
					.setText(TypeScriptUIMessages.TypeScriptMainPropertyPage_enable_builder_checkbox_label);
			enableBuilderCheckbox.setLayoutData(gd);
			enableBuilderCheckbox.setSelection(TypeScriptResourceUtil.hasTypeScriptBuilder(project));
			enableBuilderCheckbox.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent event) {
					if (TypeScriptResourceUtil.hasTypeScriptBuilder(project)) {
						try {
							TypeScriptResourceUtil.removeTypeScriptBuilder(project);
						} catch (CoreException e) {
							ErrorDialog.openError(getShell(), TypeScriptUIMessages.TypeScriptBuilder_Error_title,
									TypeScriptUIMessages.TypeScriptBuilder_disable_Error_message, e.getStatus());
						}
					} else {
						try {
							TypeScriptResourceUtil.addTypeScriptBuilder(project);
						} catch (CoreException e) {
							ErrorDialog.openError(getShell(), TypeScriptUIMessages.TypeScriptBuilder_Error_title,
									TypeScriptUIMessages.TypeScriptBuilder_enable_Error_message, e.getStatus());
						}
					}
				}
			});

			Link description = new Link(composite, SWT.NONE);
			description.setText(
					TypeScriptUIMessages.TypeScriptMainPropertyPage_enable_builder_checkbox_description);
			description.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(final SelectionEvent e) {
					if (COMPILE_LINK.equals(e.text)) {
						openProjectProperties(project, TypeScriptRuntimePreferencePage.PROP_ID);
					} else {
						// openProjectProperties(project, ValidationPreferencePage.PROP_ID);
					}
				}
			});
			description.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		}

		return composite;
	}

	private void openProjectProperties(IProject project, String id) {
		if (id != null) {
			PreferencesUtil.createPropertyDialogOn(getShell(), project, id, new String[] { id }, null).open();
		}
	}
}
