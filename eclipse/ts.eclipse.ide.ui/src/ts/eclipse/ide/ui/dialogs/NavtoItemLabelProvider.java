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
package ts.eclipse.ide.ui.dialogs;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

import ts.client.IKindProvider;
import ts.client.navto.NavtoItem;
import ts.eclipse.jface.images.TypeScriptImagesRegistry;

/**
 * TypeScript outline label provider.
 *
 */
public class NavtoItemLabelProvider extends LabelProvider {

	private static final ILabelProvider INSTANCE = new NavtoItemLabelProvider();

	public static ILabelProvider getInstance() {
		return INSTANCE;
	}

	@Override
	public String getText(Object element) {
		if (element instanceof NavtoItem) {
			return ((NavtoItem) element).getName();
		}
		return super.getText(element);
	}

	@Override
	public Image getImage(Object element) {
		if (element instanceof IKindProvider) {
			return TypeScriptImagesRegistry.getImage(((IKindProvider) element));
		}
		return super.getImage(element);
	}

}
