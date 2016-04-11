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
package ts.eclipse.ide.jsdt.internal.ui.editor;

import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.texteditor.BasicTextEditorActionContributor;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.ITextEditorExtension;

import ts.eclipse.ide.jsdt.ui.actions.TypeScriptActionConstants;

/**
 * 
 * Action contributor for TypeScript.
 */
public class TypeScriptEditorActionContributor extends BasicTextEditorActionContributor {

	public void setActiveEditor(IEditorPart part) {
		super.setActiveEditor(part);

		ITextEditor textEditor = null;
		ITextEditorExtension textEditorExtension = null;
		if (part instanceof ITextEditor) {
			textEditor = (ITextEditor) part;
		}
		if (part instanceof ITextEditorExtension) {
			textEditorExtension = (ITextEditorExtension) part;
		}

		// Source menu.
		IActionBars bars = getActionBars();
		bars.setGlobalActionHandler(TypeScriptActionConstants.FORMAT, getAction(textEditor, "Format")); //$NON-NLS-1$

		if (part instanceof TypeScriptEditor) {
			TypeScriptEditor tsEditor = (TypeScriptEditor) part;
			tsEditor.getActionGroup().fillActionBars(getActionBars());
		}
	}
}
