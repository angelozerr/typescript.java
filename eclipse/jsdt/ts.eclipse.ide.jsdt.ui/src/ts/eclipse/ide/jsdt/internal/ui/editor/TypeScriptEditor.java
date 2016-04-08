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

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.ui.texteditor.TextOperationAction;

import ts.eclipse.ide.jsdt.ui.actions.ITypeScriptEditorActionDefinitionIds;

/**
 * TypeScript editor.
 *
 */
public class TypeScriptEditor extends JavaScriptLightWeightEditor {

	@Override
	protected void createActions() {
		super.createActions();

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
		setKeyBindingScopes(new String[] { "ts.eclipse.ide.jsdt.ui.typeScriptViewScope" });  //$NON-NLS-1$
	}
}
