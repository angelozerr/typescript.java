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
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IDocument;

import ts.TypeScriptException;
import ts.client.ITypeScriptServiceClient;
import ts.compiler.ITypeScriptCompiler;
import ts.eclipse.ide.core.TypeScriptCorePlugin;
import ts.eclipse.ide.core.compiler.IIDETypeScriptCompiler;
import ts.eclipse.ide.core.console.ITypeScriptConsoleConnector;
import ts.eclipse.ide.core.resources.IIDETypeScriptFile;
import ts.eclipse.ide.core.resources.IIDETypeScriptProject;
import ts.eclipse.ide.core.resources.IIDETypeScriptProjectSettings;
import ts.eclipse.ide.core.resources.buildpath.ITypeScriptBuildPath;
import ts.eclipse.ide.core.resources.jsconfig.IDETsconfigJson;
import ts.eclipse.ide.core.resources.watcher.IFileWatcherListener;
import ts.eclipse.ide.core.resources.watcher.ProjectWatcherListenerAdapter;
import ts.eclipse.ide.core.utils.TypeScriptResourceUtil;
import ts.eclipse.ide.core.utils.WorkbenchResourceUtil;
import ts.eclipse.ide.internal.core.Trace;
import ts.eclipse.ide.internal.core.compiler.IDETypeScriptCompiler;
import ts.eclipse.ide.internal.core.console.TypeScriptConsoleConnectorManager;
import ts.eclipse.ide.internal.core.resources.jsonconfig.JsonConfigResourcesManager;
import ts.resources.TypeScriptProject;
import ts.utils.FileUtils;

/**
 * IDE TypeScript project implementation.
 *
 */
public class IDETypeScriptProject extends TypeScriptProject implements IIDETypeScriptProject {

	private final static Map<IProject, IDETypeScriptProject> tsProjects = new HashMap<IProject, IDETypeScriptProject>();

	private IFileWatcherListener tsconfigFileListener = new IFileWatcherListener() {

		@Override
		public void onDeleted(IFile file) {
			// on delete of "tsconfig.json"
			// stope the tsserver
			IDETypeScriptProject.this.disposeServer();
			// Remove cache of tsconfig.json Pojo
			JsonConfigResourcesManager.getInstance().remove(file);
			// Update build path
			ITypeScriptBuildPath buildPath = getTypeScriptBuildPath().copy();
			buildPath.removeEntry(file);
			buildPath.save();
		}

		@Override
		public void onAdded(IFile file) {
			// on create of "tsconfig.json"
			// stope the tsserver
			IDETypeScriptProject.this.disposeServer();
			// Remove cache of tsconfig.json Pojo
			JsonConfigResourcesManager.getInstance().remove(file);
			// Update build path
			// Cannot update build path when tsconfig.json file is created
			// because
			// when project is opening, it is called too
			// ITypeScriptBuildPath buildPath = getTypeScriptBuildPath().copy();
			// buildPath.addEntry(new
			// TypeScriptBuildPathEntry(file.getParent().getProjectRelativePath()));
			// ((IDETypeScriptProjectSettings)
			// getProjectSettings()).updateBuildPath(buildPath);
		}

		@Override
		public void onChanged(IFile file) {
			IDETypeScriptProject.this.disposeServer();
			JsonConfigResourcesManager.getInstance().remove(file);
		}
	};

	private final IProject project;

	private ITypeScriptBuildPath buildPath;

	public IDETypeScriptProject(IProject project) throws CoreException {
		super(project.getLocation().toFile(), null);
		this.project = project;
		super.setProjectSettings(new IDETypeScriptProjectSettings(this));
		synchronized (tsProjects) {
			tsProjects.put(project, this);
		}
		// Stop tsserver + dispose settings when project is closed, deleted.
		TypeScriptCorePlugin.getResourcesWatcher().addProjectWatcherListener(getProject(),
				new ProjectWatcherListenerAdapter() {

					@Override
					public void onClosed(IProject project) {
						try {
							dispose();
						} catch (TypeScriptException e) {
							Trace.trace(Trace.SEVERE, "Error while closing project", e);
						}
					}

					@Override
					public void onDeleted(IProject project) {
						try {
							dispose();
						} catch (TypeScriptException e) {
							Trace.trace(Trace.SEVERE, "Error while deleting project", e);
						}
					}

					private void dispose() throws TypeScriptException {
						IDETypeScriptProject.this.dispose();
						synchronized (tsProjects) {
							tsProjects.remove(IDETypeScriptProject.this.getProject());
						}
					}

				});
		// Stop tsserver when tsconfig.json/jsconfig.json of the project is
		// created, deleted or modified
		TypeScriptCorePlugin.getResourcesWatcher().addFileWatcherListener(getProject(), FileUtils.TSCONFIG_JSON,
				tsconfigFileListener);
		TypeScriptCorePlugin.getResourcesWatcher().addFileWatcherListener(getProject(), FileUtils.JSCONFIG_JSON,
				tsconfigFileListener);
	}

	/**
	 * Returns the Eclispe project.
	 * 
	 * @return
	 */
	@Override
	public IProject getProject() {
		return project;
	}

	public static IDETypeScriptProject getTypeScriptProject(IProject project) throws CoreException {
		synchronized (tsProjects) {
			return tsProjects.get(project);
		}
	}

