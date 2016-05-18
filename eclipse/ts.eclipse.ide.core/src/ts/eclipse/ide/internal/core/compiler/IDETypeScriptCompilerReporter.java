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
package ts.eclipse.ide.internal.core.compiler;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;

import ts.cmd.tsc.TypeScriptCompilerHelper;
import ts.eclipse.ide.core.compiler.IDETypeScriptCompilerMessageHandler;
import ts.eclipse.ide.core.utils.TypeScriptResourceUtil;
import ts.eclipse.ide.core.utils.WorkbenchResourceUtil;
import ts.nodejs.INodejsProcess;
import ts.nodejs.INodejsProcessListener;

/**
 * IDE TypeScript compiler reporter.
 */
public class IDETypeScriptCompilerReporter extends IDETypeScriptCompilerMessageHandler
		implements INodejsProcessListener {

	private INodejsProcess process;
	private final List<String> tsFileNames;

	public IDETypeScriptCompilerReporter(IContainer container, List<IFile> tsFiles) throws CoreException {
		super(container, tsFiles == null);
		if (tsFiles != null) {
			tsFileNames = new ArrayList<String>();
			for (IFile tsFile : tsFiles) {
				// delete marker for the given ts files.
				TypeScriptResourceUtil.deleteTscMarker(tsFile);
				// add to the list file names
				tsFileNames.add(WorkbenchResourceUtil.getRelativePath(tsFile, container).toString());
			}
		} else {
			tsFileNames = null;
		}
	}

	@Override
	public void onCreate(INodejsProcess process, List<String> commands, File projectDir) {

	}

	@Override
	public void onStart(INodejsProcess process) {
		this.process = process;
	}

	@Override
	public void onMessage(INodejsProcess process, String response) {
		TypeScriptCompilerHelper.processMessage(response, this);
	}

	@Override
	public void onStop(INodejsProcess process) {
	}

	@Override
	public void onError(INodejsProcess process, String line) {
	}

	@Override
	public void onCompilationCompleteWatchingForFileChanges() {
		process.kill();
	}

	public List<String> getFileNames() {
		return tsFileNames;
	}

}
