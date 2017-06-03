/**
 *  Copyright (c) 2015-2017 Angelo ZERR.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package ts.eclipse.ide.core.resources;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.text.IDocument;

import ts.resources.ITypeScriptFile;

/**
 * IDE TypeScript file API.
 *
 */
public interface IIDETypeScriptFile extends ITypeScriptFile {

	/**
	 * Returns the wrapped Eclipse resource.
	 * 
	 * @return
	 */
	IResource getResource();
	
	IDocument getDocument();

}
