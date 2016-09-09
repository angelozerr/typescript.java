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
package ts.eclipse.ide.ui.outline;

/**
 * TypeScript editor should implement this API to use TypesSript Outline.
 *
 */
public interface IEditorOutlineFeatures {

	/**
	 * Returns the current cursor offset of the editor.
	 * 
	 * @return the current cursor offset of the editor.
	 */
	int getCursorOffset();

}
