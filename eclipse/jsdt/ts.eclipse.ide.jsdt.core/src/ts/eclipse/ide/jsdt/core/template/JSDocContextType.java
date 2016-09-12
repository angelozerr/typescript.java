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
package ts.eclipse.ide.jsdt.core.template;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;

/**
 * JSDoc context type.
 *
 */
public class JSDocContextType extends AbstractTypeScriptContextType {

	public static final String NAME = "JSDoc"; //$NON-NLS-1$

	public JSDocContextType() {
		super(NAME);
	}

	@Override
	public JSDocContext createContext(IDocument document, Position position) {
		return new JSDocContext(this, document, position);
	}
}
