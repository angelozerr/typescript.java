package org.eclipse.jface.text.provisional.viewzones;

import org.eclipse.swt.custom.StyledText;

public class ViewZone implements IViewZone {

	private StyledText styledText;
	private int offsetAtLine;
	private int afterLineNumber;
	private int height;
	
	private final IViewZoneRenderer<?> renderer;

	public ViewZone(int afterLineNumber, int height, IViewZoneRenderer<?> renderer) {
		this.height = height;
		setAfterLineNumber(afterLineNumber);
		this.renderer = renderer;
	}

	public void setStyledText(StyledText styledText) {
		this.styledText = styledText;
	}

	public void setAfterLineNumber(int afterLineNumber) {
		this.afterLineNumber = afterLineNumber;
		this.offsetAtLine = -1;
	}

	@Override
	public int getAfterLineNumber() {
		if (afterLineNumber == -1) {
			afterLineNumber = styledText.getLineAtOffset(offsetAtLine);
		}
		return afterLineNumber;
	}

	public int getOffsetAtLine() {
		offsetAtLine = styledText.getOffsetAtLine(afterLineNumber);
		return offsetAtLine;
	}

	public void setOffsetAtLine(int offsetAtLine) {
		this.afterLineNumber = -1;
		this.offsetAtLine = offsetAtLine;
	}

	@Override
	public int getHeightInPx() {
		return height;
	}

	@Override
	public IViewZoneRenderer<?> getRenderer() {
		return renderer;
	}
	
	protected void redraw() {
		int line = getAfterLineNumber();
		if (line == 0) {
			styledText.setTopMargin(getHeightInPx());
		} else {
			line--;
			int start = styledText.getOffsetAtLine(line);
			int length = styledText.getOffsetAtLine(line + 1) - start;
			styledText.redrawRange(start, length, true);
		}
	}
}
