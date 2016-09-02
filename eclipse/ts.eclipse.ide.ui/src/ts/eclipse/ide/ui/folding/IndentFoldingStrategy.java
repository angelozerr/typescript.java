/**
 *  Copyright (c) 2015-2016 Angelo ZERR.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package ts.eclipse.ide.ui.folding;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.reconciler.DirtyRegion;
import org.eclipse.jface.text.reconciler.IReconcilingStrategy;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.projection.IProjectionListener;
import org.eclipse.jface.text.source.projection.ProjectionAnnotation;
import org.eclipse.jface.text.source.projection.ProjectionAnnotationModel;
import org.eclipse.jface.text.source.projection.ProjectionViewer;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;

/**
 * Indent folding strategy to fold code by using indentation. This folding
 * strategy is very fast because it doesn't need to know the structure of the
 * code (don't need build an AST which could take time).
 */
public class IndentFoldingStrategy implements IReconcilingStrategy, IProjectionListener {

	private IDocument document;
	private ProjectionViewer viewer;
	private ProjectionAnnotationModel projectionAnnotationModel;
	private final String lineStartsWithKeyword;

	public IndentFoldingStrategy() {
		this(null);
	}

	public IndentFoldingStrategy(String lineStartsWithKeyword) {
		this.lineStartsWithKeyword = lineStartsWithKeyword;
	}

	/**
	 * A FoldingAnnotation is a ProjectionAnnotation it is folding and
	 * overriding the paint method (in a hacky type way) to prevent one line
	 * folding annotations to be drawn.
	 */
	protected class FoldingAnnotation extends ProjectionAnnotation {
		private boolean visible; /* workaround for BUG85874 */

		/**
		 * Creates a new FoldingAnnotation.
		 * 
		 * @param isCollapsed
		 *            true if this annotation should be collapsed, false
		 *            otherwise
		 */
		public FoldingAnnotation(boolean isCollapsed) {
			super(isCollapsed);
			visible = false;
		}

		/**
		 * Does not paint hidden annotations. Annotations are hidden when they
		 * only span one line.
		 * 
		 * @see ProjectionAnnotation#paint(org.eclipse.swt.graphics.GC,
		 *      org.eclipse.swt.widgets.Canvas,
		 *      org.eclipse.swt.graphics.Rectangle)
		 */
		@Override
		public void paint(GC gc, Canvas canvas, Rectangle rectangle) {
			/* workaround for BUG85874 */
			/*
			 * only need to check annotations that are expanded because hidden
			 * annotations should never have been given the chance to collapse.
			 */
			if (!isCollapsed()) {
				// working with rectangle, so line height
				FontMetrics metrics = gc.getFontMetrics();
				if (metrics != null) {
					// do not draw annotations that only span one line and
					// mark them as not visible
					if ((rectangle.height / metrics.getHeight()) <= 1) {
						visible = false;
						return;
					}
				}
			}
			visible = true;
			super.paint(gc, canvas, rectangle);
		}

		@Override
		public void markCollapsed() {
			/* workaround for BUG85874 */
			// do not mark collapsed if annotation is not visible
			if (visible)
				super.markCollapsed();
		}

		/**
		 * Two FoldingAnnotations are equal if their IndexedRegions are equal
		 * 
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			boolean equal = false;

			if (obj instanceof FoldingAnnotation) {
				// equal = fRegion.equals(((FoldingAnnotation) obj).fRegion);
			}

			return equal;
		}
	}

	/**
	 * The folding strategy must be associated with a viewer for it to function
	 * 
	 * @param viewer
	 *            the viewer to associate this folding strategy with
	 */
	public void setViewer(ProjectionViewer viewer) {
		if (viewer != null) {
			viewer.removeProjectionListener(this);
		}
		this.viewer = viewer;
		viewer.addProjectionListener(this);
		this.projectionAnnotationModel = viewer.getProjectionAnnotationModel();
	}

	public void uninstall() {
		setDocument(null);

		if (viewer != null) {
			viewer.removeProjectionListener(this);
			viewer = null;
		}

		projectionDisabled();
	}

	@Override
	public void setDocument(IDocument document) {
		this.document = document;
	}

	@Override
	public void projectionDisabled() {
		projectionAnnotationModel = null;
	}

	@Override
	public void projectionEnabled() {
		if (viewer != null) {
			projectionAnnotationModel = viewer.getProjectionAnnotationModel();
		}
	}

