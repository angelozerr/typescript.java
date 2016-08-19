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
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;

import ts.eclipse.ide.core.preferences.TypeScriptCorePreferenceConstants;
import ts.eclipse.ide.core.resources.IIDETypeScriptProject;
import ts.eclipse.ide.core.resources.ITypeScriptElementChangedListener;
import ts.eclipse.ide.core.resources.UseSalsa;
import ts.eclipse.ide.core.resources.WorkspaceTypeScriptSettingsHelper;
import ts.eclipse.ide.core.resources.buildpath.ITypeScriptBuildPath;
import ts.eclipse.ide.internal.core.Trace;
import ts.resources.ITypeScriptResourcesManagerDelegate;
import ts.utils.FileUtils;

public class IDEResourcesManager implements ITypeScriptResourcesManagerDelegate {

	private static IDEResourcesManager instance = new IDEResourcesManager();

	private final List<ITypeScriptElementChangedListener> listeners;

	public IDEResourcesManager() {
		this.listeners = new ArrayList<ITypeScriptElementChangedListener>();
	}

	public static IDEResourcesManager getInstance() {
		return instance;
	}

	@Override
	public IDETypeScriptProject getTypeScriptProject(Object obj, boolean force) throws IOException {
		if (obj instanceof IProject) {
			IProject project = (IProject) obj;
			if (project.getLocation() == null) {
				return null;
			}
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
						"Error while creating TypeScript project [" + project.getName() + "]: " + ex.getMessage(), ex);
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

	/**
	 * Returns true if the given project contains one or several "tsconfig.json"
	 * file(s) false otherwise.
	 * 
	 * To have a very good performance, "tsconfig.json" is not searched by
	 * scanning the whole files of the project but it checks if "tsconfig.json"
	 * exists in several folders ('/tsconfig.json' or '/src/tsconfig.json).
	 * Those folders can be customized with preferences buildpath
	 * {@link TypeScriptCorePreferenceConstants#TYPESCRIPT_BUILD_PATH}.
	 * 
	 * @param project
	 *            Eclipse project.
	 * @return true if the given project contains one or several "tsconfig.json"
	 *         file(s) false otherwise.
	 */
	public boolean isTypeScriptProject(IProject project) {
		// check that TypeScript project have build path.
		try {
			IDETypeScriptProject tsProject = getTypeScriptProject(project, false);
			return tsProject != null && tsProject.getTypeScriptBuildPath().hasRootContainers();
		} catch (Exception e) {
			Trace.trace(Trace.SEVERE, "Error while getting TypeScript project", e);
		}
		return false;
	}

	public boolean hasSalsaNature(IProject project) {
		UseSalsa useSalsa = WorkspaceTypeScriptSettingsHelper.getUseSalsa();
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

	public void addTypeScriptElementChangedListener(ITypeScriptElementChangedListener listener) {
		synchronized (listeners) {
			if (!listeners.contains(listener)) {
				listeners.add(listener);
			}
		}
	}

	public void fireBuildPathChanged(IIDETypeScriptProject tsProject, ITypeScriptBuildPath oldBuildPath,
			ITypeScriptBuildPath newBuildPath) {
		synchronized (listeners) {
			for (ITypeScriptElementChangedListener listener : listeners) {
				listener.buildPathChanged(tsProject, oldBuildPath, newBuildPath);
			}
		}
	}

	public void removeTypeScriptElementChangedListener(ITypeScriptElementChangedListener listener) {
		synchronized (listeners) {
			listeners.remove(listener);
		}
	}
}
