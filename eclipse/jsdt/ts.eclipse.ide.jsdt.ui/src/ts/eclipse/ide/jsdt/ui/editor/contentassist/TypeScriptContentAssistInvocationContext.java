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
package ts.eclipse.ide.jsdt.ui.editor.contentassist;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.ui.IEditorPart;
import org.eclipse.wst.jsdt.ui.text.java.ContentAssistInvocationContext;

import ts.eclipse.ide.ui.utils.EditorUtils;

/**
 * TypeScript content assist context.
 *
 */
public class TypeScriptContentAssistInvocationContext extends ContentAssistInvocationContext {

	private IEditorPart editor;

	public TypeScriptContentAssistInvocationContext(ITextViewer viewer, int offset, IEditorPart editor) {
		super(viewer, offset);
		this.editor = editor;
	}

	public IEditorPart getEditor() {
		return editor;
	}

	public IResource getResource() {
		return EditorUtils.getResource(editor);
	}

}
