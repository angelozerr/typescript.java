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

import java.io.IOException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.jface.text.IDocument;

import ts.TypeScriptException;
import ts.client.ITypeScriptServiceClient;
import ts.eclipse.ide.core.TypeScriptCorePlugin;
import ts.eclipse.ide.core.console.ITypeScriptConsoleConnector;
import ts.eclipse.ide.core.resources.IIDETypeScriptFile;
import ts.eclipse.ide.core.resources.IIDETypeScriptProject;
import ts.eclipse.ide.core.resources.IIDETypeScriptProjectSettings;
import ts.eclipse.ide.core.resources.jsconfig.IDETsconfigJson;
import ts.eclipse.ide.core.resources.watcher.IFileWatcherListener;
import ts.eclipse.ide.core.resources.watcher.ProjectWatcherListenerAdapter;
import ts.eclipse.ide.internal.core.Trace;
import ts.eclipse.ide.internal.core.console.TypeScriptConsoleConnectorManager;
import ts.eclipse.ide.internal.core.resources.jsonconfig.JsonConfigResourcesManager;
import ts.resources.TypeScriptProject;
import ts.resources.jsonconfig.TsconfigJson;
import ts.utils.FileUtils;

/**
 * IDE TypeScript project implementation.
 *
 */
public class IDETypeScriptProject extends TypeScriptProject implements IIDETypeScriptProject {

	private static final QualifiedName TYPESCRIPT_PROJECT = new QualifiedName(
			TypeScriptCorePlugin.PLUGIN_ID + ".sessionprops", //$NON-NLS-1$
			"TypeScriptProject"); //$NON-NLS-1$

	private IFileWatcherListener tsconfigFileListener = new IFileWatcherListener() {

		@Override
		public void onDeleted(IFile file) {
			IDETypeScriptProject.this.disposeServer();
			JsonConfigResourcesManager.getInstance().remove(file);
		}

		@Override
		public void onCreate(IFile file) {
			IDETypeScriptProject.this.disposeServer();
			JsonConfigResourcesManager.getInstance().remove(file);
		}

		@Override
		public void onChanged(IFile file) {
			IDETypeScriptProject.this.disposeServer();
			JsonConfigResourcesManager.getInstance().remove(file);
		}
	};

	private final IProject project;

	public IDETypeScriptProject(IProject project) throws CoreException {
		super(project.getLocation().toFile(), null);
		this.project = project;
		super.setProjectSettings(new IDETypeScriptProjectSettings(this));
		project.setSessionProperty(TYPESCRIPT_PROJECT, this);
		// Stop tsserver + dispose settings when project is closed, deleted.
		TypeScriptCorePlugin.getResourcesWatcher().addProjectWatcherListener(getProject(),
				new ProjectWatcherListenerAdapter() {
					@Override
					public void onClosed(IProject project) {
						try {
							IDETypeScriptProject.this.dispose();
						} catch (TypeScriptException e) {
							Trace.trace(Trace.SEVERE, "Error while closing project", e);
						}
					}

					@Override
					public void onDeleted(IProject project) {
						try {
							IDETypeScriptProject.this.dispose();
						} catch (TypeScriptException e) {
							Trace.trace(Trace.SEVERE, "Error while deleting project", e);
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
		if (!project.isAccessible()) {
			return null;
		}
		return (IDETypeScriptProject) project.getSessionProperty(TYPESCRIPT_PROJECT);
	}

	public void load() throws IOException {

	}

	@Override
	public synchronized IIDETypeScriptFile openFile(IResource file, IDocument document) throws TypeScriptException {
		String fileName = TypeScriptCorePlugin.getFileName(file);
		IIDETypeScriptFile tsFile = (IIDETypeScriptFile) super.getOpenedFile(fileName);
		if (tsFile == null) {
			tsFile = new IDETypeScriptFile(file, document, this);
		}
		if (!tsFile.isOpened()) {
			tsFile.open();
		}
		return tsFile;
	}

	@Override
	public void closeFile(IResource file) throws TypeScriptException {
		String fileName = TypeScriptCorePlugin.getFileName(file);
		IIDETypeScriptFile tsFile = (IIDETypeScriptFile) super.getOpenedFile(fileName);
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
	public boolean canValidate(IResource resource) {
		try {
			IDETsconfigJson tsconfig = JsonConfigResourcesManager.getInstance().findTsconfig(resource);
			if (tsconfig != null) {
				// check if the given file is declared in the "files"
				if (tsconfig.hasFiles()) {
					return tsconfig.isInFiles(resource);
				} else if (tsconfig.hasExclude()) {
					return !tsconfig.isExcluded(resource);
				}
			}
		} catch (CoreException e) {
			Trace.trace(Trace.SEVERE, "Error while getting tsconfig.json for canValidate", e);
		}
		return true;
	}

	@Override
	public boolean canCompileOnSave(IResource resource) {
		try {
			TsconfigJson tsconfig = JsonConfigResourcesManager.getInstance().findTsconfig(resource);
			return tsconfig != null ? tsconfig.isCompileOnSave() : null;
		} catch (CoreException e) {
			Trace.trace(Trace.SEVERE, "Error while getting tsconfig.json for canCompileOnSave", e);
			return false;
		}
	}

}
