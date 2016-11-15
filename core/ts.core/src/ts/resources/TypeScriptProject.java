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

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ts.TypeScriptException;
import ts.client.CommandNames;
import ts.client.ITypeScriptClientListener;
import ts.client.ITypeScriptServiceClient;
import ts.client.Location;
import ts.client.TypeScriptServiceClient;
import ts.client.codefixes.ITypeScriptGetSupportedCodeFixesCollector;
import ts.client.diagnostics.ITypeScriptDiagnosticsCollector;
import ts.client.quickinfo.ITypeScriptQuickInfoCollector;
import ts.client.signaturehelp.ITypeScriptSignatureHelpCollector;
import ts.cmd.tsc.CompilerOptionCapability;
import ts.cmd.tsc.ITypeScriptCompiler;
import ts.cmd.tsc.TypeScriptCompiler;
import ts.cmd.tslint.ITypeScriptLint;
import ts.cmd.tslint.TypeScriptLint;

/**
 * TypeScript project implementation.
 *
 */
public class TypeScriptProject implements ITypeScriptProject {

	private final File projectDir;
	private ITypeScriptProjectSettings projectSettings;

	// TypeScript service client
	private ITypeScriptServiceClient client;
	private final Map<String, ITypeScriptFile> openedFiles;

	// TypeScript compiler
	private ITypeScriptCompiler compiler;

	private final Map<String, Object> data;
	private final List<ITypeScriptClientListener> listeners;
	protected final Object serverLock = new Object();
	private ITypeScriptLint tslint;

	private final Map<CommandNames, Boolean> serverCapabilities;
	private Map<CompilerOptionCapability, Boolean> compilerCapabilities;

	private List<String> supportedCodeFixes;

	public TypeScriptProject(File projectDir, ITypeScriptProjectSettings projectSettings) {
		this.projectDir = projectDir;
		this.projectSettings = projectSettings;
		this.openedFiles = new HashMap<String, ITypeScriptFile>();
		this.data = new HashMap<String, Object>();
		this.listeners = new ArrayList<ITypeScriptClientListener>();
		this.serverCapabilities = new HashMap<CommandNames, Boolean>();
		this.compilerCapabilities = new HashMap<CompilerOptionCapability, Boolean>();
	}

	protected void setProjectSettings(ITypeScriptProjectSettings projectSettings) {
		this.projectSettings = projectSettings;
	}

	/**
	 * Returns the project base directory.
	 * 
	 * @return the project base directory.
	 */
	public File getProjectDir() {
		return projectDir;
	}

	void openFile(ITypeScriptFile tsFile) throws TypeScriptException {
		String name = tsFile.getName();
		String contents = tsFile.getContents();
		getClient().openFile(name, contents);
		this.openedFiles.put(name, tsFile);
	}

	void closeFile(ITypeScriptFile tsFile) throws TypeScriptException {
		String name = tsFile.getName();
		getClient().closeFile(name);
		((AbstractTypeScriptFile) tsFile).setOpened(false);
		this.openedFiles.remove(name);
	}

	@Override
	public void signatureHelp(ITypeScriptFile file, int position, ITypeScriptSignatureHelpCollector collector)
			throws TypeScriptException {
		ITypeScriptServiceClient client = getClient();
		file.synch();
		Location location = file.getLocation(position);
		int line = location.getLine();
		int offset = location.getOffset();
		client.signatureHelp(file.getName(), line, offset, collector);
	}

	@Override
	public void quickInfo(ITypeScriptFile file, int position, ITypeScriptQuickInfoCollector collector)
			throws TypeScriptException {
		ITypeScriptServiceClient client = getClient();
		file.synch();
		Location location = file.getLocation(position);
		int line = location.getLine();
		int offset = location.getOffset();
		client.quickInfo(file.getName(), line, offset, collector);
	}

