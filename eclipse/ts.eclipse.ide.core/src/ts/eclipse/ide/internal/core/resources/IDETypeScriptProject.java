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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.IDocument;

import ts.TypeScriptException;
import ts.client.ITypeScriptServiceClient;
import ts.client.compileonsave.CompileOnSaveAffectedFileListSingleProject;
import ts.client.diagnostics.Diagnostic;
import ts.client.diagnostics.DiagnosticEventBody;
import ts.cmd.tsc.ITypeScriptCompiler;
import ts.cmd.tslint.ITypeScriptLint;
import ts.eclipse.ide.core.TypeScriptCorePlugin;
import ts.eclipse.ide.core.compiler.IIDETypeScriptCompiler;
import ts.eclipse.ide.core.console.ITypeScriptConsoleConnector;
import ts.eclipse.ide.core.resources.IIDETypeScriptFile;
import ts.eclipse.ide.core.resources.IIDETypeScriptProject;
import ts.eclipse.ide.core.resources.IIDETypeScriptProjectSettings;
import ts.eclipse.ide.core.resources.buildpath.ITsconfigBuildPath;
import ts.eclipse.ide.core.resources.buildpath.ITypeScriptBuildPath;
import ts.eclipse.ide.core.resources.jsconfig.IDETsconfigJson;
import ts.eclipse.ide.core.resources.watcher.IFileWatcherListener;
import ts.eclipse.ide.core.resources.watcher.ProjectWatcherListenerAdapter;
import ts.eclipse.ide.core.tslint.IIDETypeScriptLint;
import ts.eclipse.ide.core.utils.TypeScriptResourceUtil;
import ts.eclipse.ide.core.utils.WorkbenchResourceUtil;
import ts.eclipse.ide.internal.core.Trace;
import ts.eclipse.ide.internal.core.compiler.IDETypeScriptCompiler;
import ts.eclipse.ide.internal.core.console.TypeScriptConsoleConnectorManager;
import ts.eclipse.ide.internal.core.resources.jsonconfig.JsonConfigResourcesManager;
import ts.eclipse.ide.internal.core.tslint.IDETypeScriptLint;
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

			// When new project is imported, there are none build path
			// check if the tsconfig.json which is added is a default build path
			// (like tsconfig.json or src/tsconfig.json)
			if (!getTypeScriptBuildPath().hasRootContainers()) {
				ITypeScriptBuildPath tempBuildPath = createBuildPath();
				if (tempBuildPath.hasRootContainers()) {
					buildPath = tempBuildPath;
				}
			}
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
		((IDETypeScriptFile) tsFile).update(document);
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
						// Enable Install @types console (ATA) ?
						if (isEnableTelemetry()) {
							connector.connectToInstallTypesConsole(client);
						} else {
							connector.disconnectToInstallTypesConsole(client);
						}
					}
				} catch (TypeScriptException e) {
					Trace.trace(Trace.SEVERE, "Error while getting TypeScript client", e);
				}
			}
		}
	}

	private boolean isEnableTelemetry() {
		return getProjectSettings().isEnableTelemetry();
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
			// check if the given resource is a file
			IFile file = resource.getType() == IResource.FILE ? (IFile) resource : null;
			if (file == null) {
				return false;
			}
			// Use project preferences, which defines include/exclude path
			ITsconfigBuildPath tsContainer = getTypeScriptBuildPath().findTsconfigBuildPath(resource);
			if (tsContainer == null) {
				return false;
			}
			boolean isJSFile = IDEResourcesManager.getInstance().isJsFile(resource)
					|| IDEResourcesManager.getInstance().isJsxFile(resource);
			if (isJSFile) {
				// Can validate js file?
				return isJsFileIsInScope(file, tsContainer);
			}
			// is ts file is included ?
			return isTsFileIsInScope(file, tsContainer);
		} catch (CoreException e) {
			Trace.trace(Trace.SEVERE, "Error while getting tsconfig.json for canValidate", e);
		}
		return true;
	}

	/**
	 * Returns true if the given js, jsx file can be validated and false
	 * otherwise.
	 * 
	 * @param file
	 * @return true if the given js, jsx file can be validated and false
	 *         otherwise.
	 * @throws CoreException
	 */
	private boolean isJsFileIsInScope(IFile file, ITsconfigBuildPath tsContainer) throws CoreException {
		if (TypeScriptResourceUtil.isEmittedFile(file)) {
			// the js file is an emitted file
			return false;
		}
		// Search if a jsconfig.json exists?
		IFile jsconfigFile = JsonConfigResourcesManager.getInstance().findJsconfigFile(file);
		if (jsconfigFile != null) {
			return true;
		}
		// Search if tsconfig.json exists and defines alloyJs
		IDETsconfigJson tsconfig = tsContainer.getTsconfig();
		if (tsconfig != null && tsconfig.getCompilerOptions() != null
				&& tsconfig.getCompilerOptions().isAllowJs() != null && tsconfig.getCompilerOptions().isAllowJs()) {
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
	 * @param file
	 * @return true if the given ts, tsx file can be validated and false
	 *         otherwise.
	 * @throws CoreException
	 */
	private boolean isTsFileIsInScope(IFile file, ITsconfigBuildPath tsContainer) throws CoreException {
		IDETsconfigJson tsconfig = tsContainer.getTsconfig();
		if (tsconfig != null) {
			return tsconfig.isInScope(file);
		}
		// tsconfig.json was not found (ex : MyProject/node_modules),
		// validation must not be done.
		return false;
	}

	@Override
	public ITypeScriptBuildPath getTypeScriptBuildPath() {
		if (buildPath == null) {
			buildPath = createBuildPath();
		}
		return buildPath;
	}

	private ITypeScriptBuildPath createBuildPath() {
		return ((IDETypeScriptProjectSettings) getProjectSettings()).getTypeScriptBuildPath();
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
		return new IDETypeScriptCompiler(tscFile, nodejsFile, this);
	}

	@Override
	public IIDETypeScriptLint getTslint() throws TypeScriptException {
		return (IIDETypeScriptLint) super.getTslint();
	}

	@Override
	protected ITypeScriptLint createTslint(File tslintFile, File tslintJsonFile, File nodejsFile) {
		return new IDETypeScriptLint(tslintFile, tslintJsonFile, nodejsFile);
	}

	// --------------------------------------- Compile with tsserver

	@Override
	public void compileWithTsserver(List<IFile> updatedTsFiles, List<IFile> removedTsFiles, IProgressMonitor monitor)
			throws TypeScriptException {
		List<IFile> tsFilesToClosed = new ArrayList<>();
		try {
			List<String> tsFilesToCompile = new ArrayList<>();
			// Collect ts files to compile by using tsserver to retrieve
			// dependencies files.
			// It works only if tsconfig.json declares "compileOnSave: true".
			collectTsFilesToCompile(updatedTsFiles, getClient(), tsFilesToCompile, tsFilesToClosed, false);
			// Compile ts files with tsserver.
			compileTsFiles(tsFilesToCompile, getClient());
			if (removedTsFiles.size() > 0) {
				// ts files was removed, how to get referenced files which must
				// be recompiled (with errors)?
			}
		} catch (TypeScriptException e) {
			throw e;
		} catch (Exception e) {
			throw new TypeScriptException(e);
		} finally {
			for (IFile tsFile : tsFilesToClosed) {
				closeFile(tsFile);
			}
		}
	}

	/**
	 * Collect ts files to compile from the given ts files list.
	 * 
	 * @param tsFiles
	 * @param client
	 * @param tsFilesToCompile
	 * @param exclude
	 * @throws Exception
	 */
	private void collectTsFilesToCompile(List<IFile> tsFiles, ITypeScriptServiceClient client,
			List<String> tsFilesToCompile, List<IFile> tsFilesToClosed, boolean exclude) throws Exception {
		for (IFile tsFile : tsFiles) {
			String filename = WorkbenchResourceUtil.getFileName(tsFile);
			if (!tsFilesToCompile.contains(filename)) {
				// tsserver needs that file must be opened, force the "open"
				// if file is not opened.
				// see
				// https://github.com/angelozerr/typescript.java/issues/142
				if (getOpenedFile(tsFile) == null) {
					openFile(tsFile, null);
					tsFilesToClosed.add(tsFile);
				}
				collectTsFilesToCompile(filename, client, tsFilesToCompile, exclude);
			}
		}
	}

	/**
	 * Collect ts files to compile from the given ts file name.
	 * 
	 * @param filename
	 * @param client
	 * @param tsFilesToCompile
	 * @param exclude
	 * @throws Exception
	 */
	private void collectTsFilesToCompile(String filename, ITypeScriptServiceClient client,
			List<String> tsFilesToCompile, boolean exclude) throws Exception {
		// call tsserver compileOnSaveAffectedFileList to retrieve file
		// dependencies of the given filename
		List<CompileOnSaveAffectedFileListSingleProject> affectedProjects = client
				.compileOnSaveAffectedFileList(filename).get(5000, TimeUnit.MILLISECONDS);
		for (CompileOnSaveAffectedFileListSingleProject affectedProject : affectedProjects) {
			List<String> affectedTsFilenames = affectedProject.getFileNames();
			for (String affectedFilename : affectedTsFilenames) {
				if (!tsFilesToCompile.contains(affectedFilename) && !(exclude && filename.equals(affectedFilename))) {
					tsFilesToCompile.add(affectedFilename);
				}
			}
		}
	}

	/**
	 * Compile ts files list with tsserver.
	 * 
	 * @param tsFilesToCompile
	 * @param client
	 * @throws Exception
	 */
	private void compileTsFiles(List<String> tsFilesToCompile, ITypeScriptServiceClient client) throws Exception {
		for (String filename : tsFilesToCompile) {
			compileTsFile(filename, client);
		}
	}

	/**
	 * Compile ts file with tsserver.
	 * 
	 * @param filename
	 * @param client
	 * @throws Exception
	 */
	private void compileTsFile(String filename, ITypeScriptServiceClient client) throws Exception {
		// Compile the given ts filename with tsserver
		Boolean result = client.compileOnSaveEmitFile(filename, true).get(5000, TimeUnit.MILLISECONDS);

		IFile tsFile = WorkbenchResourceUtil.findFileFromWorkspace(filename);
		if (tsFile != null) {
			// Delete TypeScript error marker
			TypeScriptResourceUtil.deleteTscMarker(tsFile);
			// Add TypeScript error marker if there error errors.
			DiagnosticEventBody event = client.syntacticDiagnosticsSync(filename, false).get(5000,
					TimeUnit.MILLISECONDS);
			addMarker(tsFile, event);
			event = client.semanticDiagnosticsSync(filename, false).get(5000, TimeUnit.MILLISECONDS);
			addMarker(tsFile, event);
		}
	}

	public void addMarker(IFile tsFile, DiagnosticEventBody event) throws CoreException {
		List<Diagnostic> diagnostics = event.getDiagnostics();
		for (Diagnostic diagnostic : diagnostics) {
			TypeScriptResourceUtil.addTscMarker(tsFile, diagnostic.getText(), IMarker.SEVERITY_ERROR,
					diagnostic.getStart().getLine());
		}
	}
}
