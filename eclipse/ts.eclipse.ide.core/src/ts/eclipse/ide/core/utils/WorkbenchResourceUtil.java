/*******************************************************************************
 * Copyright (c) 2015, 2016 Red Hat, Inc. 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * 	Contributors:
 * 		 Red Hat Inc. - initial API and implementation and/or initial documentation
 *******************************************************************************/
package ts.eclipse.ide.core.utils;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

/**
 * Utilities for Eclipse resources.
 *
 */
public class WorkbenchResourceUtil {

	public static IFile findFileRecursively(IResource resource, String name) throws CoreException {
		IContainer parent = getContainer(resource);
		return findFileRecursively(parent, name);
	}

	private static IContainer getContainer(IResource resource) {
		if (resource instanceof IContainer) {
			return (IContainer) resource;
		}
		return resource.getParent();
	}

	public static IFile findFileRecursively(IContainer container, String name) throws CoreException {
		for (IResource r : container.members()) {
			if (r instanceof IContainer) {
				IFile file = findFileRecursively((IContainer) r, name);
				if (file != null && file.exists()) {
					return file;
				}
			} else if (r instanceof IFile && r.getName().equals(name) && r.exists()) {
				return (IFile) r;
			}
		}
		return null;
	}
}
