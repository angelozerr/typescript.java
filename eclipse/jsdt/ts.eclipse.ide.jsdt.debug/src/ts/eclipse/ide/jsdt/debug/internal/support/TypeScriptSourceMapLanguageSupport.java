/**
 *  Copyright (c) 2015-2017 Angelo ZERR.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package ts.eclipse.ide.jsdt.debug.internal.support;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.wst.jsdt.chromium.debug.core.sourcemap.extension.ISourceMapLanguageSupport;

import ts.eclipse.ide.core.resources.jsconfig.IDETsconfigJson;
import ts.eclipse.ide.core.utils.TypeScriptResourceUtil;
import ts.eclipse.ide.core.utils.WorkbenchResourceUtil;

/**
 * JSDT SourceMap Language Support implementation for TypeScript.
 *
 */
public class TypeScriptSourceMapLanguageSupport implements ISourceMapLanguageSupport {

	@Override
	public IPath getJsFile(IPath tsFilePath) {
		// Search js file in the same folder than ts file.
		IPath jsFilePath = tsFilePath.removeFileExtension().addFileExtension("js");
		IFile jsFile = WorkbenchResourceUtil.findFileFromWorkspace(jsFilePath);
		if (jsFile != null) {
			return jsFilePath;
		}
		// Search js file in the well folder by using tsconfig.json
		IFile tsFile = WorkbenchResourceUtil.findFileFromWorkspace(tsFilePath);
		try {
			IDETsconfigJson tsconfig = TypeScriptResourceUtil.findTsconfig(tsFile);
			if (tsconfig != null) {
				IContainer configOutDir = tsconfig.getOutDir();
				if (configOutDir != null && configOutDir.exists()) {
					IPath tsFileNamePath = WorkbenchResourceUtil.getRelativePath(tsFile, configOutDir)
							.removeFileExtension();
					return tsFileNamePath.addFileExtension("js");
				}
			}
		} catch (CoreException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public IPath getSourceMapFile(IPath tsFilePath) {
		// Search js file in the same folder than ts file.
		IPath jsMapFilePath = tsFilePath.removeFileExtension().addFileExtension("js.map");
		IFile jsMapFile = WorkbenchResourceUtil.findFileFromWorkspace(jsMapFilePath);
		if (jsMapFile != null) {
			return jsMapFilePath;
		}
		// Search js file in the well folder by using tsconfig.json
		IFile tsFile = WorkbenchResourceUtil.findFileFromWorkspace(tsFilePath);
		try {
			IDETsconfigJson tsconfig = TypeScriptResourceUtil.findTsconfig(tsFile);
			if (tsconfig != null) {
				IContainer configOutDir = tsconfig.getOutDir();
				if (configOutDir != null && configOutDir.exists()) {
					IPath tsFileNamePath = WorkbenchResourceUtil.getRelativePath(tsFile, configOutDir)
							.removeFileExtension();
					return tsFileNamePath.addFileExtension("js");
				}
			}
		} catch (CoreException e) {
			e.printStackTrace();
		}
		return null;
	}
}
