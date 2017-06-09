package org.eclipse.jface.text.provisional.codelens.internal;

import org.eclipse.jface.text.provisional.viewzones.IViewZoneRenderer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
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
			int leading = getLeadingSpaces(styledText.getLine(viewZone.getAfterLineNumber()));
			if (leading > 0) {
				Point topLeft = styledText.getLocationAtOffset(viewZone.getOffsetAtLine() + leading);
				paintX+=topLeft.x;
			}
			Font font = new Font(styledText.getDisplay(), "Arial", 9, SWT.ITALIC);
			gc.setFont(font);
			int x = paintX;
			int y = paintY + 4;
			gc.drawText(text, x, y);
			
			if (viewZone.getHover() != null) {
				styledText.setCursor(styledText.getDisplay().getSystemCursor(SWT.CURSOR_HAND));
				Point extent = gc.textExtent(text);
				gc.drawLine(x - 1, y + extent.y - 1, x + extent.x - 1, y + extent.y - 1);
			} else {
				styledText.setCursor(null);
			}
		}
	}
	
	private static int getLeadingSpaces(String line)
	{
	    int counter = 0;

	    char[] chars = line.toCharArray();
	    for (char c : chars)
	    {
	        if (c == '\t')
	            counter ++;
	        else if (c == ' ')
	            counter++;
	        else
	            break;
	    }

	    return counter;
	}



}