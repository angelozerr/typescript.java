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
package ts.eclipse.ide.internal.core.launch;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.IStreamListener;
import org.eclipse.debug.core.model.IStreamMonitor;

import ts.compiler.TypeScriptCompilerHelper;
import ts.eclipse.ide.core.compiler.IDETypeScriptCompilerMessageHandler;

/**
 * {@link IStreamListener} implementation to track TypeScript Compiler "tsc"
 * messages.
 *
 */
public class TscStreamListener extends IDETypeScriptCompilerMessageHandler implements IStreamListener {

	public TscStreamListener(IContainer container) throws CoreException {
		super(container);
	}

	@Override
	public void streamAppended(String text, IStreamMonitor monitor) {
		TypeScriptCompilerHelper.processMessage(text, this);
	}

}
