/**
 *  Copyright (c) 2015-2016 Angelo ZERR.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 */
package ts.eclipse.ide.internal.core.resources;

import java.io.File;
import java.io.IOException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;

import ts.eclipse.ide.core.resources.IIDETypeScriptProject;
import ts.eclipse.ide.core.resources.TypeScriptSettingsHelper;
import ts.eclipse.ide.core.resources.UseSalsa;
import ts.eclipse.ide.internal.core.Trace;
import ts.resources.ITypeScriptProject;
import ts.resources.ITypeScriptResourcesManagerDelegate;
import ts.utils.FileUtils;

public class IDEResourcesManager
		implements ITypeScriptResourcesManagerDelegate, IResourceChangeListener, IResourceDeltaVisitor {

	private static IDEResourcesManager instance = new IDEResourcesManager();

	private IDEResourcesManager() {
		ResourcesPlugin.getWorkspace().addResourceChangeListener(this);
	}

	public void dispose() {
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
	}

	public static IDEResourcesManager getInstance() {
		return instance;
	}

	@Override
	public ITypeScriptProject getTypeScriptProject(Object obj, boolean force) throws IOException {
		if (obj instanceof IProject) {
			IProject project = (IProject) obj;
			try {
				if (force) {
					// Dispose TypeScript project if exists
					IDETypeScriptProject tsProject = getTypeScriptProject(project);
					if (tsProject != null) {
						tsProject.dispose();
					}
				}
				IDETypeScriptProject tsProject = getTypeScriptProject(project);
				if (tsProject == null) {
					tsProject = new IDETypeScriptProject(project);
					try {
						tsProject.load();
					} catch (IOException e) {
						Trace.trace(Trace.SEVERE, "Error while loading TypeScript project", e);
						throw e;
					}
				}
				return tsProject;
			} catch (Exception ex) {
				Trace.trace(Trace.SEVERE,
						"Error while creating TypeScript ptoject [" + project.getName() + "]: " + ex.getMessage(), ex);
			}
		}
		return null;
	}

	public boolean hasTypeScriptNature(IProject project) {
		// see https://github.com/angelozerr/typescript.java/issues/12
		// FIXME: All projects can be a TypeScript project. It means that
		// project
		// properties display every time "TypeScript" menu item. But is it a
		// problem?
		// To hide the TypeScript menu item we could check that project contains
		// tsconfig.json or src/tsconfig.json
		// User could add a new path for tsconfig.json in the preferences but
		// I'm afrais that it's a little complex.
		return true;
	}

	public boolean hasSalsaNature(IProject project) {
		UseSalsa useSalsa = TypeScriptSettingsHelper.getUseSalsa();
		switch (useSalsa) {
		case Never:
			return false;
		case EveryTime:
			return true;
		case WhenNoJSDTNature:
			try {
				return !project.hasNature("org.eclipse.wst.jsdt.core.jsNature");
			} catch (CoreException e) {
				return false;
			}
		}
		return false;
	}

	private IDETypeScriptProject getTypeScriptProject(IProject project) throws CoreException {
		return IDETypeScriptProject.getTypeScriptProject(project);
	}

	protected String getExtension(Object fileObject) {
		if (fileObject instanceof IFile) {
			return ((IFile) fileObject).getFileExtension();
		} else if (fileObject instanceof File) {
			return FileUtils.getFileExtension(((File) fileObject).getName());
		} else if (fileObject instanceof String) {
			return FileUtils.getFileExtension((String) fileObject);
		}
		return null;
	}

	@Override
	public boolean isTsFile(Object fileObject) {
		String ext = getExtension(fileObject);
		return ext != null && FileUtils.TS_EXTENSION.equals(ext.toLowerCase());
	}

	@Override
	public boolean isJsFile(Object fileObject) {
		String ext = getExtension(fileObject);
		return ext != null && FileUtils.JS_EXTENSION.equals(ext.toLowerCase());
	}

	@Override
	public void resourceChanged(IResourceChangeEvent event) {
		try {
			IResource resource = event.getResource();
			switch (event.getType()) {
			case IResourceChangeEvent.PRE_DELETE:
				// called when project is deleted.
			case IResourceChangeEvent.PRE_CLOSE:
				// called when project is closed.
				if (resource != null && resource.getType() == IResource.PROJECT) {
					IProject project = (IProject) resource;
					closeProject(project);
				}
				break;
			case IResourceChangeEvent.POST_CHANGE:
				IResourceDelta delta = event.getDelta();
				if (delta != null) {
					delta.accept(this);
				}
				break;
			}
		} catch (Throwable e) {
			Trace.trace(Trace.SEVERE, "Error while TypeScript resource changed", e);
		}
	}

	private void closeProject(IProject project) {
		try {
			IIDETypeScriptProject tsProject = IDETypeScriptProject.getTypeScriptProject(project);
			if (tsProject != null) {
				tsProject.dispose();
			}
		} catch (Throwable e) {
			Trace.trace(Trace.SEVERE, "Error while disposing TypeScript project", e);
		}

	}

	@Override
	public boolean visit(IResourceDelta delta) throws CoreException {
		IResource resource = delta.getResource();
		if (resource == null) {
			return false;
		}
		switch (resource.getType()) {
		case IResource.ROOT:
			return true;
		}
		return false;
	}

	public boolean canConsumeTsserver(IProject project, Object fileObject) {
		if (isJsFile(fileObject)) {
			return hasSalsaNature(project);
		}
		return true;
	}

}
