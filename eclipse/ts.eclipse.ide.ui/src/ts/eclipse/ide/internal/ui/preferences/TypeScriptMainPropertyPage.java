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

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbenchPropertyPage;
import org.eclipse.ui.dialogs.PropertyPage;

import ts.eclipse.ide.ui.TypeScriptUIImageResource;

/**
 * TypeScript Main page for project properties.
 * 
 */
public class TypeScriptMainPropertyPage extends PropertyPage implements
		IWorkbenchPropertyPage {

	public static final String PROP_ID = "ts.eclipse.ide.ui.property.TypeScriptMainPropertyPage";

	public TypeScriptMainPropertyPage() {
		setImageDescriptor(TypeScriptUIImageResource
				.getImageDescriptor(TypeScriptUIImageResource.IMG_LOGO));
	}

	protected Control createContents(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayoutData(new GridData(4, 4, true, true));
		composite.setLayout(new GridLayout());
		return composite;
	}

}
