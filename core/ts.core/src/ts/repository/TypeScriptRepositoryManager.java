package ts.repository;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import ts.internal.repository.TypeScriptRepository;

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

}
