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
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.runtime.CoreException;

import ts.compiler.TypeScriptCompilerHelper;
import ts.eclipse.ide.core.compiler.IDETypeScriptCompilerMessageHandler;
import ts.nodejs.INodejsProcess;
import ts.nodejs.INodejsProcessListener;

/**
 * IDE TypeScript compiler reporter.
 */
public class IDETypeScriptCompilerReporter extends IDETypeScriptCompilerMessageHandler
		implements INodejsProcessListener {

	private INodejsProcess process;

	public IDETypeScriptCompilerReporter(IContainer container) throws CoreException {
		super(container);
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

}
