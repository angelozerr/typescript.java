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
package ts.eclipse.ide.json.ui.internal.tsconfig;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.Action;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;

import ts.eclipse.ide.ui.TypeScriptUIImageResource;
import ts.eclipse.ide.ui.launch.TypeScriptCompilerLaunchHelper;

/**
 * Build action.
 *
 */
public class BuildAction extends Action {

	private final TsconfigEditor editor;

	public BuildAction(TsconfigEditor editor) {
		this.editor = editor;
		super.setText(TsconfigEditorMessages.BuildAction_text);
		super.setImageDescriptor(TypeScriptUIImageResource.getImageDescriptor(TypeScriptUIImageResource.IMG_BUILD));
		super.setEnabled(getTsconfigFile() != null);
	}

	@Override
	public void run() {
		IFile tsconfigFile = getTsconfigFile();
		if (tsconfigFile != null) {
			TypeScriptCompilerLaunchHelper.launch(tsconfigFile);
		}
	}

	private IFile getTsconfigFile() {
		IEditorInput input = editor.getEditorInput();
		if (input instanceof IFileEditorInput) {
			return ((IFileEditorInput) input).getFile();
		}
		return null;
	}
}
