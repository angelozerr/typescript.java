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
package ts.eclipse.ide.internal.ui.navigator;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import ts.eclipse.ide.core.utils.TypeScriptResourceUtil;

/**
 * TypeScript navigator used to display for *.ts file, *.js and *.js.map files
 * as children.
 *
 */
public class TypeScriptNavigatorContentProvider implements ITreeContentProvider {

	public static final Object[] NO_CHILDREN = new Object[0];

	@Override
	public Object[] getElements(Object paramObject) {
		return NO_CHILDREN;
	}

	@Override
	public Object[] getChildren(Object element) {
		if (element instanceof IResource) {
			IResource resource = (IResource) element;
			Object[] children = getChildren(resource);
			return children != null ? children : NO_CHILDREN;
		}
		return NO_CHILDREN;
	}

	private Object[] getChildren(IResource resource) {
		switch (resource.getType()) {
		case IResource.PROJECT:
			return getTypescriptResources((IProject) resource);
		case IResource.FILE:
			return getEmmitedFiles((IFile) resource);
		}
		return null;
	}

	private Object[] getTypescriptResources(IProject project) {
		if (TypeScriptResourceUtil.isTypeScriptProject(project)) {
			
		}
		return null;
	}

	private Object[] getEmmitedFiles(IFile file) {
		try {
			return TypeScriptResourceUtil.getEmittedFiles(file);
		} catch (CoreException e) {
			return null;
		}
	}

	@Override
	public Object getParent(Object element) {
		if ((element instanceof IFile)) {
			return ((IFile) element).getParent();
		}
		return null;
	}

	@Override
	public boolean hasChildren(Object element) {
		// for performance, returns true to avoid loading twice compiled
		// resources *.js and *.js.map
		return TypeScriptResourceUtil.isTsOrTsxFile(element);
	}

	@Override
	public void dispose() {

	}

	@Override
	public void inputChanged(Viewer paramViewer, Object paramObject1, Object paramObject2) {

	}
}
