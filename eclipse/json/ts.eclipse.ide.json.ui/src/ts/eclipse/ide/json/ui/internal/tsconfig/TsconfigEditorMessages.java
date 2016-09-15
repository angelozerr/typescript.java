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
package ts.eclipse.ide.json.ui.internal.tsconfig;

import org.eclipse.osgi.util.NLS;

/**
 * tsconfig.json editor messages.
 *
 */
public class TsconfigEditorMessages extends NLS {

	private static final String BUNDLE_NAME = "ts.eclipse.ide.json.ui.internal.tsconfig.TsconfigEditorMessages"; //$NON-NLS-1$
	
	// Overview Page
	public static String OverviewPage_title;
	public static String OverviewPage_GeneralInformationSection_desc;
	public static String OverviewPage_GeneralInformationSection_title;
	
	static {
		NLS.initializeMessages(BUNDLE_NAME, TsconfigEditorMessages.class);
	}
}