	@Override
	public void changeFile(ITypeScriptFile file, int start, int end, String newText) throws TypeScriptException {
		Location loc = file.getLocation(start);
		int line = loc.getLine();
		int offset = loc.getOffset();
		Location endLoc = file.getLocation(end);
		int endLine = endLoc.getLine();
		int endOffset = endLoc.getOffset();
		getClient().changeFile(file.getName(), line, offset, endLine, endOffset, newText);
	}

	@Override
	public void geterr(ITypeScriptFile file, int delay, ITypeScriptDiagnosticsCollector collector)
			throws TypeScriptException {
		file.synch();
		getClient().geterr(new String[] { file.getName() }, delay, collector);
	}

	@Override
	public void semanticDiagnosticsSync(ITypeScriptFile file, Boolean includeLinePosition,
			ITypeScriptDiagnosticsCollector collector) throws TypeScriptException {
		file.synch();
		getClient().semanticDiagnosticsSync(file.getName(), includeLinePosition, collector);
	}

	@Override
	public void syntacticDiagnosticsSync(ITypeScriptFile file, Boolean includeLinePosition,
			ITypeScriptDiagnosticsCollector collector) throws TypeScriptException {
		file.synch();
		getClient().syntacticDiagnosticsSync(file.getName(), includeLinePosition, collector);
	}

	@Override
	public void diagnostics(ITypeScriptFile file, ITypeScriptDiagnosticsCollector collector)
			throws TypeScriptException {
		file.synch();
		if (canSupport(CommandNames.SemanticDiagnosticsSync)) {
			// TypeScript >=2.0.3, uses syntactic/semantic command names which
			// seems having better performance.
			getClient().syntacticDiagnosticsSync(file.getName(), true, collector);
			getClient().semanticDiagnosticsSync(file.getName(), true, collector);
		} else {
			getClient().geterr(new String[] { file.getName() }, 0, collector);
		}
	}

	@Override
	public List<String> getSupportedCodeFixes() throws TypeScriptException {
		if (supportedCodeFixes != null) {
			return supportedCodeFixes;
		}
		if (canSupport(CommandNames.GetSupportedCodeFixes)) {
			getClient().getSupportedCodeFixes(new ITypeScriptGetSupportedCodeFixesCollector() {

				@Override
				public void setSupportedCodeFixes(List<String> errorCodes) {
					supportedCodeFixes = errorCodes;
				}
			});
		} else {
			supportedCodeFixes = new ArrayList<String>();
		}
		return supportedCodeFixes;
	}
	
