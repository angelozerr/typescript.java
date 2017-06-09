package org.eclipse.jface.text.provisional.viewzones;

import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.GC;

public interface IViewZone {

	/**
	 * The line number after which this zone should appear. Use 0 to place a
	 * view zone before the first line number.
	 */
	int getAfterLineNumber();

	int getOffsetAtLine();

	void setOffsetAtLine(int offsetAtLine);

	int getHeightInPx();

	void setStyledText(StyledText styledText);
	
	boolean isDisposed();

	void dispose();

	void mouseHover(MouseEvent event);

	void mouseExit(MouseEvent event);

	void mouseEnter(MouseEvent event);

	void onMouseClick(MouseEvent event);

	void draw(int paintX, int paintY, GC gc);
}
