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

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.texteditor.BasicTextEditorActionContributor;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.ITextEditorExtension;
import org.eclipse.ui.texteditor.RetargetTextEditorAction;

import ts.eclipse.ide.jsdt.ui.actions.ITypeScriptEditorActionDefinitionIds;
import ts.eclipse.ide.jsdt.ui.actions.TypeScriptActionConstants;

/**
 * 
 * Action contributor for TypeScript.
 */
public class TypeScriptEditorActionContributor extends BasicTextEditorActionContributor {

	private RetargetTextEditorAction fShowOutline;

	public TypeScriptEditorActionContributor() {
		fShowOutline = new RetargetTextEditorAction(TypeScriptEditorMessages.getResourceBundle(), "ShowOutline."); //$NON-NLS-1$
		fShowOutline.setActionDefinitionId(ITypeScriptEditorActionDefinitionIds.SHOW_OUTLINE);
	}

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
		bars.setGlobalActionHandler(TypeScriptActionConstants.INDENT, getAction(textEditor, "Indent")); //$NON-NLS-1$
		bars.setGlobalActionHandler(TypeScriptActionConstants.COMMENT, getAction(textEditor, "Comment")); //$NON-NLS-1$
		bars.setGlobalActionHandler(TypeScriptActionConstants.UNCOMMENT, getAction(textEditor, "Uncomment")); //$NON-NLS-1$
		bars.setGlobalActionHandler(TypeScriptActionConstants.TOGGLE_COMMENT, getAction(textEditor, "ToggleComment")); //$NON-NLS-1$
		bars.setGlobalActionHandler(TypeScriptActionConstants.ADD_BLOCK_COMMENT,
				getAction(textEditor, "AddBlockComment")); //$NON-NLS-1$
		bars.setGlobalActionHandler(TypeScriptActionConstants.REMOVE_BLOCK_COMMENT,
				getAction(textEditor, "RemoveBlockComment")); //$NON-NLS-1$

		fShowOutline.setAction(getAction(textEditor, ITypeScriptEditorActionDefinitionIds.SHOW_OUTLINE));

		if (part instanceof TypeScriptEditor) {
			TypeScriptEditor tsEditor = (TypeScriptEditor) part;
			tsEditor.getActionGroup().fillActionBars(getActionBars());
		}
	}

	@Override
	public void contributeToMenu(IMenuManager menu) {
		super.contributeToMenu(menu);

		IMenuManager navigateMenu = menu.findMenuUsingPath(IWorkbenchActionConstants.M_NAVIGATE);
		if (navigateMenu != null) {
			navigateMenu.appendToGroup(IWorkbenchActionConstants.SHOW_EXT, fShowOutline);
		}
	}
}
