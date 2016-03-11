package ts.eclipse.ide.core.resources.watcher;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;

/**
 * Resources watcher API used to observe changed of :
 * 
 * <ul>
 * <li>{@link IProject} when project is opened, closed, deleted. See
 * {@link IProjectWatcherListener}.</li>
 * <li>{@link IFile} when file is deleted, created, changed. See
 * {@link IFileWatcherListener}.</li>
 * </ul>
 *
 */
public interface IResourcesWatcher {

	void addProjectWatcherListener(IProject project, IProjectWatcherListener listener);

	void removeProjectWatcherListener(IProject project, IProjectWatcherListener listener);

	void addFileWatcherListener(IProject project, String fileName, IFileWatcherListener listener);

	void removeFileWatcherListener(IProject project, String fileName, IFileWatcherListener listener);
}
