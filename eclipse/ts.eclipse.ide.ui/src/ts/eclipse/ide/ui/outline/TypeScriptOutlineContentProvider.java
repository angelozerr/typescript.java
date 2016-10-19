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
import ts.client.navbar.NavigationBarItemRoot;
import ts.client.navbar.TextSpan;

/**
 * TypeScript outline content provider.
 *
 */
public class TypeScriptOutlineContentProvider implements ITreeContentProvider {

	@Override
	public Object[] getChildren(Object element) {
		if (element instanceof NavigationBarItem) {
			NavigationBarItem item = (NavigationBarItem) element;
			return item.hasChildItems() ? item.getChildItems().toArray() : null;
		}
		return null;
	}

	@Override
	public Object[] getElements(Object element) {
		if (element instanceof NavigationBarItemRoot) {
			return getChildren(element);
		}
		if (element instanceof List<?>) {
			return ((List<?>) element).toArray();
		}
		return null;
	}

	@Override
	public Object getParent(Object element) {
		if (element instanceof NavigationBarItemRoot) {
			return null;
		}
		if (element instanceof NavigationBarItem) {
			return ((NavigationBarItem) element).getParent();
		}
		if (element instanceof TextSpan) {
			return ((TextSpan) element).getParent();
		}
		return null;
	}

	@Override
	public boolean hasChildren(Object element) {
		if (element instanceof NavigationBarItem) {
			return ((NavigationBarItem) element).hasChildItems();
		}
		return false;
	}

	@Override
	public void dispose() {
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}
}
