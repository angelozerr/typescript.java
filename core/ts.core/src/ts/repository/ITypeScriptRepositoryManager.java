package ts.repository;

import java.io.File;

public interface ITypeScriptRepositoryManager {

	ITypeScriptRepository createDefaultRepository(File baseDir) throws TypeScriptRepositoryException;

	ITypeScriptRepository createRepository(File baseDir) throws TypeScriptRepositoryException;

	ITypeScriptRepository removeRepository(String name);

	ITypeScriptRepository getDefaultRepository();

	ITypeScriptRepository getRepository(String name);

	ITypeScriptRepository[] getRepositories();

}