	private class LineIndent {
		public int line;
		public final int indent;

		public LineIndent(int line, int indent) {
			this.line = line;
			this.indent = indent;
		}
	}

	@Override
	public void reconcile(DirtyRegion dirtyRegion, IRegion subRegion) {
		// long start = System.currentTimeMillis();
		if (projectionAnnotationModel != null) {

			// these are what are passed off to the annotation model to
			// actually create and maintain the annotations
			List<Annotation> modifications = new ArrayList<Annotation>();
			List<FoldingAnnotation> deletions = new ArrayList<FoldingAnnotation>();
			List<FoldingAnnotation> existing = new ArrayList<FoldingAnnotation>();
			Map<Annotation, Position> additions = new HashMap<Annotation, Position>();
			boolean isInsert = dirtyRegion.getType().equals(DirtyRegion.INSERT);
			boolean isRemove = dirtyRegion.getType().equals(DirtyRegion.REMOVE);

			// find and mark all folding annotations with length 0 for deletion
			markInvalidAnnotationsForDeletion(dirtyRegion, deletions, existing);

			List<LineIndent> previousRegions = new ArrayList<LineIndent>();

			int tabSize = 1;
			int minimumRangeSize = 1;
			try {

				// Today we recompute annotation from the whole document each
				// time.
				// performance s good even with large document, but it should be
				// better to loop for only DirtyRegion (and before/after)
				// int offset = dirtyRegion.getOffset();
				// int length = dirtyRegion.getLength();
				// int startLine = 0; //document.getLineOfOffset(offset);
				int endLine = document.getNumberOfLines() - 1; // startLine +
																// document.getNumberOfLines(offset,
																// length) - 1;

				// sentinel, to make sure there's at least one entry
				previousRegions.add(new LineIndent(endLine, -1));

				int lastLineWhichIsNotEmpty = 0;
				int lineEmptyCount = 0;
				Integer lastLineForKeyword = null;
				int line = endLine;
				for (line = endLine; line >= 0; line--) {
					int lineOffset = document.getLineOffset(line);
					String delim = document.getLineDelimiter(line);
					int lineLength = document.getLineLength(line) - (delim != null ? delim.length() : 0);
					String lineContent = document.get(lineOffset, lineLength);

					LineState state = getLineState(lineContent, lastLineForKeyword);
					switch (state) {
					case StartWithKeyWord:
						lineEmptyCount = 0;
						lastLineWhichIsNotEmpty = line;
						if (lastLineForKeyword == null) {
							lastLineForKeyword = line;
						}
						break;
					case EmptyLine:
						lineEmptyCount++;
						break;
					default:
						addAnnotationForKeyword(modifications, deletions, existing, additions,
								line + 1 + lineEmptyCount, lastLineForKeyword);
						lastLineForKeyword = null;
						lineEmptyCount = 0;
						lastLineWhichIsNotEmpty = line;
						int indent = computeIndentLevel(lineContent, tabSize);
						if (indent == -1) {
							continue; // only whitespace
						}

						LineIndent previous = previousRegions.get(previousRegions.size() - 1);
						if (previous.indent > indent) {
							// discard all regions with larger indent
							do {
								previousRegions.remove(previousRegions.size() - 1);
								previous = previousRegions.get(previousRegions.size() - 1);
							} while (previous.indent > indent);

							// new folding range
							int endLineNumber = previous.line - 1;
							if (endLineNumber - line >= minimumRangeSize) {
								updateAnnotation(modifications, deletions, existing, additions, line, endLineNumber);
							}
						}
						if (previous.indent == indent) {
							previous.line = line;
						} else { // previous.indent < indent
							// new region with a bigger indent
							previousRegions.add(new LineIndent(line, indent));
						}
					}
				}
				addAnnotationForKeyword(modifications, deletions, existing, additions, lastLineWhichIsNotEmpty,
						lastLineForKeyword);
			} catch (BadLocationException e) {
				// should never done
				e.printStackTrace();
			}

			// reconcile each effected indexed region
			/*
			 * Iterator indexedRegionsIter = indexedRegions.iterator(); while
			 * (indexedRegionsIter.hasNext() && fProjectionAnnotationModel !=
			 * null) { IndexedRegion indexedRegion = (IndexedRegion)
			 * indexedRegionsIter.next();
			 * 
			 * // only try to create an annotation if the index region is a //
			 * valid type if (indexedRegionValidType(indexedRegion)) {
			 * FoldingAnnotation annotation = new
			 * FoldingAnnotation(indexedRegion, false);
			 * 
			 * // if INSERT calculate new addition position or modification //
			 * else if REMOVE add annotation to the deletion list if (isInsert)
			 * { Annotation existingAnno = getExistingAnnotation(indexedRegion);
			 * // if projection has been disabled the iter could be // null //
			 * if annotation does not already exist for this region // create a
			 * new one // else modify an old one, which could include deletion
			 * if (existingAnno == null) { Position newPos =
			 * calcNewFoldPosition(indexedRegion);
			 * 
			 * if (newPos != null && newPos.length > 0) {
			 * additions.put(annotation, newPos); } } else {
			 * updateAnnotations(existingAnno, indexedRegion, additions,
			 * modifications, deletions); } } else if (isRemove) {
			 * deletions.add(annotation); } } }
			 */

			// long end = System.currentTimeMillis();
			// System.err.println((end - start) + "ms");

			// be sure projection has not been disabled
			if (projectionAnnotationModel != null) {
				if (existing.size() > 0) {
					deletions.addAll(existing);
				}
				// send the calculated updates to the annotations to the
				// annotation model
				projectionAnnotationModel.modifyAnnotations((Annotation[]) deletions.toArray(new Annotation[1]),
						additions, (Annotation[]) modifications.toArray(new Annotation[0]));
			}

			// end = System.currentTimeMillis();
			// System.err.println((end - start) + "ms");
		}
	}

