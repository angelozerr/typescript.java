package org.eclipse.jface.text.provisional.viewzones;

import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.MouseEvent;

public abstract class ViewZone implements IViewZone {

	private StyledText styledText;
	private int offsetAtLine;
	private int afterLineNumber;
	private int height;

	private boolean disposed;

	public ViewZone(int afterLineNumber, int height) {
		this.height = height;
		setAfterLineNumber(afterLineNumber);
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
		if (offsetAtLine != -1) {
			try {
				afterLineNumber = styledText.getLineAtOffset(offsetAtLine);
			} catch (Exception e) {
				// e.printStackTrace();
				return -1;
			}
		}
		return afterLineNumber;
	}

	public int getOffsetAtLine() {
		if (offsetAtLine == -1) {
			offsetAtLine = getOffsetAtLine(afterLineNumber);
		}
		return offsetAtLine;
	}

	protected int getOffsetAtLine(int lineIndex) {
		return styledText.getOffsetAtLine(lineIndex);
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
	public void dispose() {
		this.disposed = true;
		this.setStyledText(null);
	}

	@Override
	public boolean isDisposed() {
		return disposed;
	}

	@Override
	public void mouseHover(MouseEvent event) {
		// System.err.println("mouseHover");
	}

	@Override
	public void mouseEnter(MouseEvent event) {
		// System.err.println("mouseEnter");
	}

	@Override
	public void mouseExit(MouseEvent event) {
		// System.err.println("mouseExit");
	}

	@Override
	public void onMouseClick(MouseEvent event) {

	}

	public StyledText getStyledText() {
		return styledText;
	}
}
