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
package ts.eclipse.jface.viewers;

import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

import ts.client.completions.CompletionEntry;
import ts.eclipse.jface.images.TypeScriptImagesRegistry;

/**
 * Label provider to manage image with {@link TSContentProposal}.
 * 
 */
public class TypeScriptLabelProvider extends LabelProvider {

	private static final ILabelProvider INSTANCE = new TypeScriptLabelProvider();

	public static ILabelProvider getInstance() {
		return INSTANCE;
	}

	@Override
	public String getText(Object element) {
		if (element instanceof IContentProposal) {
			return ((IContentProposal) element).getLabel();
		}
		return super.getText(element);
	}

	@Override
	public Image getImage(Object element) {
		if (element instanceof CompletionEntry) {
			CompletionEntry item = ((CompletionEntry) element);
			return TypeScriptImagesRegistry.getImage(item);
		}
		return super.getImage(element);
	}

}
