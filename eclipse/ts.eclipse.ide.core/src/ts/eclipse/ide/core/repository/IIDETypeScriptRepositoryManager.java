package ts.eclipse.ide.core.repository;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;

import ts.repository.ITypeScriptRepositoryManager;

/**
 * TypeScript repository manager for use in the Eclipse IDE.
 * 
 * In addition to the repositories registered manually, this manager will also
 * pick up any repository contributed to the {@code typeScriptRepositories}
 * extension point.
 */
public interface IIDETypeScriptRepositoryManager extends ITypeScriptRepositoryManager {

	String generateFileName(IResource resource, IProject project);

	IPath getPath(String path, IProject project);

	IResource getResource(String path, IProject project);
}
