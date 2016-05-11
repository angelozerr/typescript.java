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
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;

import ts.eclipse.ide.core.TypeScriptCorePlugin;
import ts.eclipse.ide.core.builder.TypeScriptBuilder;
import ts.eclipse.ide.core.preferences.TypeScriptCorePreferenceConstants;
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
	public static boolean isTypeScriptProject(IProject project) {
		if (!project.isAccessible()) {
			return false;
		}
		return IDEResourcesManager.getInstance().isTypeScriptProject(project);
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

	// --------------------------- TypeScript Builder

	/**
	 * 
	 * @param project
	 * @return
	 */
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

	public static void removeTypeScriptBuilder(IProject project) throws CoreException {
		IProjectDescription description = project.getDescription();
		ICommand[] commands = description.getBuildSpec();
		for (int i = 0; i < commands.length; i++) {
			if (TypeScriptBuilder.ID.equals(commands[i].getBuilderName())) {
				// Remove the builder
				ICommand[] newCommands = new ICommand[commands.length - 1];
				System.arraycopy(commands, 0, newCommands, 0, i);
				System.arraycopy(commands, i + 1, newCommands, i, commands.length - i - 1);
				description.setBuildSpec(newCommands);
				project.setDescription(description, null);
			}
		}
	}

	public static void addTypeScriptBuilder(IProject project) throws CoreException {
		IProjectDescription description = project.getDescription();
		ICommand[] commands = description.getBuildSpec();
		ICommand[] newCommands = new ICommand[commands.length + 1];
		System.arraycopy(commands, 0, newCommands, 0, commands.length);
		ICommand command = description.newCommand();
		command.setBuilderName(TypeScriptBuilder.ID);
		newCommands[newCommands.length - 1] = command;
		description.setBuildSpec(newCommands);
		project.setDescription(description, null);
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
	public static boolean isEmittedFile(IFile jsOrJsMapFile) {
		if (!isJsOrJsMapFile(jsOrJsMapFile)) {
			return false;
		}
		String tsFilename = IDEResourcesManager.getInstance().getTypeScriptFilename(jsOrJsMapFile);
		if (StringUtils.isEmpty(tsFilename)) {
			return false;
		}
		return jsOrJsMapFile.getParent().exists(new Path(tsFilename));
	}

	public static Object[] getEmittedFiles(IFile tsFile) throws CoreException {
		if (!isTsOrTsxFile(tsFile)) {
			return null;
		}
		List<IFile> emittedFiles = new ArrayList<IFile>();
		refreshAndCollectEmittedFiles(tsFile, false, emittedFiles);
		return emittedFiles.toArray();
	}

	public static void refreshAndCollectEmittedFiles(IFile tsFile, boolean refresh, List<IFile> emittedFiles)
			throws CoreException {
		if (!isTsOrTsxFile(tsFile)) {
			return;
		}

		// Find tsconfig.json
		IDETsconfigJson tsconfig = findTsconfig(tsFile);
		refreshAndCollectEmittedFiles(tsFile, tsconfig, refresh, emittedFiles);
	}

	public static void refreshAndCollectEmittedFiles(IFile tsFile, IDETsconfigJson tsconfig, boolean refresh,
			List<IFile> emittedFiles) throws CoreException {
		IContainer baseDir = tsFile.getParent();
		IContainer outDir = tsFile.getParent();
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
		refreshAndCollect(jsFilePath, outDir, refresh, emittedFiles);
		// Check if *js.map file compiled exists
		IPath jsMapFilePath = tsFileNamePath.addFileExtension(FileUtils.JS_EXTENSION)
				.addFileExtension(FileUtils.MAP_EXTENSION);
		refreshAndCollect(jsMapFilePath, outDir, refresh, emittedFiles);

		if (refresh) {
			tsFile.refreshLocal(IResource.DEPTH_INFINITE, null);
		}
	}

	private static void refreshAndCollect(IPath filePath, IContainer baseDir, boolean refresh, List<IFile> emittedFiles)
			throws CoreException {
		IFile file = null;
		if (refresh) {
			file = baseDir.getFile(filePath);
			file.refreshLocal(IResource.DEPTH_INFINITE, null);
			if (file.exists()) {
				file.setDerived(true, null);
			}
		}

		if (emittedFiles != null) {
			if (file == null && baseDir.exists(filePath)) {
				file = baseDir.getFile(filePath);
			}
			if (file != null) {
				emittedFiles.add(file);
			}
		}
	}

	public static IDETsconfigJson findTsconfig(IResource resource) throws CoreException {
		return JsonConfigResourcesManager.getInstance().findTsconfig(resource);
	}

	public static IContainer getBuildPathContainer(Object receiver) {
		if (receiver instanceof IAdaptable) {
			IResource resource = (IResource) ((IAdaptable) receiver).getAdapter(IResource.class);
			if (resource != null) {
				switch (resource.getType()) {
				case IResource.PROJECT:
				case IResource.FOLDER:
					IContainer container = (IContainer) resource;
					if (container.exists(new Path(FileUtils.TSCONFIG_JSON))) {
						return container;
					}
				case IResource.FILE:
					if (isTsConfigFile(resource)) {
						return resource.getParent();
					}
				}
			}
		}
		return null;
	}

	public static boolean isTsConfigFile(IResource resource) {
		return resource.getType() == IResource.FILE && FileUtils.TSCONFIG_JSON.equals(resource.getName());
	}

	public static String getBuildPathLabel(IContainer container) {
		if (container.getType() == IResource.PROJECT) {
			return container.getName();
		}
		return new StringBuilder("").append(container.getProjectRelativePath().toString()).toString();
	}

}
