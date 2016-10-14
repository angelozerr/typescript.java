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

import ts.eclipse.ide.json.ui.actions.AbstractFileAction;
import ts.eclipse.ide.ui.TypeScriptUIImageResource;
import ts.eclipse.ide.ui.launch.TypeScriptCompilerLaunchHelper;

/**
 * Build action.
 *
 */
public class BuildAction extends AbstractFileAction {

	public BuildAction(TsconfigEditor editor) {
		super(editor);
		super.setText(TsconfigEditorMessages.BuildAction_text);
		super.setImageDescriptor(TypeScriptUIImageResource.getImageDescriptor(TypeScriptUIImageResource.IMG_BUILD));
	}

	@Override
	public void run() {
		IFile tsconfigFile = getFile();
		if (tsconfigFile != null) {
			TypeScriptCompilerLaunchHelper.launch(tsconfigFile);
		}
	}

}
