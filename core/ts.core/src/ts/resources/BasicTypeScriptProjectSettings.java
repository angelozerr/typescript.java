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

import ts.internal.repository.TypeScriptRepository;
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
	public File getTscFile() {
		return repository.getTscFile();
	}

	@Override
	public File getTsserverFile() {
		return repository.getTsserverFile();
	}

}
