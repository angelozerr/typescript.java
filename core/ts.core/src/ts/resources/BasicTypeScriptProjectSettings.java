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

import ts.TypeScriptException;
import ts.client.completions.ICompletionEntryMatcher;
import ts.client.format.FormatOptions;
import ts.cmd.tslint.TslintSettingsStrategy;
import ts.internal.repository.TypeScriptRepository;
import ts.nodejs.NodejsProcess;
import ts.repository.ITypeScriptRepository;
import ts.repository.TypeScriptRepositoryException;

/**
 * Basic project settings.
 *
 */
public class BasicTypeScriptProjectSettings implements ITypeScriptProjectSettings {

	private final File nodejsInstallPath;
	private final SynchStrategy synchStrategy;
	private final ITypeScriptRepository repository;
	private ICompletionEntryMatcher completionEntryMatcher;
	private TslintSettingsStrategy tslintStrategy;

	public BasicTypeScriptProjectSettings(File nodejsInstallPath, File typeScriptDir)
			throws TypeScriptRepositoryException {
		this(nodejsInstallPath, typeScriptDir, SynchStrategy.RELOAD);
	}

	public BasicTypeScriptProjectSettings(File nodejsInstallPath, File typeScriptDir, SynchStrategy synchStrategy)
			throws TypeScriptRepositoryException {
		this.nodejsInstallPath = nodejsInstallPath;
		this.repository = new TypeScriptRepository(typeScriptDir);
		this.synchStrategy = synchStrategy;
	}

	@Override
	public SynchStrategy getSynchStrategy() {
		return synchStrategy;
	}

	@Override
	public File getNodejsInstallPath() {
		return nodejsInstallPath;
	}

	@Override
	public String getNodeVersion() {
		File nodejsFile = getNodejsInstallPath();
		return NodejsProcess.getNodeVersion(nodejsFile);
	}

	@Override
	public File getTscFile() {
		return repository.getTscFile();
	}

	@Override
	public File getTsserverFile() {
		return repository.getTsserverFile();
	}

	@Override
	public File getTslintFile() throws TypeScriptException {
		return repository.getTslintFile();
	}

	@Override
	public ICompletionEntryMatcher getCompletionEntryMatcher() {
		return completionEntryMatcher;
	}

	public void setCompletionEntryMatcher(ICompletionEntryMatcher completionEntryMatcher) {
		this.completionEntryMatcher = completionEntryMatcher;
	}

	@Override
	public File getCustomTslintJsonFile() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TslintSettingsStrategy getTslintStrategy() {
		return tslintStrategy;
	}

	public void setTslintStrategy(TslintSettingsStrategy tslintStrategy) {
		this.tslintStrategy = tslintStrategy;
	}

	@Override
	public boolean isUseCodeSnippetsOnMethodSuggest() {
		return false;
	}

	@Override
	public void dispose() {
		// Do nothing
	}

	@Override
	public FormatOptions getFormatOptions() {
		return null;
	}

	@Override
	public String getTypeScriptVersion() {
		return repository.getTypesScriptVersion();
	}
}
