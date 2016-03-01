package ts.internal.repository;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;

import ts.repository.ITypeScriptRepository;
import ts.repository.TypeScriptRepositoryException;
import ts.repository.TypeScriptRepositoryManager;
import ts.utils.FileUtils;
import ts.utils.IOUtils;
import ts.utils.JsonHelper;

/**
 *
 *
 */
public class TypeScriptRepository implements ITypeScriptRepository {

	private final TypeScriptRepositoryManager manager;
	private File baseDir;
	private String name;
	private File typesScriptDir;
	private File tscFile;
	private File tsserverFile;

	public TypeScriptRepository(File baseDir) throws TypeScriptRepositoryException {
		this(baseDir, null);
	}

	public TypeScriptRepository(File baseDir, TypeScriptRepositoryManager manager)
			throws TypeScriptRepositoryException {
		this.manager = manager;
		this.baseDir = baseDir;
		updateBaseDir(baseDir);
	}

	private void updateBaseDir(File baseDir) throws TypeScriptRepositoryException {
		this.typesScriptDir = baseDir;
		this.tsserverFile = getTsserverFile(typesScriptDir);
		if (!tsserverFile.exists()) {
			this.typesScriptDir = new File(baseDir, "node_modules/typescript");
			this.tsserverFile = getTsserverFile(typesScriptDir);
		}
		if (!tsserverFile.exists()) {
			throw new TypeScriptRepositoryException(FileUtils.getPath(typesScriptDir)
					+ " is not a valid TypeScript repository. Check the directory contains node_modules/typescript/bin/tsserver or bin/tsserver.");
		}
		this.tscFile = getTscFile(typesScriptDir);
		this.setName(generateName());
	}

	private String generateName() {
		StringBuilder name = new StringBuilder("TypeScript (");
		File packageJsonFile = new File(typesScriptDir, "package.json");
		try {
			JsonObject json = Json.parse(IOUtils.toString(new FileInputStream(packageJsonFile))).asObject();
			String version = json.getString("version", null);
			if (version != null) {
				name.append(version);
			}
		} catch (Exception e) {
		}
		name.append(")");
		return name.toString();
	}

	private File getTsserverFile(File typesScriptDir) {
		return new File(typesScriptDir, "bin/tsserver");
	}

	private File getTscFile(File typesScriptDir) {
		return new File(typesScriptDir, "bin/tsc");
	}

	@Override
	public String getName() {
		return name;
	}

	public void setName(String name) throws TypeScriptRepositoryException {
		ITypeScriptRepository repository = manager != null ? manager.getRepository(name) : null;
		if (repository == null || repository.equals(this)) {
			this.name = name;
		} else {
			throw new TypeScriptRepositoryException("It already exists a TypeScript repository with the name " + name);
		}
	}

	@Override
	public File getBaseDir() {
		return baseDir;
	}

	@Override
	public void setBaseDir(File baseDir) {
		this.baseDir = baseDir;
	}

	@Override
	public File getTscFile() {
		return tscFile;
	}

	@Override
	public File getTsserverFile() {
		return tsserverFile;
	}
}