	private void addAnnotationForKeyword(List<Annotation> modifications, List<FoldingAnnotation> deletions,
			List<FoldingAnnotation> existing, Map<Annotation, Position> additions, int startLine,
			Integer lastLineForKeyword) throws BadLocationException {
		if (lastLineForKeyword != null) {
			updateAnnotation(modifications, deletions, existing, additions, startLine, lastLineForKeyword);
		}
	}

	private enum LineState {
		StartWithKeyWord, DontStartWithKeyWord, EmptyLine
	}

	/**
	 * Returns the line state for line which starts with a given keyword.
	 * 
	 * @param lineContent
	 *            line content.
	 * @param lastLineForKeyword
	 *            last line for the given keyword.
	 * @return
	 */
	private LineState getLineState(String lineContent, Integer lastLineForKeyword) {
		if (lineStartsWithKeyword == null) {
			// none keyword defined.
			return LineState.DontStartWithKeyWord;
		}
		if (lineContent != null && lineContent.trim().startsWith(lineStartsWithKeyword)) {
			// The line starts with the given keyword (ex: starts with "import")
			return LineState.StartWithKeyWord;
		}
		if (lastLineForKeyword != null && (lineContent == null || lineContent.trim().length() == 0)) {
			// a last line for keyword was defined, line is empty
			return LineState.EmptyLine;
		}
		return LineState.DontStartWithKeyWord;
	}

	private void updateAnnotation(List<Annotation> modifications, List<FoldingAnnotation> deletions,
			List<FoldingAnnotation> existing, Map<Annotation, Position> additions, int line, int endLineNumber)
			throws BadLocationException {
		int startOffset = document.getLineOffset(line);
		int endOffset = document.getLineOffset(endLineNumber) + document.getLineLength(endLineNumber);
		Position newPos = new Position(startOffset, endOffset - startOffset);
		if (existing.size() > 0) {
			FoldingAnnotation existingAnnotation = existing.remove(existing.size() - 1);
			updateAnnotations(existingAnnotation, newPos, additions, modifications, deletions);
		} else {
			additions.put(new FoldingAnnotation(false), newPos);
		}
	}

	private int computeIndentLevel(String line, int tabSize) {
		int i = 0;
		int indent = 0;
		while (i < line.length()) {
			char ch = line.charAt(i);
			if (ch == ' ') {
				indent++;
			} else if (ch == '\t') {
				indent = indent - indent % tabSize + tabSize;
			} else {
				break;
			}
			i++;
		}
		if (i == line.length()) {
			return -1; // line only consists of whitespace
		}
		return indent;
	}

