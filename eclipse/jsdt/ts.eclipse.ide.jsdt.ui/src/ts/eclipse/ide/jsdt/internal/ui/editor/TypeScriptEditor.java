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

import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.ui.actions.ActionContext;
import org.eclipse.ui.actions.ActionGroup;
import org.eclipse.ui.texteditor.TextOperationAction;
import org.eclipse.wst.jsdt.ui.IContextMenuConstants;

import ts.eclipse.ide.jsdt.internal.ui.actions.CompositeActionGroup;
import ts.eclipse.ide.jsdt.internal.ui.actions.JavaSearchActionGroup;
import ts.eclipse.ide.jsdt.ui.actions.ITypeScriptEditorActionDefinitionIds;

/**
 * TypeScript editor.
 *
 */
public class TypeScriptEditor extends JavaScriptLightWeightEditor {

	protected CompositeActionGroup fActionGroups;
	private CompositeActionGroup fContextMenuGroup;
	
	protected ActionGroup getActionGroup() {
		return fActionGroups;
	}

	@Override
	protected void createActions() {
		super.createActions();

		ActionGroup oeg, ovg, jsg;
		fActionGroups = new CompositeActionGroup(new ActionGroup[] {/* oeg = new OpenEditorActionGroup(this),
				ovg = new OpenViewActionGroup(this),*/ jsg = new JavaSearchActionGroup(this) });
		fContextMenuGroup = new CompositeActionGroup(new ActionGroup[] { /*oeg, ovg,*/ jsg });

		// Format Action
		IAction action = new TextOperationAction(TypeScriptUIMessages.getResourceBundle(), "Format.", this, //$NON-NLS-1$
				ISourceViewer.FORMAT);
		action.setActionDefinitionId(ITypeScriptEditorActionDefinitionIds.FORMAT);
		setAction("Format", action); //$NON-NLS-1$
		markAsStateDependentAction("Format", true); //$NON-NLS-1$
		markAsSelectionDependentAction("Format", true); //$NON-NLS-1$
		// PlatformUI.getWorkbench().getHelpSystem().setHelp(action,
		// IJavaHelpContextIds.FORMAT_ACTION);
	}

	@Override
	protected void initializeKeyBindingScopes() {
		setKeyBindingScopes(new String[] { "ts.eclipse.ide.jsdt.ui.typeScriptViewScope" }); //$NON-NLS-1$
	}

	@Override
	public void editorContextMenuAboutToShow(IMenuManager menu) {

		super.editorContextMenuAboutToShow(menu);
		menu.insertAfter(IContextMenuConstants.GROUP_OPEN, new GroupMarker(IContextMenuConstants.GROUP_SHOW));

		ActionContext context= new ActionContext(getSelectionProvider().getSelection());
		fContextMenuGroup.setContext(context);
		fContextMenuGroup.fillContextMenu(menu);
		fContextMenuGroup.setContext(null);

		// Quick views
//		IAction action= getAction(IJavaEditorActionDefinitionIds.SHOW_OUTLINE);
//		menu.appendToGroup(IContextMenuConstants.GROUP_OPEN, action);
//		action= getAction(IJavaEditorActionDefinitionIds.OPEN_HIERARCHY);
//		menu.appendToGroup(IContextMenuConstants.GROUP_OPEN, action);
		
	}

	
	@Override
	public void dispose() {
		super.dispose();

		if (fActionGroups != null) {
			fActionGroups.dispose();
			fActionGroups = null;
		}
	}
}