	public void load() throws IOException {

	}

	@Override
	public synchronized IIDETypeScriptFile openFile(IResource file, IDocument document) throws TypeScriptException {
		IIDETypeScriptFile tsFile = getOpenedFile(file);
		if (tsFile == null) {
			tsFile = new IDETypeScriptFile(file, document, this);
		}
		if (!tsFile.isOpened()) {
			tsFile.open();
		}
		return tsFile;
	}

	@Override
	public IIDETypeScriptFile getOpenedFile(IResource file) {
		String fileName = WorkbenchResourceUtil.getFileName(file);
		return (IIDETypeScriptFile) super.getOpenedFile(fileName);
	}

	@Override
	public void closeFile(IResource file) throws TypeScriptException {
		IIDETypeScriptFile tsFile = getOpenedFile(file);
		if (tsFile != null) {
			tsFile.close();
		}
	}

	@Override
	protected void onCreateClient(ITypeScriptServiceClient client) {
		configureConsole();
	}

	@Override
	public void configureConsole() {
		synchronized (serverLock) {
			if (hasClient()) {
				// There is a TypeScript client instance., Retrieve the well
				// connector
				// the
				// the eclipse console.
				try {
					ITypeScriptServiceClient client = getClient();
					ITypeScriptConsoleConnector connector = TypeScriptConsoleConnectorManager.getManager()
							.getConnector(client);
					if (connector != null) {
						if (isTraceOnConsole()) {
							// connect the tsserver to the eclipse console.
							connector.connectToConsole(client, this);
						} else {
							// disconnect the tsserver to the eclipse
							// console.
							connector.disconnectToConsole(client, this);
						}
					}
				} catch (TypeScriptException e) {
					Trace.trace(Trace.SEVERE, "Error while getting TypeScript client", e);
				}
			}
		}
	}

	private boolean isTraceOnConsole() {
		return getProjectSettings().isTraceOnConsole();
	}

	@Override
	public IIDETypeScriptProjectSettings getProjectSettings() {
		return (IIDETypeScriptProjectSettings) super.getProjectSettings();
	}

	@Override
	public boolean isInScope(IResource resource) {
		try {
			// Use project preferences, which defines include/exclude path
			if (!getTypeScriptBuildPath().isInScope(resource)) {
				return false;
			}
			boolean isJSFile = IDEResourcesManager.getInstance().isJsFile(resource)
					|| IDEResourcesManager.getInstance().isJsxFile(resource);
			if (isJSFile) {
				// Can validate js file?
				return isJsFileIsInScope(resource);
			}
			// is ts file is included ?
			return isTsFileIsInScope(resource);
		} catch (CoreException e) {
			Trace.trace(Trace.SEVERE, "Error while getting tsconfig.json for canValidate", e);
		}
		return true;
	}

	/**
	 * Returns true if the given js, jsx file can be validated and false
	 * otherwise.
	 * 
	 * @param resource
	 * @return true if the given js, jsx file can be validated and false
	 *         otherwise.
	 * @throws CoreException
	 */
	private boolean isJsFileIsInScope(IResource resource) throws CoreException {
		// Search if a jsconfig.json exists?
		IFile jsconfigFile = JsonConfigResourcesManager.getInstance().findJsconfigFile(resource);
		if (jsconfigFile != null) {
			return true;
		}
		// Search if tsconfig.json exists and defines alloyJs
		IDETsconfigJson tsconfig = TypeScriptResourceUtil.findTsconfig(resource);
		if (tsconfig != null && tsconfig.getCompilerOptions() != null && tsconfig.getCompilerOptions().isAllowJs()) {
			return true;
		}
		// jsconfig.json was not found (ex : MyProject/node_modules),
		// validation must not be done.
		return false;
	}

	/**
	 * Returns true if the given ts, tsx file can be validated and false
	 * otherwise.
	 * 
	 * @param resource
	 * @return true if the given ts, tsx file can be validated and false
	 *         otherwise.
	 * @throws CoreException
	 */
	private boolean isTsFileIsInScope(IResource resource) throws CoreException {
		IDETsconfigJson tsconfig = TypeScriptResourceUtil.findTsconfig(resource);
		if (tsconfig != null) {
			return tsconfig.isInScope(resource);			
		}
		// tsconfig.json was not found (ex : MyProject/node_modules),
		// validation must not be done.
		return false;
	}

	@Override
	public ITypeScriptBuildPath getTypeScriptBuildPath() {
		if (buildPath == null) {
			buildPath = ((IDETypeScriptProjectSettings) getProjectSettings()).getTypeScriptBuildPath();
		}
		return buildPath;
	}

	public void disposeBuildPath() {
		ITypeScriptBuildPath oldBuildPath = getTypeScriptBuildPath();
		buildPath = null;
		ITypeScriptBuildPath newBuildPath = getTypeScriptBuildPath();
		IDEResourcesManager.getInstance().fireBuildPathChanged(this, oldBuildPath, newBuildPath);
	}
	
	@Override
	public IIDETypeScriptCompiler getCompiler() throws TypeScriptException {
		return (IIDETypeScriptCompiler) super.getCompiler();
	}
	
	@Override
	protected ITypeScriptCompiler createCompiler(File tscFile, File nodejsFile) {
		return new IDETypeScriptCompiler(tscFile, nodejsFile);
	}
}
