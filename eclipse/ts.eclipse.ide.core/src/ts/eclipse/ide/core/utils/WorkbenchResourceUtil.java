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

import java.io.File;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

import ts.eclipse.ide.core.TypeScriptCorePlugin;
import ts.utils.FileUtils;
import ts.utils.StringUtils;

/**
 * Utilities for Eclipse resources.
 *
 */
public class WorkbenchResourceUtil {

	public static String getFileName(IResource file) {
		return FileUtils.normalizeSlashes(file.getLocation().toString());
	}

	public static IFile findFileInContainerOrParent(IResource resource, IPath name) throws CoreException {
		IContainer parent = getContainer(resource);
		return findFileInContainerOrParent(parent, name);
	}

	private static IContainer getContainer(IResource resource) {
		if (resource instanceof IContainer) {
			return (IContainer) resource;
		}
		return resource.getParent();
	}

	public static IFile findFileInContainerOrParent(IContainer container, IPath name) throws CoreException {
		if (container == null || container.getType() == IResource.ROOT) {
			// container is null, or it's workspace root.
			return null;
		}
		IFile file = container.getFile(name);
		if (file != null && file.exists()) {
			return file;
		}
		return findFileInContainerOrParent(container.getParent(), name);
	}

	public static IFile findFileFromWorkspace(String path) {
		if (StringUtils.isEmpty(path)) {
			return null;
		}
		IPath filePath = new Path(path);
		return findFileFromWorkspace(filePath);
	}

	public static IFile findFileFromWorkspace(IPath filePath) {
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IFile file = root.getFile(filePath);
		if (file.exists()) {
			return file;
		}
		IFile[] files = ResourcesPlugin.getWorkspace().getRoot().findFilesForLocation(filePath);
		if (files.length > 0) {
			file = files[0];
			if (file.exists()) {
				return file;
			}
		}
		return null;
	}

	public static IContainer findContainerFromWorkspace(String path) {
		if (StringUtils.isEmpty(path)) {
			return null;
		}
		IPath containerPath = new Path(path);
		return findContainerFromWorkspace(containerPath);
	}

	public static IContainer findContainerFromWorkspace(IPath containerPath) {
		if (containerPath == null) {
			return null;
		}
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IContainer container = root.getContainerForLocation(containerPath);
		if (container != null && container.exists()) {
			return container;
		}
		IContainer[] containers = ResourcesPlugin.getWorkspace().getRoot().findContainersForLocation(containerPath);
		if (containers.length > 0) {
			container = containers[0];
			if (container.exists()) {
				return container;
			}
		}
		return null;
	}

	public static File findFileFormFileSystem(String path) {
		if (StringUtils.isEmpty(path)) {
			return null;
		}
		File file = new File(path);
		return (file.exists() && file.isFile()) ? file : null;
	}

	public static IPath getRelativePath(IResource resource, IContainer parent) {
		return resource.getLocation().makeRelativeTo(parent.getLocation());
	}

	public static File resolvePath(String path, IProject project) {
		if (!StringUtils.isEmpty(path)) {
			IPath p = TypeScriptCorePlugin.getTypeScriptRepositoryManager().getPath(path, project);
			return p != null ? p.toFile() : new File(path);
		}
		return null;
	}
}
