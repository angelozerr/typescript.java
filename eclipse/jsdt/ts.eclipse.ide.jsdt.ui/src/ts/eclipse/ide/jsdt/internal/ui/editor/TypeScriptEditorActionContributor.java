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

import java.util.Iterator;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.actions.RetargetAction;
import org.eclipse.ui.texteditor.BasicTextEditorActionContributor;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.ITextEditorActionConstants;
import org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds;
import org.eclipse.ui.texteditor.ITextEditorExtension;
import org.eclipse.ui.texteditor.RetargetTextEditorAction;

import ts.eclipse.ide.jsdt.internal.ui.JSDTTypeScriptUIMessages;
import ts.eclipse.ide.jsdt.ui.actions.ITypeScriptEditorActionDefinitionIds;
import ts.eclipse.ide.jsdt.ui.actions.TypeScriptActionConstants;

/**
 * 
 * Action contributor for TypeScript.
 */
public class TypeScriptEditorActionContributor extends BasicTextEditorActionContributor {

	private RetargetTextEditorAction fShowOutline;
	private RetargetTextEditorAction openImplementation;

	public TypeScriptEditorActionContributor() {
		fShowOutline = new RetargetTextEditorAction(JSDTTypeScriptUIMessages.getResourceBundle(), "ShowOutline."); //$NON-NLS-1$
		fShowOutline.setActionDefinitionId(ITypeScriptEditorActionDefinitionIds.SHOW_OUTLINE);
		openImplementation = new RetargetTextEditorAction(JSDTTypeScriptUIMessages.getResourceBundle(),
				"OpenImplementation."); //$NON-NLS-1$
		openImplementation.setActionDefinitionId(ITypeScriptEditorActionDefinitionIds.OPEN_IMPLEMENTATION);
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
		openImplementation.setAction(getAction(textEditor, ITypeScriptEditorActionDefinitionIds.OPEN_IMPLEMENTATION));

		if (part instanceof TypeScriptEditor) {
			TypeScriptEditor tsEditor = (TypeScriptEditor) part;
			tsEditor.getActionGroup().fillActionBars(getActionBars());
		}
		
		IActionBars actionBars= getActionBars();
		IStatusLineManager manager= actionBars.getStatusLineManager();
		manager.setMessage(null);
		manager.setErrorMessage(null);
		
		/** The global actions to be connected with editor actions */
		IAction action= getAction(textEditor, ITextEditorActionConstants.NEXT);
		actionBars.setGlobalActionHandler(ITextEditorActionDefinitionIds.GOTO_NEXT_ANNOTATION, action);
		actionBars.setGlobalActionHandler(ITextEditorActionConstants.NEXT, action);
		action= getAction(textEditor, ITextEditorActionConstants.PREVIOUS);
		actionBars.setGlobalActionHandler(ITextEditorActionDefinitionIds.GOTO_PREVIOUS_ANNOTATION, action);
		actionBars.setGlobalActionHandler(ITextEditorActionConstants.PREVIOUS, action);
	}

	@Override
	public void contributeToMenu(IMenuManager menu) {
		super.contributeToMenu(menu);

		IMenuManager navigateMenu = menu.findMenuUsingPath(IWorkbenchActionConstants.M_NAVIGATE);
		if (navigateMenu != null) {
			navigateMenu.appendToGroup(IWorkbenchActionConstants.SHOW_EXT, fShowOutline);
		}
	}

	@Override
	public void dispose() {

//		Iterator e= fPartListeners.iterator();
//		while (e.hasNext())
//			getPage().removePartListener((RetargetAction) e.next());
//		fPartListeners.clear();

		setActiveEditor(null);
		super.dispose();
	}
}
