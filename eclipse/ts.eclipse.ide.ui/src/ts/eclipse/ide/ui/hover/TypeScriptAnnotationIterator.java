/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package ts.eclipse.ide.ui.hover;

import java.util.Iterator;

import org.eclipse.jface.text.quickassist.IQuickFixableAnnotation;
import org.eclipse.jface.text.source.Annotation;

/**
 * Filters problems based on their types.
 */
public class TypeScriptAnnotationIterator implements Iterator {

	private static final String ORG_ECLIPSE_WST_SSE_UI_TEMP = "org.eclipse.wst.sse.ui.temp.";

	private Iterator fIterator;
	private Annotation fNext;
	private boolean fSkipIrrelevants;
	private boolean fReturnAllAnnotations;

	/**
	 * Equivalent to
	 * <code>JavaAnnotationIterator(model, skipIrrelevants, false)</code>.
	 */
	public TypeScriptAnnotationIterator(Iterator<Annotation> iterator, boolean skipIrrelevants) {
		this(iterator, skipIrrelevants, false);
	}

	/**
	 * Returns a new JavaAnnotationIterator.
	 * 
	 * @param model
	 *            the annotation model
	 * @param skipIrrelevants
	 *            whether to skip irrelevant annotations
	 * @param returnAllAnnotations
	 *            Whether to return non IJavaAnnotations as well
	 */
	public TypeScriptAnnotationIterator(Iterator<Annotation> iterator, boolean skipIrrelevants,
			boolean returnAllAnnotations) {
		fReturnAllAnnotations = returnAllAnnotations;
		fIterator = iterator;
		fSkipIrrelevants = skipIrrelevants;
		skip();
	}

	private void skip() {
		while (fIterator.hasNext()) {
			Annotation next = (Annotation) fIterator.next();
			if (isTypeScriptAnnotation(next) || next instanceof IQuickFixableAnnotation) {
				if (fSkipIrrelevants) {
					if (!next.isMarkedDeleted()) {
						fNext = next;
						return;
					}
				} else {
					fNext = next;
					return;
				}
			} else if (fReturnAllAnnotations) {
				fNext = next;
				return;
			}
		}
		fNext = null;
	}

	/**
	 * Returns true if the given annotation is a Tern Annotation and false
	 * otherwise.
	 * 
	 * @param a
	 *            annotation to check
	 * @return true if the given annotation is a Tern Annotation and false
	 *         otherwise.
	 */
	protected boolean isTypeScriptAnnotation(Annotation a) {
		String type = a.getType();
		// Annotation coming from WTP TernSourceValidator
		return ((type != null && type.startsWith(ORG_ECLIPSE_WST_SSE_UI_TEMP)));
	}

	/*
	 * @see Iterator#hasNext()
	 */
	public boolean hasNext() {
		return fNext != null;
	}

	/*
	 * @see Iterator#next()
	 */
	public Object next() {
		try {
			return fNext;
		} finally {
			skip();
		}
	}

	/*
	 * @see Iterator#remove()
	 */
	public void remove() {
		throw new UnsupportedOperationException();
	}
}
