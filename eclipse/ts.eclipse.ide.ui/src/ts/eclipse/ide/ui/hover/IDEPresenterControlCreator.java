/**
 *  Copyright (c) 2013-2015 Angelo ZERR.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package ts.eclipse.ide.ui.hover;

import ts.eclipse.jface.text.HoverLocationListener;
import ts.eclipse.jface.text.PresenterControlCreator;
import ts.eclipse.jface.text.html.TypeScriptBrowserInformationControl;

/**
 * IDE presenter control creator
 *
 */
public class IDEPresenterControlCreator extends PresenterControlCreator {

	private final ITypeScriptHoverInfoProvider provider;

	public IDEPresenterControlCreator(ITypeScriptHoverInfoProvider provider) {
		this.provider = provider;
	}

	@Override
	protected void addLinkListener(TypeScriptBrowserInformationControl control) {
		HoverLocationListener.addLinkListener(control,
				new IDEHoverLocationListener(control, provider));

	}
}
