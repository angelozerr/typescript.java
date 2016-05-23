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

import ts.eclipse.ide.ui.preferences.PropertyAndPreferencePage;

/**
 * Editor preferences page
 *
 */
public class EditorPreferencePage extends PropertyAndPreferencePage {

	public static final String PREF_ID = "ts.eclipse.ide.ui.preference.EditorPreferencePage"; //$NON-NLS-1$
	public static final String PROP_ID = "ts.eclipse.ide.ui.property.EditorPreferencePage"; //$NON-NLS-1$

	public EditorPreferencePage() {
	}

	@Override
	protected Control createPreferenceBodyContent(Composite parent) {
		Composite content = new Composite(parent, SWT.NONE);
		return content;
	}

	@Override
	protected boolean hasProjectSpecificOptions(IProject project) {
		return false;
	}

	@Override
	protected String getPreferencePageID() {
		return PREF_ID;
	}

	@Override
	protected String getPropertyPageID() {
		return PROP_ID;
	}
}
