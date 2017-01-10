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
package ts.eclipse.ide.ui.implementation;

import java.util.List;

import org.eclipse.jface.viewers.ITreeContentProvider;

public class TypeScriptImplementationContentProvider implements ITreeContentProvider {

	@Override
	public Object[] getChildren(Object arg0) {
		return null;
	}

	@Override
	public Object[] getElements(Object parent) {
		if (parent instanceof List<?>) {
			return ((List<?>) parent).toArray();
		}
		return null;
	}

	@Override
	public Object getParent(Object arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean hasChildren(Object arg0) {
		return false;
	}

}
