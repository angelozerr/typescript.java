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
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.osgi.util.NLS;

import ts.TypeScriptException;
import ts.compiler.CompilerOptions;
import ts.compiler.TypeScriptCompiler;
import ts.eclipse.ide.core.compiler.IIDETypeScriptCompiler;
import ts.eclipse.ide.core.resources.jsconfig.IDETsconfigJson;
import ts.eclipse.ide.core.utils.TypeScriptResourceUtil;
import ts.eclipse.ide.internal.core.TypeScriptCoreMessages;

/**
 * Extends {@link TypeScriptCompiler} to use Eclipse {@link IResource}.
 */
public class IDETypeScriptCompiler extends TypeScriptCompiler implements IIDETypeScriptCompiler {

	public IDETypeScriptCompiler(File tscFile, File nodejsFile) {
		super(tscFile, nodejsFile);
	}

	@Override
	public void compile(IDETsconfigJson tsconfig, List<IFile> tsFiles) throws TypeScriptException, CoreException {
		IFile tsconfigFile = tsconfig.getTsconfigFile();
		if (tsconfig.isBuildOnSave() || tsconfig.isCompileOnSave()) {
			// Compile the whole files for the given tsconfig.json
			compile(tsconfigFile, tsconfig.getCompilerOptions(), tsFiles, true);
		} else {
			if (tsconfig.isCompileOnSave()) {
				// compileOnSave is activated, compile the list of ts
				// files.
				compile(tsconfigFile, tsconfig.getCompilerOptions(), tsFiles, false);
			} else {
				// compileOnSave is setted to false in the
				// tsconfig.json,
				// add a warning marker inside each ts files that user
				// whish
				// to compile
				for (IFile tsFile : tsFiles) {
					// delete existing marker
					TypeScriptResourceUtil.deleteTscMarker(tsFile);
					// add warning marker
					TypeScriptResourceUtil.addTscMarker(tsFile,
							NLS.bind(TypeScriptCoreMessages.tsconfig_compileOnSave_disable_error,
									tsconfig.getTsconfigFile().getProjectRelativePath().toString()),
							IMarker.SEVERITY_WARNING, 1);
					// delete emitted files *.js, *.js.map
					TypeScriptResourceUtil.deleteEmittedFiles(tsFile, tsconfig);
				}
			}
		}
	}

	private void compile(IFile tsConfigFile, CompilerOptions tsconfigOptions, List<IFile> tsFiles, boolean buildOnSave)
			throws TypeScriptException, CoreException {
		IContainer container = tsConfigFile.getParent();
		IDETypeScriptCompilerReporter reporter = new IDETypeScriptCompilerReporter(container,
				!buildOnSave ? tsFiles : null);
		CompilerOptions options = tsconfigOptions != null ? new CompilerOptions(tsconfigOptions)
				: new CompilerOptions();
		if (buildOnSave && tsconfigOptions != null) {
			// buildOnSave, copy outFile
			options.setOutFile(tsconfigOptions.getOutFile());
		}
		options.setListFiles(true);
		options.setWatch(false);
		super.compile(container.getLocation().toFile(), options, reporter.getFileNames(), reporter);
		reporter.refreshEmittedFiles();
		// check the given list of ts files are the same than tsc
		// --listFiles
		for (IFile tsFile : tsFiles) {
			if (!reporter.getFilesToRefresh().contains(tsFile)) {
				// delete existing marker
				TypeScriptResourceUtil.deleteTscMarker(tsFile);
				// add warning marker
				TypeScriptResourceUtil.addTscMarker(tsFile,
						NLS.bind(TypeScriptCoreMessages.tsconfig_compilation_context_error,
								tsConfigFile.getProjectRelativePath().toString()),
						IMarker.SEVERITY_WARNING, 1);
			}
		}

	}

}
