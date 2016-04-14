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
package ts.eclipse.ide.internal.ui.viewer;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

import ts.eclipse.ide.core.utils.TypeScriptResourceUtil;

/**
 * {@link ViewerFilter} implementation to filter compiled source *.js, *.js.map.
 * If we have:
 * 
 * <pre>
 * -a.js - a.js.map - a.ts - b.js
 * </pre>
 * 
 * The filter will hide a.js and a.js.map
 */
public class TypeScriptFilter extends ViewerFilter {

	@Override
	public boolean select(Viewer viewer, Object parent, Object element) {
		if (element instanceof IFile) {
			IFile file = (IFile) element;
			if (!TypeScriptResourceUtil.isCompiledTypeScriptResource(file)) {
				return true;
			}
			return hasParentTypeScriptFile(parent);
		}
		return true;
	}

	private boolean hasParentTypeScriptFile(Object parent) {
		if (parent instanceof TreePath) {
			TreePath treePath = (TreePath) parent;
			Object segment = treePath.getLastSegment();
			if (segment == null) {
				return false;
			}
			return TypeScriptResourceUtil.isTsOrTsxFile(segment);
		}
		return false;
	}

}
