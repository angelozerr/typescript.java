/**
 *  Copyright (c) 2015-2016 Angelo ZERR.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 *  Lorenzo Dalla Vecchia <lorenzo.dallavecchia@webratio.com> - protected API for setting default
 */
package ts.repository;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Map;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;

import ts.internal.repository.TypeScriptRepository;
import ts.utils.IOUtils;

public class TypeScriptRepositoryManager implements ITypeScriptRepositoryManager {

	private final Map<String, ITypeScriptRepository> repositories;

	private ITypeScriptRepository defaultRepository;

	public TypeScriptRepositoryManager() {
		this.repositories = new HashMap<String, ITypeScriptRepository>();
	}

	@Override
	public ITypeScriptRepository createDefaultRepository(File baseDir) throws TypeScriptRepositoryException {
		return this.defaultRepository = createRepository(baseDir);
	}

	protected final void setDefaultRepository(ITypeScriptRepository repository) {
		this.defaultRepository = repository;
	}

	@Override
	public ITypeScriptRepository createRepository(File baseDir) throws TypeScriptRepositoryException {
		synchronized (repositories) {
			ITypeScriptRepository repository = new TypeScriptRepository(baseDir, this);
			repositories.put(repository.getName(), repository);
			return repository;
		}
	}

	@Override
	public ITypeScriptRepository removeRepository(String name) {
		synchronized (repositories) {
			return repositories.remove(name);
		}
	}

	@Override
	public ITypeScriptRepository getDefaultRepository() {
		return defaultRepository;
	}

	@Override
	public ITypeScriptRepository getRepository(String name) {
		return repositories.get(name);
	}

	@Override
	public ITypeScriptRepository[] getRepositories() {
		return repositories.values().toArray(new ITypeScriptRepository[repositories.size()]);
	}

	public static File getTsserverFile(File typesScriptDir) {
		if (typesScriptDir.getName().equals("tsserver")) {
			return typesScriptDir;
		}
		return new File(typesScriptDir, "bin/tsserver");
	}

	public static File getTscFile(File typesScriptDir) {
		if (typesScriptDir.getName().equals("tsc")) {
			return typesScriptDir;
		}
		return new File(typesScriptDir, "bin/tsc");
	}

	public static File getTslintFile(File tslintScriptDir) {
		return new File(tslintScriptDir, "bin/tslint");
	}
	
	public static String getPackageJsonVersion(File baseDir) {
		File packageJsonFile = new File(baseDir, "package.json");
		try {
			JsonObject json = Json.parse(IOUtils.toString(new FileInputStream(packageJsonFile))).asObject();
			return json.getString("version", null);
		} catch (Exception e) {
			return null;
		}
	}

	public static File getTsserverPluginsFile(File typesScriptDir) {
		if (typesScriptDir.getName().equals("tsserver-plugins")) {
			return typesScriptDir;
		}
		return new File(typesScriptDir, "bin/tsserver-plugins");
	}
}
