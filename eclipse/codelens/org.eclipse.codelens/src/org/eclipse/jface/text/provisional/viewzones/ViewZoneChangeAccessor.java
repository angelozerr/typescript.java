package org.eclipse.jface.text.provisional.viewzones;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IPaintPositionManager;
import org.eclipse.jface.text.IPainter;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.ITextViewerExtension2;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.custom.patch.StyledTextPatcher;
import org.eclipse.swt.custom.provisional.ILineSpacingProvider;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;

public class ViewZoneChangeAccessor implements IViewZoneChangeAccessor, ILineSpacingProvider, IPainter, PaintListener {

	private final ITextViewer textViewer;
	private List<IViewZone> viewZones;

	/** The active state of this painter */
	private boolean fIsActive = false;
	private StyledText fTextWidget;
	private int originalTopMargin;

	public ViewZoneChangeAccessor(ITextViewer textViewer) {
		super();
		this.textViewer = textViewer;
		fTextWidget = textViewer.getTextWidget();
		originalTopMargin = fTextWidget.getTopMargin();
		this.viewZones = new ArrayList<>();
		try {
			// Should be replaced with
			// "fTextWidget.setLineSpacingProvider(this);"
			StyledTextPatcher.setLineSpacingProvider(fTextWidget, this);
		} catch (Exception e) {
			e.printStackTrace();
		}
		synch(fTextWidget);
		((ITextViewerExtension2) textViewer).addPainter(this);
	}

	@Override
	public void addZone(IViewZone zone) {
		viewZones.add(zone);
		StyledText styledText = textViewer.getTextWidget();
		zone.setStyledText(styledText);
//		int line = zone.getAfterLineNumber();
//		if (line == 0) {
//			styledText.setTopMargin(originalTopMargin + zone.getHeightInPx());
//			// StyledTextRendererHelper.updateSpacing(styledText);
//		} else {
//			line--;
//			int start = styledText.getOffsetAtLine(line);
//			int length = styledText.getText().length() - start;
//			styledText.redrawRange(start, length, true);
//		}
	}

	@Override
	public void removeZone(IViewZone zone) {
		viewZones.remove(zone);
		StyledText styledText = textViewer.getTextWidget();
//		int line = zone.getAfterLineNumber();
//		if (line == 0) {
//			styledText.setTopMargin(originalTopMargin);
//		} else {
//			line--;
//			int start = styledText.getOffsetAtLine(line);
//			int length = styledText.getText().length() - start;
//			styledText.redrawRange(start, length, true);
//		}
	}

	// @Override
	// public void layoutZone(int id) {
	// // TODO Auto-generated method stub
	//
	// }

	public IViewZone getViewZone(int lineNumber) {
		for (IViewZone viewZone : viewZones) {
			if (lineNumber == viewZone.getAfterLineNumber()) {
				return viewZone;
			}
		}
		return null;
	}

	private void synch(StyledText text) {
		// use a verify listener to keep the offsets up to date
		text.addVerifyListener(new VerifyListener() {
			public void verifyText(VerifyEvent e) {
				int start = e.start;
				int replaceCharCount = e.end - e.start;
				int newCharCount = e.text.length();
				for (IViewZone viewZone : new ArrayList<>(viewZones)) {
					int offset = viewZone.getOffsetAtLine();
					if (start <= offset && offset < start + replaceCharCount) {
						// this zone is being deleted from the text
						removeZone(viewZone);
						offset = -1;
					}
					if (offset != -1 && offset >= start) {
						offset += newCharCount - replaceCharCount;
					}
					viewZone.setOffsetAtLine(offset);
				}
			}
		});
	}

	@Override
	public int getSize() {
		return viewZones.size();
	}

	@Override
	public Integer getLineSpacing(int lineIndex) {
		int lineNumber = lineIndex + 1;
		IViewZone viewZone = getViewZone(lineNumber);
		if (viewZone != null) {
			// There is view zone to render for the given line, update the line
			// spacing of the TextLayout linked to this line number.
			return viewZone.getHeightInPx();
		}
		return null;
	}

	@Override
	public void dispose() {
		fTextWidget = null;
	}

	@Override
	public void paint(int reason) {
		IDocument document = textViewer.getDocument();
		if (document == null) {
			deactivate(false);
			return;
		}
		if (!fIsActive) {
			StyledText styledText = textViewer.getTextWidget();
			fIsActive = true;
			styledText.addPaintListener(this);
			redrawAll();
		} else if (reason == CONFIGURATION || reason == INTERNAL) {
			redrawAll();
		}
	}

	@Override
	public void deactivate(boolean redraw) {
		if (fIsActive) {
			fIsActive = false;
			fTextWidget.removePaintListener(this);
			if (redraw) {
				redrawAll();
			}
		}
	}

	@Override
	public void setPositionManager(IPaintPositionManager manager) {

	}

	/**
	 * Redraw all of the text widgets visible content.
	 */
	private void redrawAll() {
		fTextWidget.redraw();
	}

	@Override
	public void paintControl(PaintEvent event) {
		if (fTextWidget == null)
			return;
		if (event.width == 0 || event.height == 0)
			return;
		int clientAreaWidth = fTextWidget.getClientArea().width;
		int clientAreaHeight = fTextWidget.getClientArea().height;
		if (clientAreaWidth == 0 || clientAreaHeight == 0)
			return;

		int startLine = fTextWidget.getLineIndex(event.y);
		int y = startLine == 0 ? 0 : fTextWidget.getLinePixel(startLine);
		int endY = event.y + event.height;
		GC gc = event.gc;
		Color background = fTextWidget.getBackground();
		Color foreground = fTextWidget.getForeground();
		if (endY > 0) {
			int lineCount = fTextWidget.getLineCount();
			int x = fTextWidget.getLeftMargin() - fTextWidget.getHorizontalPixel();
			// leftMargin - horizontalScrollOffset;
			for (int lineIndex = startLine; y < endY && lineIndex < lineCount; lineIndex++) {
				if (lineIndex == 0) {
					IViewZone viewZone = getViewZone(lineIndex);
					if (viewZone != null) {
						viewZone.getRenderer().draw(viewZone, x, 0, event.gc, fTextWidget);
					} else {
						if (originalTopMargin != fTextWidget.getTopMargin()) {
							fTextWidget.setTopMargin(originalTopMargin);
						}
					}
				}
				int lineNumber = lineIndex + 1;
				IViewZone viewZone = getViewZone(lineNumber);
				if (viewZone != null) {
					Point topLeft = fTextWidget.getLocationAtOffset(viewZone.getOffsetAtLine());
					y = topLeft.y; // fTextWidget.getLinePixel(lineIndex);
					viewZone.getRenderer().draw(viewZone, x, y - viewZone.getHeightInPx(), event.gc, fTextWidget);
				}
			}
		}
	}

	/*
	 * Draw characters in view range.
	 */
	private void handleDrawRequest(GC gc, int x, int y, int w, int h) {
		int startLine = fTextWidget.getLineIndex(y);
		int endLine = fTextWidget.getLineIndex(y + h - 1);

		if (startLine <= endLine && startLine < fTextWidget.getLineCount()) {
			if (startLine == 1) {
				// fTextWidget.
			}
		}
	}

}
