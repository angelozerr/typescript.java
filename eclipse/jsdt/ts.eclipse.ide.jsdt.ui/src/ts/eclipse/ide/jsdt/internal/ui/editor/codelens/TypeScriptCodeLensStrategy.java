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
package ts.eclipse.ide.jsdt.internal.ui.editor.codelens;

import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.provisional.codelens.CodeLensStrategy;

/**
 * TypeScript CodeLens strategy.
 *
 */
public class TypeScriptCodeLensStrategy extends CodeLensStrategy {

	private static String CODELENS_TARGET = "typeScript.codeLens";

	public TypeScriptCodeLensStrategy(ITextViewer textViewer) {
		super(textViewer);
		super.addTarget(CODELENS_TARGET);
	}

}
