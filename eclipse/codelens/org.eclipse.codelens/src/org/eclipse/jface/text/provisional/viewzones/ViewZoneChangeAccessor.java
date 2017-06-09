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
import org.eclipse.swt.events.MouseTrackListener;
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

	private class ViewZoneMouseListener implements MouseListener, MouseTrackListener {

		private IViewZone hoveredZone;

		@Override
		public void mouseUp(MouseEvent arg0) {
			System.err.println("mouseUp");
			if (hoveredZone != null) {

			}
		}

		@Override
		public void mouseDown(MouseEvent event) {
			if (event.button == 1 && hoveredZone != null) {
				hoveredZone.onMouseClick(event);
			}
		}

		@Override
		public void mouseDoubleClick(MouseEvent arg0) {
			System.err.println("mouseDoubleClick");

		}

		@Override
		public void mouseHover(MouseEvent event) {
			int lineIndex = fTextWidget.getLineIndex(event.y);
			int lineNumber = lineIndex + 1;
			IViewZone zone = getViewZone(lineNumber);
			if (zone != null) {
				int offset = fTextWidget.getOffsetAtLine(lineIndex + 1);
				Point p = fTextWidget.getLocationAtOffset(offset);
				if (p.y - zone.getHeightInPx() < event.y) {
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

		@Override
		public void mouseExit(MouseEvent event) {
			if (hoveredZone != null) {
				hoveredZone.mouseExit(event);
				layoutZone(hoveredZone);
				hoveredZone = null;
			}
		}

		@Override
		public void mouseEnter(MouseEvent event) {
			if (hoveredZone != null) {
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
		// try {
		// Field fixedLineHeight =
		// fTextWidget.getClass().getDeclaredField("fixedLineHeight");
		// fixedLineHeight.setAccessible(true);
		// fixedLineHeight.set(fTextWidget, false);
		// } catch (Exception e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		synch(fTextWidget);

		textViewer.getDocument().addDocumentListener(new IDocumentListener() {

			@Override
			public void documentChanged(DocumentEvent event) {

			}

			@Override
			public void documentAboutToBeChanged(DocumentEvent e) {
				int start = e.getOffset();
				int replaceCharCount = e.getLength();
				int newCharCount = e.getText().length();
				synchronized (viewZones) {
					List<IViewZone> toRemove = new ArrayList<>();
					for (IViewZone viewZone : viewZones) {
						// System.err.println("before:" +
						// viewZone.getAfterLineNumber());
						int offset = viewZone.getOffsetAtLine();
						if (start <= offset && offset < start + replaceCharCount) {
							// this zone is being deleted from the text
							toRemove.add(viewZone);
							offset = -1;
						}
						if (offset != -1 && offset >= start) {
							offset += newCharCount - replaceCharCount;
						}
						viewZone.setOffsetAtLine(offset);
						// System.err.println("after:" +
						// viewZone.getAfterLineNumber());
					}

					for (IViewZone viewZone : toRemove) {
						removeZone(viewZone);
					}
				}

			}
		});

		this.mouseListener = new ViewZoneMouseListener();
		textViewer.getTextWidget().addMouseListener(mouseListener);
		textViewer.getTextWidget().addMouseTrackListener(mouseListener);

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

	private void synch(StyledText text) {
		// use a verify listener to keep the offsets up to date
		// text.addVerifyListener(new VerifyListener() {
		// public void verifyText(VerifyEvent e) {
		// int start = e.start;
		// int replaceCharCount = e.end - e.start;
		// int newCharCount = e.text.length();
		// synchronized (viewZones) {
		// List<IViewZone> toRemove = new ArrayList<>();
		// for (IViewZone viewZone : viewZones) {
		// System.err.println("before:" + viewZone.getAfterLineNumber());
		// int offset = viewZone.getOffsetAtLine();
		// if (start <= offset && offset < start + replaceCharCount) {
		// // this zone is being deleted from the text
		// toRemove.add(viewZone);
		// offset = -1;
		// }
		// if (offset != -1 && offset >= start) {
		// offset += newCharCount - replaceCharCount;
		// }
		// viewZone.setOffsetAtLine(offset);
		// if (e.text.length() == 0) {
		// int lineIndex = fTextWidget.getLineAtOffset(e.start);
		// //int lineOffset = fTextWidget.getOffsetAtLine(lineIndex + 1);
		// //viewZone.setOffsetAtLine(lineOffset);
		// }
		// System.err.println("after:" + viewZone.getAfterLineNumber());
		// }
		//
		// for (IViewZone viewZone : toRemove) {
		// removeZone(viewZone);
		// }
		// }
		// }
		// });
		//
		// text.addExtendedModifyListener(new ExtendedModifyListener() {
		//
		// @Override
		// public void modifyText(ExtendedModifyEvent e) {
		// int start = e.start;
		// int replaceCharCount = e.replacedText.length();
		// int newCharCount = e.length;
		// synchronized (viewZones) {
		// List<IViewZone> toRemove = new ArrayList<>();
		// for (IViewZone viewZone : viewZones) {
		// System.err.println("before:" + viewZone.getAfterLineNumber());
		// int offset = viewZone.getOffsetAtLine();
		// if (start <= offset && offset < start + replaceCharCount) {
		// // this zone is being deleted from the text
		// toRemove.add(viewZone);
		// offset = -1;
		// }
		// if (offset != -1 && offset >= start) {
		// offset += newCharCount - replaceCharCount;
		// }
		// viewZone.setOffsetAtLine(offset);
		// System.err.println("after:" + viewZone.getAfterLineNumber());
		// }
		//
		// for (IViewZone viewZone : toRemove) {
		// removeZone(viewZone);
		// }
		// }
		// }
		// });
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
		textViewer.getTextWidget().removeMouseTrackListener(mouseListener);
		textViewer.getTextWidget().removeMouseListener(mouseListener);
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
			for (int lineIndex = startLine; y < endY && lineIndex < lineCount; lineIndex++) {
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
					viewZone.draw(x, y - viewZone.getHeightInPx(), gc);
				}
			}
		}
	}

}