	/**
	 * Given a {@link DirtyRegion} returns an {@link Iterator} of the already
	 * existing annotations in that region.
	 * 
	 * @param dirtyRegion
	 *            the {@link DirtyRegion} to check for existing annotations in
	 * 
	 * @return an {@link Iterator} over the annotations in the given
	 *         {@link DirtyRegion}. The iterator could have no annotations in
	 *         it. Or <code>null</code> if projection has been disabled.
	 */
	private Iterator getAnnotationIterator(DirtyRegion dirtyRegion) {
		Iterator annoIter = null;
		// be sure project has not been disabled
		if (projectionAnnotationModel != null) {
			// workaround for Platform Bug 299416
			int offset = dirtyRegion.getOffset();
			if (offset > 0) {
				offset--;
			}
			annoIter = projectionAnnotationModel.getAnnotationIterator(0, document.getLength(), false, false);
		}
		return annoIter;
	}

	/**
	 * <p>
	 * Gets the first {@link Annotation} at the start offset of the given
	 * {@link IndexedRegion}.
	 * </p>
	 * 
	 * @param indexedRegion
	 *            get the first {@link Annotation} at this {@link IndexedRegion}
	 * @return the first {@link Annotation} at the start offset of the given
	 *         {@link IndexedRegion}
	 */
	// private Annotation getExistingAnnotation(IndexedRegion indexedRegion) {
	// Iterator iter =
	// projectionAnnotationModel.getAnnotationIterator(indexedRegion.getStartOffset(),
	// 1, false, true);
	// Annotation anno = null;
	// if (iter.hasNext()) {
	// anno = (Annotation) iter.next();
	// }
	//
	// return anno;
	// }

	/**
	 * This is the default behavior for updating a dirtied IndexedRegion. This
	 * function can be overridden if slightly different functionality is
	 * required in a specific instance of this class.
	 * 
	 * @param existingAnnotationsIter
	 *            the existing annotations that need to be updated based on the
	 *            given dirtied IndexRegion
	 * @param dirtyRegion
	 *            the IndexedRegion that caused the annotations need for
	 *            updating
	 * @param modifications
	 *            the list of annotations to be modified
	 * @param deletions
	 *            the list of annotations to be deleted
	 */
	protected void updateAnnotations(Annotation existingAnnotation, Position newPos, Map additions, List modifications,
			List deletions) {
		if (existingAnnotation instanceof FoldingAnnotation) {
			FoldingAnnotation foldingAnnotation = (FoldingAnnotation) existingAnnotation;
			// Position newPos = null; //calcNewFoldPosition(null);

			// if a new position can be calculated then update the position of
			// the annotation,
			// else the annotation needs to be deleted
			if (newPos != null && newPos.length > 0 && projectionAnnotationModel != null) {
				Position oldPos = projectionAnnotationModel.getPosition(foldingAnnotation);
				// only update the position if we have to
				if (!newPos.equals(oldPos)) {
					oldPos.setOffset(newPos.offset);
					oldPos.setLength(newPos.length);
					modifications.add(foldingAnnotation);
				}
			} else {
				deletions.add(foldingAnnotation);
			}
		}
	}

	/**
	 * <p>
	 * Searches the given {@link DirtyRegion} for annotations that now have a
	 * length of 0. This is caused when something that was being folded has been
	 * deleted. These {@link FoldingAnnotation}s are then added to the
	 * {@link List} of {@link FoldingAnnotation}s to be deleted
	 * </p>
	 * 
	 * @param dirtyRegion
	 *            find the now invalid {@link FoldingAnnotation}s in this
	 *            {@link DirtyRegion}
	 * @param deletions
	 *            the current list of {@link FoldingAnnotation}s marked for
	 *            deletion that the newly found invalid
	 *            {@link FoldingAnnotation}s will be added to
	 */
	protected void markInvalidAnnotationsForDeletion(DirtyRegion dirtyRegion, List<FoldingAnnotation> deletions,
			List<FoldingAnnotation> existing) {
		Iterator iter = getAnnotationIterator(dirtyRegion);
		if (iter != null) {
			while (iter.hasNext()) {
				Annotation anno = (Annotation) iter.next();
				if (anno instanceof FoldingAnnotation) {
					FoldingAnnotation folding = (FoldingAnnotation) anno;
					Position pos = projectionAnnotationModel.getPosition(anno);
					if (pos.length == 0) {
						deletions.add(folding);
					} else {
						existing.add(folding);
					}
				}
			}
		}
	}

	@Override
	public void reconcile(IRegion partition) {
		// not used, we use:
		// reconcile(DirtyRegion dirtyRegion, IRegion subRegion)
	}
}
