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
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

import ts.TypeScriptException;
import ts.compiler.CompilerOptions;
import ts.compiler.TypeScriptCompiler;
import ts.eclipse.ide.core.compiler.IIDETypeScriptCompiler;
import ts.eclipse.ide.core.resources.jsconfig.IDETsconfigJson;
import ts.eclipse.ide.core.utils.TypeScriptResourceUtil;
import ts.eclipse.ide.internal.core.Trace;

/**
 * Extends {@link TypeScriptCompiler} to use Eclipse {@link IResource}.
 */
public class IDETypeScriptCompiler extends TypeScriptCompiler implements IIDETypeScriptCompiler {

	public IDETypeScriptCompiler(File tscFile, File nodejsFile) {
		super(tscFile, nodejsFile);
	}

	@Override
	public void compile(IContainer container) throws TypeScriptException, CoreException {
		compile(container, null);
	}

	@Override
	public void compile(IContainer container, List<IFile> tsFiles) throws TypeScriptException, CoreException {
		IDETsconfigJson tsconfig = TypeScriptResourceUtil.findTsconfig(container);
		compile(tsconfig, container, tsFiles);
	}

	@Override
	public void compile(IDETsconfigJson tsconfig) throws TypeScriptException, CoreException {
		compile(tsconfig, null);
	}

	@Override
	public void compile(IDETsconfigJson tsconfig, List<IFile> tsFiles) throws TypeScriptException, CoreException {
		IContainer container = tsconfig.getTsconfigFile().getParent();
		compile(tsconfig, container, tsFiles);
	}

	private void compile(IDETsconfigJson tsconfig, IContainer container, List<IFile> tsFiles)
			throws TypeScriptException, CoreException {
		IDETypeScriptCompilerReporter reporter = new IDETypeScriptCompilerReporter(container, tsFiles);
		CompilerOptions options = tsconfig != null && tsconfig.getCompilerOptions() != null
				? new CompilerOptions(tsconfig.getCompilerOptions()) : new CompilerOptions();
		if (tsFiles == null && tsconfig != null && tsconfig.getCompilerOptions() != null) {
			// buildOnSave, copy outFile
			options.setOutFile(tsconfig.getCompilerOptions().getOutFile());

		}
		options.setListFiles(true);
		options.setWatch(false);
		super.compile(container.getLocation().toFile(), options, reporter.getFileNames(), reporter);
		reporter.refreshEmittedFiles();
	}
}
