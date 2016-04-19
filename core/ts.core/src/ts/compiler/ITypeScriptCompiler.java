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
package ts.compiler;

import java.io.File;

import ts.TypeScriptException;
import ts.nodejs.INodejsProcessListener;

/**
 * TypeScript compiler which uses 'tsc'
 *
 */
public interface ITypeScriptCompiler {

	/**
	 * Execute 'tsc' command from the given directory.
	 * 
	 * @param baseDir
	 *            teh directory where 'tsc' must be executed.
	 * @throws TypeScriptException
	 */
	void compile(File baseDir, INodejsProcessListener listener) throws TypeScriptException;

	/**
	 * Dispose the compiler.
	 */
	void dispose();
}
