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
package ts.eclipse.ide.json.ui.actions;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.Action;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;

/**
 * Abstract class for Action which must use {@link IFile} of the editor.
 * 
 */
public class AbstractFileAction extends Action {

	private final IEditorPart editor;

	public AbstractFileAction(IEditorPart editor) {
		this.editor = editor;
		super.setEnabled(getFile() != null);
	}

	/**
	 * Returns the file of the editor and null otherwise
	 * 
	 * @return the file of the editor and null otherwise
	 */
	protected IFile getFile() {
		IEditorInput input = editor.getEditorInput();
		if (input instanceof IFileEditorInput) {
			return ((IFileEditorInput) input).getFile();
		}
		return null;
	}

	protected IEditorPart getEditor() {
		return editor;
	}
}
