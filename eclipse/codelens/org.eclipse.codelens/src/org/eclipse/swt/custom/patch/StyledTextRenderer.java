package org.eclipse.swt.custom.patch;

import java.lang.reflect.Method;

import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.custom.patch.internal.StyledTextRendererEmulator;
import org.eclipse.swt.custom.provisional.ILineSpacingProvider;
import org.eclipse.swt.graphics.TextLayout;

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
		int newSpacing = lineSpacing;
		TextLayout layout = super.getTextLayout(lineIndex, orientation, width, lineSpacing, obj, proceed, args);
		if (lineSpacingProvider != null) {
			Integer spacing = lineSpacingProvider.getLineSpacing(lineIndex);
			if (spacing != null) {
				newSpacing = spacing;
			}
		}
		if (layout.getSpacing() != newSpacing) {
			// Update spacing
			layout.setSpacing(newSpacing);

			// invalidate text layout.
			// call styledText.setVariableLineHeight();
			Method m1 = styledText.getClass().getDeclaredMethod("setVariableLineHeight");
			m1.setAccessible(true);
			m1.invoke(styledText);

			// recreate text layout.
			layout = super.getTextLayout(lineIndex, orientation, width, newSpacing, obj, proceed, args);
		}
		return layout;
	}
}
