package org.eclipse.jface.text.provisional.viewzones;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.IPaintPositionManager;
import org.eclipse.jface.text.IPainter;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.ITextViewerExtension2;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.custom.patch.StyledTextPatcher;
import org.eclipse.swt.custom.provisional.ILineSpacingProvider;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
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
	private ViewZoneMouseListener mouseListener;

	private class ViewZoneMouseListener implements MouseListener, MouseMoveListener {

		private IViewZone hoveredZone;

		@Override
		public void mouseUp(MouseEvent event) {
		}

		@Override
		public void mouseDown(MouseEvent event) {
			if (event.button == 1 && hoveredZone != null) {
				hoveredZone.onMouseClick(event);
			}
		}

		@Override
		public void mouseDoubleClick(MouseEvent event) {

		}

		@Override
		public void mouseMove(MouseEvent event) {
			int lineIndex = fTextWidget.getLineIndex(event.y);
			int lineNumber = lineIndex + 1;
			IViewZone zone = getViewZone(lineNumber);
			if (zone != null) {
				// The line which have a zone at end of this line is
				// hovered.
				// Check if it's the zone which is hovered or the view zone.
				int offset = fTextWidget.getOffsetAtLine(lineIndex + 1);
				Point p = fTextWidget.getLocationAtOffset(offset);
				if (p.y - zone.getHeightInPx() < event.y) {
					// Zone is hovered
					if (zone.equals(hoveredZone)) {
						hoveredZone.mouseHover(event);
						layoutZone(hoveredZone);
					} else {
						if (hoveredZone != null) {
							hoveredZone.mouseExit(event);
							layoutZone(hoveredZone);
						}
						hoveredZone = zone;
						hoveredZone.mouseEnter(event);
						layoutZone(hoveredZone);
					}
				} else {
					if (hoveredZone != null) {
						hoveredZone.mouseExit(event);
						layoutZone(hoveredZone);
						hoveredZone = null;
					}
				}
			} else if (hoveredZone != null) {
				hoveredZone.mouseExit(event);
				layoutZone(hoveredZone);
				hoveredZone = null;
			}
		}
	}

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
		// synch(fTextWidget);

		textViewer.getDocument().addDocumentListener(new IDocumentListener() {

			private List<IViewZone> toUpdate = new ArrayList<>();

			@Override
			public void documentChanged(DocumentEvent event) {
				if (!toUpdate.isEmpty()) {
					textViewer.getTextWidget().getDisplay().asyncExec(() -> {
						for (IViewZone viewZone : toUpdate) {
							ViewZoneChangeAccessor.this.layoutZone(viewZone);
						}
					});
				}
			}

			@Override
			public void documentAboutToBeChanged(DocumentEvent e) {
				int start = e.getOffset();
				int replaceCharCount = e.getLength();
				int newCharCount = e.getText().length();
				synchronized (viewZones) {
					toUpdate.clear();
					List<IViewZone> toRemove = new ArrayList<>();
					for (IViewZone viewZone : viewZones) {
						// System.err.println("before:" +
						// viewZone.getAfterLineNumber());
						int oldOffset = viewZone.getOffsetAtLine();
						int newOffset = oldOffset;
						if (start <= newOffset && newOffset < start + replaceCharCount) {
							// this zone is being deleted from the text
							toRemove.add(viewZone);
							newOffset = -1;
						}
						if (newOffset != -1 && newOffset >= start) {
							newOffset += newCharCount - replaceCharCount;
						}
						if (oldOffset != newOffset) {
							viewZone.setOffsetAtLine(newOffset);
							toUpdate.add(viewZone);
						}
						// System.err.println("after:" +
						// viewZone.getAfterLineNumber());
					}

					for (IViewZone viewZone : toRemove) {
						removeZone(viewZone);
					}
				}

			}
		});

		mouseListener = new ViewZoneMouseListener();
		textViewer.getTextWidget().addMouseMoveListener(mouseListener);
		textViewer.getTextWidget().addMouseListener(mouseListener);
		((ITextViewerExtension2) textViewer).addPainter(this);
	}

	@Override
	public void addZone(IViewZone zone) {
		synchronized (viewZones) {
			viewZones.add(zone);
			StyledText styledText = textViewer.getTextWidget();
			zone.setStyledText(styledText);
		}
	}

	@Override
	public void removeZone(IViewZone zone) {
		synchronized (viewZones) {
			viewZones.remove(zone);
			zone.dispose();
		}
	}

	@Override
	public void layoutZone(IViewZone zone) {
		StyledText styledText = textViewer.getTextWidget();
		int line = zone.getAfterLineNumber();
		if (line == 0) {
			styledText.setTopMargin(zone.isDisposed() ? originalTopMargin : zone.getHeightInPx());
		} else {
			line--;
			int start = styledText.getOffsetAtLine(line);
			int length = styledText.getText().length() - start;
			styledText.redrawRange(start, length, true);
		}
	}

	public IViewZone getViewZone(int lineNumber) {
		synchronized (viewZones) {
			for (IViewZone viewZone : viewZones) {
				if (lineNumber == viewZone.getAfterLineNumber()) {
					return viewZone;
				}
			}
		}
		return null;
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
		fTextWidget.removeMouseMoveListener(mouseListener);
		fTextWidget.removeMouseListener(mouseListener);
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
		// fTextWidget.redraw();
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
			for (int lineIndex = startLine - 1; y < endY && lineIndex < lineCount; lineIndex++) {
				if (lineIndex == 0) {
					IViewZone viewZone = getViewZone(lineIndex);
					if (viewZone != null) {
						int height = viewZone.getHeightInPx() + originalTopMargin;
						// if (height != fTextWidget.getTopMargin()) {
						// fTextWidget.setTopMargin(height);
						// }
						// TODO: support codelens with changed of top margin
						// viewZone.getRenderer().draw(viewZone, x, 0, gc,
						// fTextWidget);
					} else {
						// if (originalTopMargin != fTextWidget.getTopMargin())
						// {
						// fTextWidget.setTopMargin(originalTopMargin);
						// }
					}
				}
				int lineNumber = lineIndex + 1;
				IViewZone viewZone = getViewZone(lineNumber);
				if (viewZone != null) {
					Point topLeft = fTextWidget.getLocationAtOffset(viewZone.getOffsetAtLine());
					y = topLeft.y; // fTextWidget.getLinePixel(lineIndex);
					viewZone.draw(x, topLeft.x, y - viewZone.getHeightInPx(), gc);
				}
			}
		}
	}

}
