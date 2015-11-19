/**
 *  Copyright (c) 2013-2015 Angelo ZERR.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package ts.eclipse.ide.core;

import java.io.File;
import java.io.IOException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.osgi.framework.BundleContext;

import ts.eclipse.ide.core.resources.IIDETypeScriptProject;
import ts.eclipse.ide.internal.core.resources.IDEResourcesManager;
import ts.resources.ConfigurableTypeScriptResourcesManager;
import ts.resources.TypeScriptResourcesManager;

/**
 * The activator class controls the plug-in life cycle
 */
public class TypeScriptCorePlugin extends Plugin {

	public static final String PLUGIN_ID = "ts.eclipse.ide.core"; //$NON-NLS-1$

	// The shared instance.
	private static TypeScriptCorePlugin plugin;

	/**
	 * The constructor.
	 */
	public TypeScriptCorePlugin() {
		super();
		plugin = this;
	}

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);

		// set up resource management for IDE
		ConfigurableTypeScriptResourcesManager resourceManager = ConfigurableTypeScriptResourcesManager.getInstance();
		resourceManager.setTypeScriptResourcesManagerDelegate(IDEResourcesManager.getInstance());
	}

	/**
	 * Returns the TypeScript repository base directory.
	 * 
	 * @return the TypeScript repository base directory.
	 * @throws IOException
	 */
	public static File getTypeScriptRepositoryBaseDir() throws IOException {
		return FileLocator.getBundleFile(Platform.getBundle("ts.repository"));
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		// NodejsProcessManager.getInstance().dispose();
		// TSServerTypeManager.getManager().destroy();
		// TSNatureAdaptersManager.getManager().destroy();
		// TSFileConfigurationManager.getManager().destroy();
		// IDETSProjectSynchronizer.getInstance().dispose();
		// TSModuleInstallManager.getManager().destroy();
		// TSRepositoryManager.getManager().dispose();

		plugin = null;
		super.stop(context);
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
		return true; // return IDETSProject.hasTypeScriptNature(project);
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
	 * Returns the shared instance
	 * 
	 * @return the shared instance
	 */
	public static TypeScriptCorePlugin getDefault() {
		return plugin;
	}

	/**
	 * Returns the instance of the TypeScript server type manager.
	 * 
	 * @return the instance of the TypeScript server type manager.
	 */
	// public static ITSServerTypeManager getTSServerTypeManager() {
	// return TSServerTypeManager.getManager();
	// }
	//
	// public static void addTSProjectLifeCycleListener(
	// ITSProjectLifecycleListener listener) {
	// TSProjectLifecycleManager.getManager()
	// .addTSProjectLifeCycleListener(listener);
	// }
	//
	// public static void removeTSProjectLifeCycleListener(
	// ITSProjectLifecycleListener listener) {
	// TSProjectLifecycleManager.getManager()
	// .removeTSProjectLifeCycleListener(listener);
	//
	// }
	//
	// public static ITSRepositoryManager getTSRepositoryManager() {
	// return TSRepositoryManager.getManager();
	// }

}
