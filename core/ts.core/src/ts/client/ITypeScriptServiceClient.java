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
package ts.client;

import ts.TypeScriptException;
import ts.client.completions.ITypeScriptCompletionCollector;
import ts.client.definition.ITypeScriptDefinitionCollector;
import ts.client.geterr.ITypeScriptGeterrCollector;
import ts.client.quickinfo.ITypeScriptQuickInfoCollector;
import ts.client.signaturehelp.ITypeScriptSignatureHelpCollector;

/**
 * TypeScript client API which communicates with tsserver.
 * 
 * @see https://github.com/Microsoft/TypeScript/blob/master/src/server/client.ts
 *
 */
public interface ITypeScriptServiceClient {

	void openFile(String fileName, String contents) throws TypeScriptException;

	void closeFile(String fileName) throws TypeScriptException;

	void updateFile(String fileName, String newText) throws TypeScriptException;

	void completions(String fileName, int line, int offset, String prefix, ITypeScriptCompletionCollector collector)
			throws TypeScriptException;

	void definition(String fileName, int line, int offset, ITypeScriptDefinitionCollector collector) throws TypeScriptException;

	void signatureHelp(String fileName, int line, int offset, ITypeScriptSignatureHelpCollector collector)
			throws TypeScriptException;

	void quickInfo(String fileName, int line, int offset, ITypeScriptQuickInfoCollector collector) throws TypeScriptException;

	void changeFile(String fileName, int line, int offset, int endLine, int endOffset, String newText)
			throws TypeScriptException;

	void geterr(String[] files, int delay, ITypeScriptGeterrCollector collector) throws TypeScriptException;

	void join() throws InterruptedException;

	void addClientListener(ITypeScriptClientListener listener);

	void removeClientListener(ITypeScriptClientListener listener);

	void addInterceptor(IInterceptor interceptor);

	void removeInterceptor(IInterceptor interceptor);

	void dispose();

	boolean isDisposed();

}
