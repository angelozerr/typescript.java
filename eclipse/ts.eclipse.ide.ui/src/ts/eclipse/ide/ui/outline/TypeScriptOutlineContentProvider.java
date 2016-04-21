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

import java.util.List;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import ts.client.navbar.NavigationBarItem;

/**
 * TypeScript outline content provider.
 *
 */
public class TypeScriptOutlineContentProvider implements ITreeContentProvider {

	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}

	@Override
	public void inputChanged(Viewer arg0, Object arg1, Object arg2) {
		// TODO Auto-generated method stub

	}

	@Override
	public Object[] getChildren(Object element) {
		if (element instanceof NavigationBarItem) {
			return ((NavigationBarItem) element).getChildItems().toArray();
		}
		return null;
	}

	@Override
	public Object[] getElements(Object element) {
		if (element instanceof List<?>) {
			return ((List<?>) element).toArray();
//			try {
//				long start = System.currentTimeMillis();
//				Object[]  o =  ((ITypeScriptFile) element).getNavBar().toArray();
//				System.err.println(System.currentTimeMillis() - start);
//				return o;
//			} catch (TypeScriptException e) {
//				e.printStackTrace();
//			}
		}
		return null;
	}

	@Override
	public Object getParent(Object arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean hasChildren(Object element) {
		if (element instanceof NavigationBarItem) {
			return ((NavigationBarItem) element).hasChildItems();
		}
		return false;
	}

}
