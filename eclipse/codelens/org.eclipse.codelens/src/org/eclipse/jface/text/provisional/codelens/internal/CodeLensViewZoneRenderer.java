package org.eclipse.jface.text.provisional.codelens.internal;

import org.eclipse.jface.text.provisional.viewzones.IViewZoneRenderer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;

public class CodeLensViewZoneRenderer implements IViewZoneRenderer<CodeLensViewZone> {

	private static final CodeLensViewZoneRenderer INSTANCE = new CodeLensViewZoneRenderer();

	public static CodeLensViewZoneRenderer getInstance() {
		return INSTANCE;
	}

	@Override
	public void draw(CodeLensViewZone viewZone, int paintX, int paintY, GC gc, StyledText styledText) {
		Rectangle client = styledText.getClientArea();
		gc.setBackground(styledText.getDisplay().getSystemColor(SWT.COLOR_WHITE));
		styledText.drawBackground(gc, paintX, paintY, client.width, viewZone.getHeightInPx());
		gc.setForeground(styledText.getDisplay().getSystemColor(SWT.COLOR_GRAY));

		String text = viewZone.getText();
		if (text != null) {
			Font font = new Font(styledText.getDisplay(), "Arial", 9, SWT.ITALIC);
			gc.setFont(font);
			gc.drawText(text, paintX, paintY + 4);
		}
	}

}