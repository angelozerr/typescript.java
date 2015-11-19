/**
 *  Copyright (c) 2013-2015 Angelo ZERR.
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
import org.eclipse.core.runtime.CoreException;

import ts.eclipse.ide.internal.core.Trace;
import ts.resources.ITypeScriptProject;
import ts.resources.ITypeScriptResourcesManagerDelegate;
import ts.utils.FileExtensionUtils;

public class IDEResourcesManager implements ITypeScriptResourcesManagerDelegate {

	private static IDEResourcesManager instance = new IDEResourcesManager();

	private IDEResourcesManager() {
	}

	public static IDEResourcesManager getInstance() {
		return instance;
	}

	@Override
	public ITypeScriptProject getTypeScriptProject(Object obj, boolean force) throws IOException {
		if (obj instanceof IProject) {
			IProject project = (IProject) obj;
			try {
				if (!IDETypeScriptProject.hasTypeScriptNature(project) && !force) {
					return null;
				}
				if (force) {
					// Dispose TypeScript project if exists
					IDETypeScriptProject ternProject = IDETypeScriptProject.getTypeScriptProject(project);
					if (ternProject != null) {
						ternProject.dispose();
					}
				}
				IDETypeScriptProject tsProject = IDETypeScriptProject.getTypeScriptProject(project);
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
			} catch (CoreException ex) {
				Trace.trace(Trace.SEVERE, "Error creating " + project.getName() + ": " + ex.getMessage(), ex);
			}
		}
		return null;
	}

	protected String getExtension(Object fileObject) {
		if (fileObject instanceof IFile) {
			return ((IFile) fileObject).getFileExtension();
		} else if (fileObject instanceof File) {
			return FileExtensionUtils.getFileExtension(((File) fileObject).getName());
		} else if (fileObject instanceof String) {
			return FileExtensionUtils.getFileExtension((String) fileObject);
		}
		return null;
	}

	@Override
	public boolean isTSFile(Object fileObject) {
		String ext = getExtension(fileObject);
		return ext != null && FileExtensionUtils.TS_EXTENSION.equals(ext.toLowerCase());
	}

}
