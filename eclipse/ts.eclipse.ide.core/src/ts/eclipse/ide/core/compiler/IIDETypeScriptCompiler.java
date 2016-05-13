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

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;

import ts.TypeScriptException;
import ts.compiler.ITypeScriptCompiler;
import ts.eclipse.ide.core.resources.jsconfig.IDETsconfigJson;

/**
 * IDE TypeScript compiler.
 *
 */
public interface IIDETypeScriptCompiler extends ITypeScriptCompiler {

	/**
	 * Try to compile the given ts files by using tsconfig.json compiler
	 * options.
	 * 
	 * @param tsconfig
	 *            tsconfig.json file.
	 * @param tsFiles
	 * @throws TypeScriptException
	 * @throws CoreException
	 */
	public void compile(IDETsconfigJson tsconfig, List<IFile> tsFiles) throws TypeScriptException, CoreException;
}
