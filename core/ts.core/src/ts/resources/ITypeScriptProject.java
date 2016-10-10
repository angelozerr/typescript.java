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
package ts.resources;

import ts.TypeScriptException;
import ts.client.CommandNames;
import ts.client.ITypeScriptClientListener;
import ts.client.ITypeScriptServiceClient;
import ts.client.diagnostics.ITypeScriptDiagnosticsCollector;
import ts.client.quickinfo.ITypeScriptQuickInfoCollector;
import ts.client.signaturehelp.ITypeScriptSignatureHelpCollector;
import ts.cmd.tsc.ITypeScriptCompiler;
import ts.cmd.tslint.ITypeScriptLint;

/**
 * TypeScript project API.
 *
 */
public interface ITypeScriptProject {

	/**
	 * Returns associated tsclient if any. This call may result in creating one
	 * if it hasn't been created already.
	 * 
	 * @return
	 * @throws TypeScriptException
	 */
	ITypeScriptServiceClient getClient() throws TypeScriptException;

	/**
	 * Returns the tsc compiler.
	 * 
	 * @return
	 */
	ITypeScriptCompiler getCompiler() throws TypeScriptException;

	void signatureHelp(ITypeScriptFile file, int position, ITypeScriptSignatureHelpCollector collector)
			throws TypeScriptException;

	void quickInfo(ITypeScriptFile file, int position, ITypeScriptQuickInfoCollector collector)
			throws TypeScriptException;

	void changeFile(ITypeScriptFile tsFile, int start, int end, String newText) throws TypeScriptException;

	void geterr(ITypeScriptFile tsFile, int delay, ITypeScriptDiagnosticsCollector collector)
			throws TypeScriptException;

	void semanticDiagnosticsSync(ITypeScriptFile tsFile, Boolean includeLinePosition,
			ITypeScriptDiagnosticsCollector collector) throws TypeScriptException;

	void syntacticDiagnosticsSync(ITypeScriptFile tsFile, Boolean includeLinePosition,
			ITypeScriptDiagnosticsCollector collector) throws TypeScriptException;

	void diagnostics(ITypeScriptFile tsFile, ITypeScriptDiagnosticsCollector collector) throws TypeScriptException;

	ITypeScriptFile getOpenedFile(String fileName);

	void dispose() throws TypeScriptException;

	<T> T getData(String key);

	void setData(String key, Object value);

	// -------------- TypeScript server.

	void addServerListener(ITypeScriptClientListener listener);

	void removeServerListener(ITypeScriptClientListener listener);

	void disposeServer();

	void disposeCompiler();

	boolean isServerDisposed();

	/**
	 * Returns the tslint linter.
	 * 
	 * @return
	 * @throws TypeScriptException
	 */
	ITypeScriptLint getTslint() throws TypeScriptException;

	ITypeScriptProjectSettings getProjectSettings();

	void disposeTslint();

	/**
	 * Returns true if the given tsserver command can be supported by the
	 * TypeScript version configured for the project and false otherwise.
	 * 
	 * @param command
	 * @return true if the given tsserver command can be supported by the
	 *         TypeScript version configured for the project and false
	 *         otherwise.
	 */
	boolean canSupport(CommandNames command);
}
