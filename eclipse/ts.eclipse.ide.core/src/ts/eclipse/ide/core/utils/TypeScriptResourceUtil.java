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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;

import ts.eclipse.ide.core.TypeScriptCorePlugin;
import ts.eclipse.ide.core.builder.TypeScriptBuilder;
import ts.eclipse.ide.core.resources.IIDETypeScriptProject;
import ts.eclipse.ide.core.resources.jsconfig.IDETsconfigJson;
import ts.eclipse.ide.internal.core.resources.IDEResourcesManager;
import ts.eclipse.ide.internal.core.resources.jsonconfig.JsonConfigResourcesManager;
import ts.resources.TypeScriptResourcesManager;
import ts.utils.FileUtils;
import ts.utils.StringUtils;

/**
 * TypeScript resource utilities.
 *
 */
public class TypeScriptResourceUtil {

	public static boolean isTsOrTsxFile(Object element) {
		return IDEResourcesManager.getInstance().isTsOrTsxFile(element);
	}

	public static boolean isTsOrTsxOrJsxFile(Object element) {
		return IDEResourcesManager.getInstance().isTsOrTsxOrJsxFile(element);
	}

	public static boolean isJsOrJsMapFile(Object element) {
		return IDEResourcesManager.getInstance().isJsOrJsMapFile(element);
	}

	/**
	 * Return true if the given project contains a "tsconfig.json" file false
	 * otherwise.
	 * 
	 * @param project
	 *            Eclipse project.
	 * @return true if the given project contains a "tsconfig.json" file and
	 *         false otherwise.
	 */
	public static boolean hasTypeScriptNature(IProject project) {
		return IDEResourcesManager.getInstance().hasTypeScriptNature(project);
	}

	public static boolean canConsumeTsserver(IProject project, Object fileObject) {
		return IDEResourcesManager.getInstance().canConsumeTsserver(project, fileObject);
	}

	public static boolean canConsumeTsserver(IResource resource) {
		if (resource == null) {
			return false;
		}
		return canConsumeTsserver(resource.getProject(), resource);
	}

	public static boolean hasTypeScriptBuilder(IProject project) {
		try {
			IProjectDescription description = project.getDescription();
			ICommand[] commands = description.getBuildSpec();
			for (int i = 0; i < commands.length; i++) {
				if (TypeScriptBuilder.ID.equals(commands[i].getBuilderName())) {
					return true;
				}
			}
		} catch (CoreException e) {
			return false;
		}
		return false;
	}

	/**
	 * Returns the TypeScript project of the given eclipse project and throws
	 * exception if the eclipse project has not TypeScript nature.
	 * 
	 * @param project
	 *            eclipse project.
	 * @return the TypeScript project of the given eclipse projectand throws
	 *         exception if the eclipse project has not TypeScript nature.
	 * @throws CoreException
	 */
	public static IIDETypeScriptProject getTypeScriptProject(IProject project, boolean force) throws CoreException {
		try {
			return (IIDETypeScriptProject) TypeScriptResourcesManager.getTypeScriptProject(project, force);
		} catch (Exception e) {
			throw new CoreException(new Status(IStatus.ERROR, TypeScriptCorePlugin.PLUGIN_ID,
					"The project " + project.getName() + " cannot be converted as TypeScript project.", e));
		}
	}

	/**
	 * Returns the TypeScript project of the given eclipse project and throws
	 * exception if the eclipse project has not TypeScript nature.
	 * 
	 * @param project
	 *            eclipse project.
	 * @return the TypeScript project of the given eclipse projectand throws
	 *         exception if the eclipse project has not TypeScript nature.
	 * @throws CoreException
	 */
	public static IIDETypeScriptProject getTypeScriptProject(IProject project) throws CoreException {
		IIDETypeScriptProject result = (IIDETypeScriptProject) TypeScriptResourcesManager.getTypeScriptProject(project);
		if (result == null) {
			throw new CoreException(new Status(IStatus.ERROR, TypeScriptCorePlugin.PLUGIN_ID,
					"The project " + project.getName() + " is not a TypeScript project."));
		}
		return result;
	}

	/**
	 * Returns true if the given *.js file or *.js.map have a corresponding *.ts
	 * file in the same folder and false otherwise.
	 * 
	 * @param jsOrJsMapFile
	 *            *.js file or *.js.map file.
	 * @return true if the given *.js file or *.js.map have a corresponding *.ts
	 *         file in the same folder and false otherwise.
	 */
	public static boolean isCompiledTypeScriptResource(IFile jsOrJsMapFile) {
		if (!isJsOrJsMapFile(jsOrJsMapFile)) {
			return false;
		}
		String tsFilename = IDEResourcesManager.getInstance().getTypeScriptFilename(jsOrJsMapFile);
		if (StringUtils.isEmpty(tsFilename)) {
			return false;
		}
		return jsOrJsMapFile.getParent().exists(new Path(tsFilename));
	}

	public static Object[] getCompiledTypesScriptResources(IFile tsFile) throws CoreException {
		if (!isTsOrTsxFile(tsFile)) {
			return null;
		}
		List<IFile> compiledFiles = new ArrayList<IFile>();
		refreshAndCollectCompiledFiles(tsFile, false, compiledFiles);
		return compiledFiles.toArray();
	}

	public static void refreshAndCollectCompiledFiles(IFile tsFile, boolean refresh, List<IFile> compiledFiles)
			throws CoreException {
		if (!isTsOrTsxFile(tsFile)) {
			return;
		}

		IContainer baseDir = tsFile.getParent();
		IContainer outDir = tsFile.getParent();

		// Find tsconfig.json
		IDETsconfigJson tsconfig = findTsconfig(tsFile);
		if (tsconfig != null) {
			// tsconfig.json is found and "outDir" is setted, check if *.js and
			// *.js.map file in the "outDir" folder.
			IContainer configOutDir = tsconfig.getOutDirContainer();
			if (configOutDir != null && configOutDir.exists()) {
				outDir = configOutDir;
			}
		}

		IPath tsFileNamePath = WorkbenchResourceUtil.getRelativePath(tsFile, baseDir).removeFileExtension();
		// Check if *js file compiled exists
		IPath jsFilePath = tsFileNamePath.addFileExtension(FileUtils.JS_EXTENSION);
		refreshAndCollect(jsFilePath, outDir, refresh, compiledFiles);
		// Check if *js.map file compiled exists
		IPath jsMapFilePath = tsFileNamePath.addFileExtension(FileUtils.JS_EXTENSION)
				.addFileExtension(FileUtils.MAP_EXTENSION);
		refreshAndCollect(jsMapFilePath, outDir, refresh, compiledFiles);

		if (refresh) {
			tsFile.refreshLocal(IResource.DEPTH_INFINITE, null);
		}
	}

	private static void refreshAndCollect(IPath jsFilePath, IContainer baseDir, boolean refresh,
			List<IFile> compiledFiles) throws CoreException {
		IFile file = null;
		if (refresh) {
			file = baseDir.getFile(jsFilePath);
			if (!file.exists()) {
				file.refreshLocal(IResource.DEPTH_INFINITE, null);
			}
		}

		if (compiledFiles != null) {
			if (file == null && baseDir.exists(jsFilePath)) {
				file = baseDir.getFile(jsFilePath);
			}
			if (file != null) {
				compiledFiles.add(file);
			}
		}
	}

	public static IDETsconfigJson findTsconfig(IResource resource) throws CoreException {
		return JsonConfigResourcesManager.getInstance().findTsconfig(resource);
	}
}
