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
package ts.eclipse.ide.internal.core.resources;

import java.io.File;
import java.io.IOException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;

import ts.eclipse.ide.core.resources.TypeScriptSettingsHelper;
import ts.eclipse.ide.core.resources.UseSalsa;
import ts.eclipse.ide.internal.core.Trace;
import ts.resources.ITypeScriptProject;
import ts.resources.ITypeScriptResourcesManagerDelegate;
import ts.utils.FileUtils;

public class IDEResourcesManager implements ITypeScriptResourcesManagerDelegate {

	private static IDEResourcesManager instance = new IDEResourcesManager();

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
					tsProject = create(project);
				}
				return tsProject;
			} catch (Exception ex) {
				Trace.trace(Trace.SEVERE,
						"Error while creating TypeScript ptoject [" + project.getName() + "]: " + ex.getMessage(), ex);
			}
		}
		return null;
	}

	private synchronized IDETypeScriptProject create(IProject project) throws CoreException, IOException {
		IDETypeScriptProject tsProject = getTypeScriptProject(project);
		if (tsProject != null) {
			return tsProject;
		}
		tsProject = new IDETypeScriptProject(project);
		try {
			tsProject.load();
		} catch (IOException e) {
			Trace.trace(Trace.SEVERE, "Error while loading TypeScript project", e);
			throw e;
		}
		return tsProject;
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
	public boolean isJsFile(Object fileObject) {
		String ext = getExtension(fileObject);
		return ext != null && FileUtils.JS_EXTENSION.equals(ext.toLowerCase());
	}

	@Override
	public boolean isJsxFile(Object fileObject) {
		String ext = getExtension(fileObject);
		return ext != null && FileUtils.JSX_EXTENSION.equals(ext.toLowerCase());
	}

	@Override
	public boolean isTsFile(Object fileObject) {
		String ext = getExtension(fileObject);
		return ext != null && FileUtils.TS_EXTENSION.equals(ext.toLowerCase());
	}

	@Override
	public boolean isTsxFile(Object fileObject) {
		String ext = getExtension(fileObject);
		return ext != null && FileUtils.TSX_EXTENSION.equals(ext.toLowerCase());
	}

	@Override
	public boolean isTsOrTsxFile(Object fileObject) {
		String ext = getExtension(fileObject);
		ext = ext != null ? ext.toLowerCase() : null;
		return ext != null && (FileUtils.TS_EXTENSION.equals(ext) || FileUtils.TSX_EXTENSION.equals(ext));
	}

	@Override
	public boolean isTsOrTsxOrJsxFile(Object fileObject) {
		String ext = getExtension(fileObject);
		ext = ext != null ? ext.toLowerCase() : null;
		return ext != null && (FileUtils.TS_EXTENSION.equals(ext) || FileUtils.TSX_EXTENSION.equals(ext)
				|| FileUtils.JSX_EXTENSION.equals(ext));
	}

	public boolean isJsOrJsMapFile(Object fileObject) {
		if (fileObject instanceof IFile) {
			return FileUtils.isJsOrJsMapFile(((IFile) fileObject).getName());
		} else if (fileObject instanceof File) {
			return FileUtils.isJsOrJsMapFile(((File) fileObject).getName());
		} else if (fileObject instanceof String) {
			return FileUtils.isJsOrJsMapFile((String) fileObject);
		}
		return false;
	}

	public boolean canConsumeTsserver(IProject project, Object fileObject) {
		if (!project.isAccessible()) {
			return false;
		}
		if (isJsFile(fileObject)) {
			return hasSalsaNature(project);
		}
		return (isTsOrTsxOrJsxFile(fileObject));
	}

	@Override
	public String getTypeScriptFilename(Object fileObject) {
		if (fileObject instanceof IFile) {
			return FileUtils.getTypeScriptFilename(((IFile) fileObject).getName());
		} else if (fileObject instanceof File) {
			return FileUtils.getTypeScriptFilename(((File) fileObject).getName());
		} else if (fileObject instanceof String) {
			return FileUtils.getTypeScriptFilename((String) fileObject);
		}
		return null;
	}

}
