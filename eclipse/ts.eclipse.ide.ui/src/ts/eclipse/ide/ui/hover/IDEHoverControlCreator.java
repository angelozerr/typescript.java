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

import org.eclipse.jface.internal.text.html.BrowserInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;

import ts.eclipse.jface.text.HoverControlCreator;
import ts.eclipse.jface.text.HoverLocationListener;

/**
 * IDE tern hover control creator.
 *
 */
public class IDEHoverControlCreator extends HoverControlCreator {

	private final ITypeScriptHoverInfoProvider provider;

	public IDEHoverControlCreator(
			IInformationControlCreator informationPresenterControlCreator,
			ITypeScriptHoverInfoProvider provider) {
		super(informationPresenterControlCreator);
		this.provider = provider;
	}

	public IDEHoverControlCreator(
			IInformationControlCreator informationPresenterControlCreator,
			boolean additionalInfoAffordance, ITypeScriptHoverInfoProvider provider) {
		super(informationPresenterControlCreator, additionalInfoAffordance);
		this.provider = provider;
	}

	@Override
	protected void addLinkListener(BrowserInformationControl control) {
		HoverLocationListener.addLinkListener(control,
				new IDEHoverLocationListener(control, provider));
	}
}
