package org.eclipse.jface.text.provisional.viewzones;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;

public class DefaultViewZoneRenderer implements IViewZoneRenderer<DefaultViewZone> {

	private static final IViewZoneRenderer<DefaultViewZone> INSTANCE = new DefaultViewZoneRenderer();

	public static IViewZoneRenderer<DefaultViewZone> getInstance() {
		return INSTANCE;
	}

	@Override
	public void draw(DefaultViewZone viewZone, int paintX, int paintY, GC gc, StyledText styledText) {
		Rectangle client = styledText.getClientArea();
		gc.setBackground(styledText.getDisplay().getSystemColor(SWT.COLOR_WHITE));
		styledText.drawBackground(gc, paintX, paintY, client.width, viewZone.getHeightInPx());
		gc.setForeground(styledText.getDisplay().getSystemColor(SWT.COLOR_GRAY));

		Font font = new Font(styledText.getDisplay(), "Arial", 9, SWT.ITALIC);
		gc.setFont(font);
		gc.drawText(viewZone.getText(), paintX, paintY + 4);
	}

}
