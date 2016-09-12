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
package ts.eclipse.ide.jsdt.core.template;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.templates.DocumentTemplateContext;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.TemplateContextType;

/**
 * Abstract class for TypeScript document template context.
 *
 */
public class AbstractTypeScriptContext extends DocumentTemplateContext {

	private boolean fForceEvaluation;

	public AbstractTypeScriptContext(TemplateContextType type, IDocument document, int offset, int length) {
		super(type, document, offset, length);
	}

	public AbstractTypeScriptContext(TemplateContextType type, IDocument document, Position position) {
		super(type, document, position);
	}

	/**
	 * Sets whether evaluation is forced or not.
	 * 
	 * @param evaluate
	 *            <code>true</code> in order to force evaluation,
	 *            <code>false</code> otherwise
	 */
	public void setForceEvaluation(boolean evaluate) {
		fForceEvaluation = evaluate;
	}

	@Override
	public boolean canEvaluate(Template template) {
		if (fForceEvaluation)
			return true;

		String key = getKey();
		return template.matches(key, getContextType().getId()) && key.length() != 0
				&& template.getName().toLowerCase().startsWith(key.toLowerCase());
	}

	@Override
	public int getStart() {
		try {
			IDocument document = getDocument();

			int start = getCompletionOffset();
			int end = getCompletionOffset() + getCompletionLength();

			while (start != 0 && isTemplateNamePart(document.getChar(start - 1)))
				start--;

			while (start != end && Character.isWhitespace(document.getChar(start)))
				start++;

			if (start == end)
				start = getCompletionOffset();

			return start;

		} catch (BadLocationException e) {
			return super.getStart();
		}
	}

	@Override
	public int getEnd() {
		try {
			IDocument document = getDocument();

			int start = getCompletionOffset();
			int end = getCompletionOffset() + getCompletionLength();

			while (start != end && Character.isWhitespace(document.getChar(end - 1)))
				end--;

			return end;

		} catch (BadLocationException e) {
			return super.getEnd();
		}
	}

	/**
	 * Tells whether the given character can be part of a template name.
	 * 
	 * @param ch
	 *            the character to test
	 * @return <code>true</code> if the given character can be part of a
	 *         template name
	 * 
	 */
	private boolean isTemplateNamePart(char ch) {
		return !Character.isWhitespace(ch) && ch != '(' && ch != ')' && ch != '{' && ch != '}' && ch != ';'
				&& ch != '>';
	}

	@Override
	public String getKey() {
		if (getCompletionLength() == 0) {
			return super.getKey();
		}
		try {
			IDocument document = getDocument();
			int start = getStart();
			int end = getCompletionOffset();
			return start <= end ? document.get(start, end - start) : ""; //$NON-NLS-1$
		} catch (BadLocationException e) {
			return super.getKey();
		}
	}
}
