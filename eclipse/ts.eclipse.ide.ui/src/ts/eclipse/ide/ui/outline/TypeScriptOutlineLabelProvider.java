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
package ts.eclipse.ide.ui.outline;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

import ts.client.navbar.NavigationBarItem;
import ts.eclipse.jface.images.TypeScriptImagesRegistry;

/**
 * TypeScript outline label provider.
 *
 */
public class TypeScriptOutlineLabelProvider extends LabelProvider {

	@Override
	public String getText(Object element) {
		if (element instanceof NavigationBarItem) {
			return ((NavigationBarItem) element).getText();
		}
		return super.getText(element);
	}

	@Override
	public Image getImage(Object element) {
		if (element instanceof NavigationBarItem) {
			return TypeScriptImagesRegistry.getImage(((NavigationBarItem) element));
		}
		return super.getImage(element);
	}
}
