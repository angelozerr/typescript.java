package org.eclipse.jface.text.provisional.viewzones;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;

public class DefaultViewZone extends ViewZone {

	private String text;

	public DefaultViewZone(int afterLineNumber, int height) {
		super(afterLineNumber, height);
	}

	public DefaultViewZone(int afterLineNumber, int height, String text) {
		this(afterLineNumber, height);
		setText(text);
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getText() {
		return text;
	}

	@Override
	public void draw(int paintX, int paintY, GC gc) {
		StyledText styledText = super.getStyledText();
		Rectangle client = styledText.getClientArea();
		gc.setBackground(styledText.getDisplay().getSystemColor(SWT.COLOR_WHITE));
		styledText.drawBackground(gc, paintX, paintY, client.width, super.getHeightInPx());
		gc.setForeground(styledText.getDisplay().getSystemColor(SWT.COLOR_GRAY));

		Font font = new Font(styledText.getDisplay(), "Arial", 9, SWT.ITALIC);
		gc.setFont(font);
		gc.drawText(this.getText(), paintX, paintY + 4);
	}

}
