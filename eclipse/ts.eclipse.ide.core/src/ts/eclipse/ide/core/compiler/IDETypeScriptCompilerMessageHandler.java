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
package ts.eclipse.ide.core.compiler;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

import ts.client.Location;
import ts.cmd.Severity;
import ts.cmd.tsc.ITypeScriptCompilerMessageHandler;
import ts.eclipse.ide.core.TypeScriptCorePlugin;
import ts.eclipse.ide.core.resources.jsconfig.IDETsconfigJson;
import ts.eclipse.ide.core.utils.TypeScriptResourceUtil;
import ts.eclipse.ide.core.utils.WorkbenchResourceUtil;
import ts.eclipse.ide.internal.core.Trace;
import ts.resources.jsonconfig.TsconfigJson;
import ts.utils.FileUtils;

/**
 * Eclipse IDE implementation of {@link ITypeScriptCompilerMessageHandler} used
 * to track "tsc" message to:
 * 
 * <ul>
 * <li>add error marker to the *.ts files which have error.</li>
 * <li>refresh emitted files *.js and *.js.map files</li>
 * </ul>
 */
public class IDETypeScriptCompilerMessageHandler implements ITypeScriptCompilerMessageHandler {

	private final IContainer container;
	private final boolean listEmittedFiles;
	private final IDETsconfigJson tsconfig;
	private final List<IFile> filesToRefresh;
	private final List<IFile> emittedFiles;

	public IDETypeScriptCompilerMessageHandler(IContainer container, boolean listEmittedFiles, boolean deleteMarkers)
			throws CoreException {
		this.container = container;
		this.listEmittedFiles = listEmittedFiles;
		this.tsconfig = TypeScriptResourceUtil.findTsconfig(container);
		this.filesToRefresh = new ArrayList<IFile>();
		this.emittedFiles = new ArrayList<IFile>();
		if (deleteMarkers) {
			TypeScriptResourceUtil.deleteTscMarker(container);
		}
	}

	@Override
	public void addFile(String filePath, boolean emitted) {
		IFile file = getFile(filePath);
		if (file == null) {
			return;
		}
		List<IFile> files = emitted ? emittedFiles : filesToRefresh;
		if (!files.contains(file)) {
			files.add(file);
		}
	}

	private IFile getFile(String filePath) {
		IPath path = new Path(filePath);
		if (container.exists(path)) {
			return container.getFile(path);
		}
		return WorkbenchResourceUtil.findFileFromWorkspace(filePath);
	}

	public List<IFile> getFilesToRefresh() {
		return filesToRefresh;
	}

	@Override
	public void onCompilationCompleteWatchingForFileChanges() {
		try {
			refreshEmittedFiles();
		} catch (CoreException e) {
			TypeScriptCorePlugin.logError(e);
		}
	}

	/**
	 * Refresh emitted files *.js , *.js.map
	 * 
	 * @throws CoreException
	 */
	public void refreshEmittedFiles() throws CoreException {
		// refresh *.js, *.js.map files
		for (IFile emittedFile : emittedFiles) {
			TypeScriptResourceUtil.refreshFile(emittedFile, true);
		}
		for (IFile tsFile : getFilesToRefresh()) {
			try {
				TypeScriptResourceUtil.refreshAndCollectEmittedFiles(tsFile, tsconfig, true, null);
			} catch (CoreException e) {
				Trace.trace(Trace.SEVERE, "Error while tsc compilation when ts file is refreshed", e);
			}
		}
		// refresh outFile if tsconfig.json defines it.
		if (tsconfig != null) {
			// Refresh *.js outFile
			IFile outFile = tsconfig.getOutFile();
			if (outFile != null) {
				TypeScriptResourceUtil.refreshFile(outFile, true);
				// Refresh *.js.map outFile
				IContainer outDir = outFile.getParent();
				IPath mapFileNamePath = WorkbenchResourceUtil.getRelativePath(outFile, outDir)
						.addFileExtension(FileUtils.MAP_EXTENSION);
				TypeScriptResourceUtil.refreshAndCollectEmittedFile(mapFileNamePath, outDir, true, null);
			}
		}
	}

	public IDETsconfigJson getTsconfig() {
		return tsconfig;
	}

	@Override
	public void addError(String filename, Location startLoc, Location endLoc, Severity severity, String code,
			String message) {
		IFile file = getFile(filename);
		if (file != null) {
			try {
				String error = TypeScriptResourceUtil.formatTscError(code, message);
				TypeScriptResourceUtil.addTscMarker(file, error, getSeverity(severity), startLoc.getLine());
			} catch (CoreException e) {

			}
		}
	}

	private int getSeverity(Severity severity) {
		switch (severity) {
		case error:
			return IMarker.SEVERITY_ERROR;
		case info:
			return IMarker.SEVERITY_INFO;
		default:
			return IMarker.SEVERITY_WARNING;
		}
	}

	public boolean isWatch() {
		TsconfigJson tsconfig = getTsconfig();
		return tsconfig != null && tsconfig.getCompilerOptions() != null
				&& tsconfig.getCompilerOptions().isWatch() != null && tsconfig.getCompilerOptions().isWatch();
	}

	public boolean isListEmittedFiles() {
		return listEmittedFiles;
	}
}
