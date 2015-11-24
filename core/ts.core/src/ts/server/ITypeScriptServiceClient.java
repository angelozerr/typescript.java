/**
 *  Copyright (c) 2013-2015 Angelo ZERR.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package ts.server;

import ts.TSException;
import ts.server.completions.ITypeScriptCompletionCollector;
import ts.server.definition.ITypeScriptDefinitionCollector;

/**
 * TypeScript client API which communicates with tsserver.
 * 
 * @see https://github.com/Microsoft/TypeScript/blob/master/src/server/client.ts
 *
 */
public interface ITypeScriptServiceClient {

	void openFile(String fileName) throws TSException;

	void closeFile(String fileName) throws TSException;

	void updateFile(String fileName, String newText) throws TSException;

	void completions(String fileName, int line, int offset, String prefix, ITypeScriptCompletionCollector collector)
			throws TSException;

	void changeFile(String fileName, int line, int offset, int endLine, int endOffset, String newText)
			throws TSException;

	void definition(String fileName, int line, int offset, ITypeScriptDefinitionCollector collector) throws TSException;

	void join() throws InterruptedException;

	void addServerListener(ITypeScriptServerListener listener);

	void removeServerListener(ITypeScriptServerListener listener);

	void addInterceptor(IInterceptor interceptor);
	
	void removeInterceptor(IInterceptor interceptor);
	
	void dispose();
	
	boolean isDisposed();

}
