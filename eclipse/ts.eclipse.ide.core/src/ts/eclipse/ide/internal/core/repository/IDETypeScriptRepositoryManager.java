package ts.eclipse.ide.internal.core.repository;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;

import ts.eclipse.ide.core.repository.IIDETypeScriptRepositoryManager;
import ts.repository.TypeScriptRepositoryManager;

public class IDETypeScriptRepositoryManager extends TypeScriptRepositoryManager
		implements IIDETypeScriptRepositoryManager {

	public static final IIDETypeScriptRepositoryManager INSTANCE = new IDETypeScriptRepositoryManager();

	private static final String PROJECT_LOC_TOKEN = "${project_loc:";
	private static final String WORKSPACE_LOC_TOKEN = "${workspace_loc:";
	private static final String END_TOKEN = "}";

	@Override
	public String generateFileName(IResource resource, IProject project) {
		if (resource.getProject().equals(project)) {
			return new StringBuilder(PROJECT_LOC_TOKEN).append(resource.getProjectRelativePath().toString())
					.append(END_TOKEN).toString();
		}
		return new StringBuilder(WORKSPACE_LOC_TOKEN).append(resource.getFullPath().toString()).append(END_TOKEN)
				.toString();
	}

	@Override
	public IPath getPath(String path, IProject project) {
		if (path.startsWith(PROJECT_LOC_TOKEN)) {
			// ${project_loc:node_modules/typescript
			String projectPath = path.substring(PROJECT_LOC_TOKEN.length(),
					path.endsWith(END_TOKEN) ? path.length() - 1 : path.length());
			return project.getLocation().append(projectPath);
		} else if (path.startsWith(WORKSPACE_LOC_TOKEN)) {
			String wsPath = path.substring(WORKSPACE_LOC_TOKEN.length(),
					path.endsWith(END_TOKEN) ? path.length() - 1 : path.length());
			return ResourcesPlugin.getWorkspace().getRoot().getLocation().append(wsPath);
		}
		return null;
	}

	@Override
	public IResource getResource(String path, IProject project) {
		IPath location = getPath(path, project);
		if (location == null) {
			return null;
		}
		IContainer container = ResourcesPlugin.getWorkspace().getRoot().getContainerForLocation(location);
		if (container.exists()) {
			return container;
		}
		IFile file = ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(location);
		return file.exists() ? file : null;
	}
}
