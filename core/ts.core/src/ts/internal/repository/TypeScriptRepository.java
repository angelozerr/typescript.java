package ts.internal.repository;

import java.io.File;
import java.io.FileInputStream;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;

import ts.repository.ITypeScriptRepository;
import ts.repository.TypeScriptRepositoryException;
import ts.repository.TypeScriptRepositoryManager;
import ts.utils.FileUtils;
import ts.utils.IOUtils;

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
	private File tslintFile;
	private String tslintName;

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
		// tsserver file
		this.tsserverFile = TypeScriptRepositoryManager.getTsserverFile(typesScriptDir);
		if (!tsserverFile.exists()) {
			this.typesScriptDir = new File(baseDir, "node_modules/typescript");
			this.tsserverFile = TypeScriptRepositoryManager.getTsserverFile(typesScriptDir);
		}
		if (!tsserverFile.exists()) {
			throw new TypeScriptRepositoryException(FileUtils.getPath(typesScriptDir)
					+ " is not a valid TypeScript repository. Check the directory contains node_modules/typescript/bin/tsserver or bin/tsserver.");
		}
		// tsc file
		this.tscFile = TypeScriptRepositoryManager.getTscFile(typesScriptDir);
		this.setName(generateName("TypeScript", typesScriptDir));
		// tslint file
		File tslintBaseDir = new File(baseDir, "node_modules/tslint");
		if (tslintBaseDir.exists()) {
			this.tslintFile = TypeScriptRepositoryManager.getTslintFile(tslintBaseDir);
			this.tslintName = generateName("tslint", tslintBaseDir);
		}
	}

	private String generateName(String prefix, File baseDir) {
		StringBuilder name = new StringBuilder(prefix);
		name.append(" (");
		String version = getPackageJsonVersion(baseDir);
		if (version != null) {
			name.append(version);
		}
		name.append(")");
		return name.toString();
	}

	private String getPackageJsonVersion(File baseDir) {
		File packageJsonFile = new File(baseDir, "package.json");
		try {
			JsonObject json = Json.parse(IOUtils.toString(new FileInputStream(packageJsonFile))).asObject();
			return json.getString("version", null);
		} catch (Exception e) {
			return null;
		}
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

	@Override
	public File getTslintFile() {
		return tslintFile;
	}

	@Override
	public String getTslintName() {
		return tslintName;
	}
}
