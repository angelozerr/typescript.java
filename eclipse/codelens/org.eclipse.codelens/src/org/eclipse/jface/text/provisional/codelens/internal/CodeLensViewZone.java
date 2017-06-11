package org.eclipse.jface.text.provisional.codelens.internal;

import java.util.List;

import org.eclipse.jface.text.provisional.codelens.ICodeLens;
import org.eclipse.jface.text.provisional.viewzones.ViewZone;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;

public class CodeLensViewZone extends ViewZone {

	private MouseEvent hover;
	private List<ICodeLens> resolvedSymbols;
	private ICodeLens hoveredCodeLens;
	private Integer hoveredCodeLensStartX;
	private Integer hoveredCodeLensEndX;

	public CodeLensViewZone(int afterLineNumber, int height) {
		super(afterLineNumber, height);
	}

	@Override
	public void mouseHover(MouseEvent event) {
		hover = event;
		StyledText styledText = getStyledText();
		styledText.setCursor(styledText.getDisplay().getSystemCursor(SWT.CURSOR_HAND));

	}

	@Override
	public void mouseEnter(MouseEvent event) {
		hover = event;
		StyledText styledText = getStyledText();
		styledText.setCursor(styledText.getDisplay().getSystemCursor(SWT.CURSOR_HAND));
	}

	@Override
	public void mouseExit(MouseEvent event) {
		hover = null;
		StyledText styledText = getStyledText();
		styledText.setCursor(null);
	}

	public MouseEvent getHover() {
		return hover;
	}

	@Override
	public void onMouseClick(MouseEvent event) {
		if (hoveredCodeLens != null) {
			hoveredCodeLens.open();
		}
	}

	public void updateCommands(List<ICodeLens> resolvedSymbols) {
		this.resolvedSymbols = resolvedSymbols;
	}

	@Override
	public void draw(int paintX, int paintY, GC gc) {
		StyledText styledText = super.getStyledText();
		Rectangle client = styledText.getClientArea();
		gc.setBackground(styledText.getDisplay().getSystemColor(SWT.COLOR_WHITE));
		styledText.drawBackground(gc, paintX, paintY, client.width, this.getHeightInPx());

		gc.setForeground(styledText.getDisplay().getSystemColor(SWT.COLOR_GRAY));

		Font font = new Font(styledText.getDisplay(), "Arial", 9, SWT.ITALIC);
		gc.setFont(font);
		String text = getText(gc);
		if (text != null) {
			Point topLeft = null;
			int leading = getLeadingSpaces(styledText.getLine(super.getAfterLineNumber()));
			if (leading > 0) {
				topLeft = styledText.getLocationAtOffset(super.getOffsetAtLine() + leading);
				paintX += topLeft.x;
			}
			int x = paintX;
			int y = paintY + 4;
			gc.drawText(text, x, y);

			if (hoveredCodeLensEndX != null) {
				Point extent = gc.textExtent(text);
				int startX = topLeft != null ? topLeft.x : 0;
				gc.drawLine(startX + hoveredCodeLensStartX, y + extent.y - 1, startX + hoveredCodeLensEndX,
						y + extent.y - 1);
			}
		}
	}

	private static int getLeadingSpaces(String line) {
		int counter = 0;

		char[] chars = line.toCharArray();
		for (char c : chars) {
			if (c == '\t')
				counter++;
			else if (c == ' ')
				counter++;
			else
				break;
		}

		return counter;
	}

	public String getText(GC gc) {
		hoveredCodeLens = null;
		hoveredCodeLensStartX = null;
		hoveredCodeLensEndX = null;
		if (resolvedSymbols == null || resolvedSymbols.size() < 1) {
			return "no command";
		} else {
			StringBuilder text = new StringBuilder();
			int i = 0;
			boolean hasHover = hover != null;
			for (ICodeLens codeLens : resolvedSymbols) {
				if (i > 0) {
					text.append(" | ");
				}
				Integer startX = null;
				if (hasHover && hoveredCodeLens == null) {
					startX = gc.textExtent(text.toString()).x;
				}
				text.append(codeLens.getCommand().getTitle());
				if (hasHover && hoveredCodeLens == null) {
					int endX = gc.textExtent(text.toString()).x;
					if (hover.x < endX) {
						hoveredCodeLensStartX = startX;
						hoveredCodeLensEndX = endX;
						hoveredCodeLens = codeLens;
					}
				}				
				i++;
			}
			return text.toString();
		}
	}

}