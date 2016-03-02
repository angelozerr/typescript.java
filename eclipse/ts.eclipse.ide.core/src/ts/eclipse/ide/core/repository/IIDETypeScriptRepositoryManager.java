package ts.eclipse.ide.core.repository;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;

import ts.repository.ITypeScriptRepositoryManager;

public interface IIDETypeScriptRepositoryManager extends ITypeScriptRepositoryManager {

	String generateFileName(IResource resource, IProject project);

	IPath getPath(String path, IProject project);

	IResource getResource(String path, IProject project);
}
