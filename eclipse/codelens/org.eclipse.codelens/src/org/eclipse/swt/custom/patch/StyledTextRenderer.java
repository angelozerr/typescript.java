/**
 *  Copyright (c) 2015-2017 Angelo ZERR.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package org.eclipse.swt.custom.patch;

import java.lang.reflect.Method;

import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.custom.patch.internal.StyledTextRendererEmulator;
import org.eclipse.swt.custom.provisional.ILineSpacingProvider;
import org.eclipse.swt.graphics.TextLayout;

/**
 * Class which should replace the private class
 * {@link org.eclipse.swt.custom.StyledTextRenderer} to support line spacing for
 * a given line.
 *
 */
public class StyledTextRenderer extends StyledTextRendererEmulator {

	private ILineSpacingProvider lineSpacingProvider;

	private final StyledText styledText;

	public StyledTextRenderer(StyledText styledText) {
		this.styledText = styledText;
	}

	public void setLineSpacingProvider(ILineSpacingProvider lineSpacingProvider) {
		this.lineSpacingProvider = lineSpacingProvider;
	}

	@Override
	protected TextLayout getTextLayout(int lineIndex, int orientation, int width, int lineSpacing, Object obj,
			Method proceed, Object[] args) throws Exception {
		// Compute line spacing for the given line index.
		int newSpacing = lineSpacing;
		if (lineSpacingProvider != null) {
			Integer spacing = lineSpacingProvider.getLineSpacing(lineIndex);
			if (spacing != null) {
				newSpacing = spacing;
			}
		}
		TextLayout layout = super.getTextLayout(lineIndex, orientation, width, lineSpacing, obj, proceed, args);
		if (layout.getSpacing() != newSpacing) {
			// Update line spacing
			layout.setSpacing(newSpacing);
			// System.err.println("spacing changed [" + lineIndex + "] to " +
			// newSpacing);

			// invalidate text layout.
			// call styledText.setVariableLineHeight();
			StyledTextPatcher.setVariableLineHeight(styledText);

			// recreate text layout.
			layout = super.getTextLayout(lineIndex, orientation, width, newSpacing, obj, proceed, args);
		}
		return layout;
	}

}
