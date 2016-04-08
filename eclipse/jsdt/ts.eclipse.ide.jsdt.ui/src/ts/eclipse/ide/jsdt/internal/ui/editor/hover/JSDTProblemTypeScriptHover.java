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
package ts.eclipse.ide.jsdt.internal.ui.editor.hover;

import org.eclipse.wst.jsdt.ui.text.java.hover.IJavaEditorTextHover;

import ts.eclipse.ide.ui.hover.ProblemTypeScriptHover;

/**
 * JSDT Problem Hover used to display errors when mouse over a JS content which
 * have a typescript error.
 *
 */
public class JSDTProblemTypeScriptHover extends ProblemTypeScriptHover implements IJavaEditorTextHover {

}
