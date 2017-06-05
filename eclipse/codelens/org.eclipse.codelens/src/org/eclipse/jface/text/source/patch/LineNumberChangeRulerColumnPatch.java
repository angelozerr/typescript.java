package org.eclipse.jface.text.source.patch;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.eclipse.jface.internal.text.revisions.RevisionPainter;
import org.eclipse.jface.internal.text.source.DiffPainter;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.JFaceTextUtil;
import org.eclipse.jface.text.source.CompositeRuler;
import org.eclipse.jface.text.source.ILineRange;
import org.eclipse.jface.text.source.ISharedTextColors;
import org.eclipse.jface.text.source.LineNumberChangeRulerColumn;
import org.eclipse.jface.text.source.LineNumberRulerColumn;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.widgets.Display;

import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyFactory;

public class LineNumberChangeRulerColumnPatch {

	public static LineNumberChangeRulerColumn create(ISharedTextColors sharedColors) {
		try {
			ProxyFactory factory = new ProxyFactory();
			factory.setSuperclass(LineNumberChangeRulerColumn.class);
			factory.setHandler(new LineNumberChangeRulerColumnMethodHandler());
			return (LineNumberChangeRulerColumn) factory.create(new Class[] { ISharedTextColors.class },
					new Object[] { sharedColors });
		} catch (Exception e) {
			e.printStackTrace();
			return new LineNumberChangeRulerColumn(sharedColors);
		}
	}

	private static class LineNumberChangeRulerColumnMethodHandler implements MethodHandler {

		private ITextViewer fCachedTextViewer;
		private StyledText fCachedTextWidget;
		private boolean fCharacterDisplay;

		private RevisionPainter fRevisionPainter;
		/**
		 * The diff information painter strategy.
		 *
		 * @since 3.2
		 */
		private DiffPainter fDiffPainter;

		@Override
		public Object invoke(Object obj, Method thisMethod, Method proceed, Object[] args) throws Throwable {
			if ("createControl".equals(thisMethod.getName())) {
				CompositeRuler parentRuler = (CompositeRuler) args[0];
				this.fCachedTextViewer = parentRuler.getTextViewer();
				this.fCachedTextWidget = fCachedTextViewer.getTextWidget();
			} else if ("setDisplayMode".equals(thisMethod.getName())) {
				this.fCharacterDisplay = (boolean) args[0];
			} else if ("doPaint".equals(thisMethod.getName()) && args.length > 1) {
				GC gc = (GC) args[0];
				ILineRange visibleLines = (ILineRange) args[1];

				if (fRevisionPainter == null) {
					fRevisionPainter = getValue("fRevisionPainter", obj);
					fDiffPainter = getValue("fDiffPainter", obj);
				}

				LineNumberChangeRulerColumn l = ((LineNumberChangeRulerColumn) obj);
				doPaint(gc, visibleLines, l);
				return null;
			}
			return proceed.invoke(obj, args);
		}

		void doPaint(GC gc, ILineRange visibleLines, LineNumberChangeRulerColumn l) {
			Color foreground = gc.getForeground();
			if (visibleLines != null) {
				if (fRevisionPainter.hasInformation())
					fRevisionPainter.paint(gc, visibleLines);
				else if (fDiffPainter.hasInformation()) // don't paint quick
														// diff
														// colors if revisions
														// are
														// painted
					fDiffPainter.paint(gc, visibleLines);
			}
			gc.setForeground(foreground);
			if (l.isShowingLineNumbers() || fCharacterDisplay)
				doPaintPatch(gc, visibleLines, l);
		}

		/**
		 * Draws the ruler column.
		 *
		 * @param gc
		 *            the GC to draw into
		 * @param visibleLines
		 *            the visible model lines
		 * @since 3.2
		 */
		void doPaintPatch(GC gc, ILineRange visibleLines, LineNumberChangeRulerColumn l) {
			Display display = fCachedTextWidget.getDisplay();

			// draw diff info
			int y = -JFaceTextUtil.getHiddenTopLinePixels(fCachedTextWidget);

			// add empty lines if line is wrapped
			boolean isWrapActive = fCachedTextWidget.getWordWrap();

			int lastLine = end(visibleLines);
			for (int line = visibleLines.getStartLine(); line < lastLine; line++) {
				int widgetLine = JFaceTextUtil.modelLineToWidgetLine(fCachedTextViewer, line);
				if (widgetLine == -1)
					continue;

				final int offsetAtLine = fCachedTextWidget.getOffsetAtLine(widgetLine);
				// START PATCH
				// int lineHeight=
				// fCachedTextWidget.getLineHeight(offsetAtLine);
				int lineHeight = JFaceTextUtil.computeLineHeight(fCachedTextWidget, widgetLine, widgetLine + 1, 1);
				// END PATCH
				paintLine(line, y, lineHeight, gc, display, l);

				// increment y position
				if (!isWrapActive) {
					y += lineHeight;
				} else {
					int charCount = fCachedTextWidget.getCharCount();
					if (offsetAtLine == charCount)
						continue;

					// end of wrapped line
					final int offsetEnd = offsetAtLine + fCachedTextWidget.getLine(widgetLine).length();

					if (offsetEnd == charCount)
						continue;

					// use height of text bounding because bounds.width changes
					// on
					// word wrap
					y += fCachedTextWidget.getTextBounds(offsetAtLine, offsetEnd).height;
				}
			}
		}

		/* @since 3.2 */
		private static int end(ILineRange range) {
			return range.getStartLine() + range.getNumberOfLines();
		}

	};

	private static <T> T getValue(String name, Object instance) {
		try {
			Field f = LineNumberChangeRulerColumn.class.getDeclaredField(name);
			f.setAccessible(true);
			return (T) f.get(instance);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	private static void paintLine(int line, int y, int lineHeight, GC gc, Display display,
			LineNumberChangeRulerColumn l) {
		try {
			Method m = LineNumberRulerColumn.class
					.getDeclaredMethod("paintLine",
							new Class[] { int.class, int.class, int.class, GC.class, Display.class });
			m.setAccessible(true);
			m.invoke(l, line, y, lineHeight, gc, display);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
