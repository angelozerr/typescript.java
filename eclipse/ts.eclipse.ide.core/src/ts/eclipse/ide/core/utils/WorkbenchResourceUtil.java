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
package ts.eclipse.ide.core.utils;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

/**
 * Utilities for Eclipse resources.
 *
 */
public class WorkbenchResourceUtil {

	public static IFile findFileRecursively(IResource resource, IPath name) throws CoreException {
		IContainer parent = getContainer(resource);
		return findFileRecursively(parent, name);
	}

	private static IContainer getContainer(IResource resource) {
		if (resource instanceof IContainer) {
			return (IContainer) resource;
		}
		return resource.getParent();
	}

	public static IFile findFileRecursively(IContainer container, IPath name) throws CoreException {
		if (container == null) {
			return null;
		}
		IFile file = container.getFile(name);
		if (file != null && file.exists()) {
			return file;
		}
		return findFileRecursively(container.getParent(), name);
	}
}
