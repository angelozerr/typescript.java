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
package ts.resources;

import ts.TypeScriptException;
import ts.server.ITypeScriptServerListener;
import ts.server.ITypeScriptServiceClient;
import ts.server.completions.ITypeScriptCompletionCollector;
import ts.server.definition.ITypeScriptDefinitionCollector;
import ts.server.geterr.ITypeScriptGeterrCollector;
import ts.server.quickinfo.ITypeScriptQuickInfoCollector;
import ts.server.signaturehelp.ITypeScriptSignatureHelpCollector;

public interface ITypeScriptProject {

	/**
	 * Returns associated tsclient if any. This call may result in creating one
	 * if it hasn't been created already.
	 * 
	 * @return
	 * @throws TypeScriptException
	 */
	ITypeScriptServiceClient getClient() throws TypeScriptException;

	void signatureHelp(ITypeScriptFile file, int position, ITypeScriptSignatureHelpCollector collector)
			throws TypeScriptException;

	void quickInfo(ITypeScriptFile file, int position, ITypeScriptQuickInfoCollector collector)
			throws TypeScriptException;
	
	void changeFile(ITypeScriptFile tsFile, int start, int end, String newText) throws TypeScriptException;

	void geterr(ITypeScriptFile tsFile, int delay, ITypeScriptGeterrCollector collector) throws TypeScriptException;
	
	ITypeScriptFile getOpenedFile(String fileName);

	void dispose() throws TypeScriptException;

	<T> T getData(String key);

	void setData(String key, Object value);

	// -------------- TypeScript server.

	void addServerListener(ITypeScriptServerListener listener);

	void removeServerListener(ITypeScriptServerListener listener);

	void disposeServer();

	boolean isServerDisposed();

	SynchStrategy getSynchStrategy();
}