	@Override
	public boolean canFix(String errorCode) {
		try {
			return getSupportedCodeFixes().contains(errorCode);
		} catch (Throwable e) {
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public final ITypeScriptServiceClient getClient() throws TypeScriptException {
		synchronized (serverLock) {
			if (isServerDisposed()) {
				try {
					this.client = createServiceClient(getProjectDir());
					copyListeners();
					onCreateClient(client);
				} catch (Exception e) {
					if (e instanceof TypeScriptException) {
						throw (TypeScriptException) e;
					}
					throw new TypeScriptException(e);
				}

			}
			return client;
		}
	}

	protected void onCreateClient(ITypeScriptServiceClient client) {

	}

	@Override
	public synchronized ITypeScriptFile getOpenedFile(String fileName) {
		return openedFiles.get(fileName);
	}

	@Override
	public void dispose() throws TypeScriptException {
		disposeServer();
		getProjectSettings().dispose();
	}

	/**
	 * Create service client which consumes tsserver.
	 * 
	 * @param projectDir
	 * @return
	 * @throws TypeScriptException
	 */
	protected ITypeScriptServiceClient createServiceClient(File projectDir) throws TypeScriptException {
		File nodeFile = getProjectSettings().getNodejsInstallPath();
		File tsserverFile = getProjectSettings().getTsserverFile();
		return new TypeScriptServiceClient(getProjectDir(), tsserverFile, nodeFile);
	}

	/**
	 * Create compiler which consumes tsc.
	 * 
	 * @return
	 * @throws TypeScriptException
	 */
	protected ITypeScriptCompiler createCompiler() throws TypeScriptException {
		File nodeFile = getProjectSettings().getNodejsInstallPath();
		File tscFile = getProjectSettings().getTscFile();
		return createCompiler(tscFile, nodeFile);
	}

	protected ITypeScriptCompiler createCompiler(File tscFile, File nodejsFile) {
		return new TypeScriptCompiler(tscFile, nodejsFile);
	}

	// ----------------------- TypeScript server listeners.

	@Override
	public void addServerListener(ITypeScriptClientListener listener) {
		synchronized (listeners) {
			if (!listeners.contains(listener)) {
				listeners.add(listener);
			}
		}
		copyListeners();
	}

	@Override
	public void removeServerListener(ITypeScriptClientListener listener) {
		synchronized (listeners) {
			listeners.remove(listener);
		}
		synchronized (serverLock) {
			if (hasClient()) {
				this.client.removeClientListener(listener);
			}
		}
	}

	protected boolean hasClient() {
		return client != null;
	}

	private void copyListeners() {
		synchronized (serverLock) {
			if (hasClient()) {
				for (ITypeScriptClientListener listener : listeners) {
					client.addClientListener(listener);
				}
			}
		}
	}

	@Override
	public void disposeServer() {
		synchronized (serverLock) {
			if (!isServerDisposed()) {
				if (hasClient()) {
					// close opened files
					List<ITypeScriptFile> files = new ArrayList<ITypeScriptFile>(openedFiles.values());
					for (ITypeScriptFile openedFile : files) {
						try {
							openedFile.close();
						} catch (TypeScriptException e) {
							e.printStackTrace();
						}
					}
					client.dispose();
					client = null;
				}
			}
		}
		serverCapabilities.clear();
		supportedCodeFixes = null;
	}

	@Override
	public void disposeCompiler() {
		if (compiler != null) {
			compiler.dispose();
			compiler = null;
			compilerCapabilities.clear();
		}
	}

	@SuppressWarnings("unchecked")
	public <T> T getData(String key) {
		synchronized (data) {
			return (T) data.get(key);
		}
	}

	public void setData(String key, Object value) {
		synchronized (data) {
			data.put(key, value);
		}
	}

	@Override
	public boolean isServerDisposed() {
		synchronized (serverLock) {
			return client == null || client.isDisposed();
		}
	}

	@Override
	public ITypeScriptCompiler getCompiler() throws TypeScriptException {
		if (compiler == null) {
			compiler = createCompiler();
		}
		return compiler;
	}

	@Override
	public ITypeScriptLint getTslint() throws TypeScriptException {
		if (tslint == null) {
			tslint = createTslint();
		}
		return tslint;
	}

	@Override
	public void disposeTslint() {
		if (tslint != null) {
			// tslint.dispose();
			tslint = null;
		}
	}

	protected ITypeScriptLint createTslint() throws TypeScriptException {
		File nodeFile = getProjectSettings().getNodejsInstallPath();
		File tslintFile = getProjectSettings().getTslintFile();
		File tslintJsonFile = getProjectSettings().getCustomTslintJsonFile();
		return createTslint(tslintFile, tslintJsonFile, nodeFile);
	}

	protected ITypeScriptLint createTslint(File tslintFile, File tslintJsonFile, File nodejsFile) {
		return new TypeScriptLint(tslintFile, tslintJsonFile, nodejsFile);
	}

	@Override
	public ITypeScriptProjectSettings getProjectSettings() {
		return projectSettings;
	}

	@Override
	public boolean canSupport(CommandNames command) {
		Boolean support = serverCapabilities.get(command);
		if (support == null) {
			support = command.canSupport(getProjectSettings().getTypeScriptVersion());
			serverCapabilities.put(command, support);
		}
		return support;
	}

	@Override
	public boolean canSupport(CompilerOptionCapability option) {
		Boolean support = compilerCapabilities.get(option);
		if (support == null) {
			support = option.canSupport(getProjectSettings().getTypeScriptVersion());
			compilerCapabilities.put(option, support);
		}
		return support;
	}
}
