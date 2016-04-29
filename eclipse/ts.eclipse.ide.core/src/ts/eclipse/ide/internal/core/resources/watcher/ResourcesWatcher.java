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
package ts.eclipse.ide.internal.core.resources.watcher;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;

import ts.eclipse.ide.core.resources.watcher.IFileWatcherListener;
import ts.eclipse.ide.core.resources.watcher.IProjectWatcherListener;
import ts.eclipse.ide.core.resources.watcher.IResourcesWatcher;
import ts.eclipse.ide.internal.core.Trace;

/**
 * {@link IResourcesWatcher} implementation.
 *
 */
public class ResourcesWatcher implements IResourcesWatcher, IResourceChangeListener, IResourceDeltaVisitor {

	private static final ResourcesWatcher INSTANCE = new ResourcesWatcher();

	public static ResourcesWatcher getInstance() {
		return INSTANCE;
	}

	private final Map<IProject, List<IProjectWatcherListener>> projectListeners;
	private final Map<IProject, Map<String, List<IFileWatcherListener>>> fileListeners;

	private ResourcesWatcher() {
		this.projectListeners = new HashMap<IProject, List<IProjectWatcherListener>>();
		this.fileListeners = new HashMap<IProject, Map<String, List<IFileWatcherListener>>>();
		ResourcesPlugin.getWorkspace().addResourceChangeListener(this);
	}

	public void dispose() {
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
		synchronized (projectListeners) {
			this.projectListeners.clear();
		}
		synchronized (fileListeners) {
			this.fileListeners.clear();
		}
	}

	@Override
	public void addProjectWatcherListener(IProject project, IProjectWatcherListener listener) {
		synchronized (projectListeners) {
			List<IProjectWatcherListener> listenersForProject = projectListeners.get(project);
			if (listenersForProject == null) {
				listenersForProject = new ArrayList<IProjectWatcherListener>();
				projectListeners.put(project, listenersForProject);
			}
			if (!listenersForProject.contains(listener)) {
				listenersForProject.add(listener);
			}
		}
	}

	@Override
	public void removeProjectWatcherListener(IProject project, IProjectWatcherListener listener) {
		synchronized (projectListeners) {
			List<IProjectWatcherListener> listenersForProject = projectListeners.get(project);
			if (listenersForProject != null) {
				listenersForProject.remove(listener);
			}
		}
	}

	@Override
	public void addFileWatcherListener(IProject project, String fileName, IFileWatcherListener listener) {
		synchronized (fileListeners) {
			Map<String, List<IFileWatcherListener>> listenersForProject = fileListeners.get(project);
			if (listenersForProject == null) {
				listenersForProject = new HashMap<String, List<IFileWatcherListener>>();
				fileListeners.put(project, listenersForProject);
			}
			List<IFileWatcherListener> listenersForProjectAndFile = listenersForProject.get(fileName);
			if (listenersForProjectAndFile == null) {
				listenersForProjectAndFile = new ArrayList<IFileWatcherListener>();
				listenersForProject.put(fileName, listenersForProjectAndFile);
			}
			if (!listenersForProjectAndFile.contains(listener)) {
				listenersForProjectAndFile.add(listener);
			}
		}
	}

	@Override
	public void removeFileWatcherListener(IProject project, String fileName, IFileWatcherListener listener) {
		synchronized (fileListeners) {
			Map<String, List<IFileWatcherListener>> listenersForProject = fileListeners.get(project);
			if (listenersForProject != null) {
				List<IFileWatcherListener> listenersForProjectAndFile = listenersForProject.get(fileName);
				if (listenersForProjectAndFile != null) {
					listenersForProjectAndFile.remove(listener);
				}
			}
		}
	}

	@Override
	public void resourceChanged(IResourceChangeEvent event) {
		try {
			IResource resource = event.getResource();
			switch (event.getType()) {
			case IResourceChangeEvent.PRE_DELETE:
				if (resource != null) {
					switch (resource.getType()) {
					case IResource.PROJECT:
						// called when project is deleted.
						deleteProject((IProject) resource);
						break;
					}
				}
				break;
			case IResourceChangeEvent.PRE_CLOSE:
				if (resource != null) {
					switch (resource.getType()) {
					case IResource.PROJECT:
						// called when project is closed.
						closeProject((IProject) resource);
						break;
					}
				}
				break;
			case IResourceChangeEvent.POST_CHANGE:
				IResourceDelta delta = event.getDelta();
				if (delta != null) {
					delta.accept(this);
				}
				break;
			}
		} catch (Throwable e) {
			Trace.trace(Trace.SEVERE, "Error while TypeScript resource changed", e);
		}
	}

	private void closeProject(IProject current) {
		processProjectListeners(current, true);
	}

	private void deleteProject(IProject current) {
		processProjectListeners(current, false);
	}

	private void processProjectListeners(IProject current, boolean close) {
		synchronized (projectListeners) {
			List<IProjectWatcherListener> listeners = projectListeners.get(current);
			if (listeners != null) {
				for (IProjectWatcherListener listener : listeners) {
					if (close) {
						listener.onClosed(current);
					} else {
						listener.onDeleted(current);
					}
				}
			}
			// Remove all project listeners of the project
			projectListeners.remove(current);
		}
		synchronized (fileListeners) {
			// Remove all file listeners of the project
			fileListeners.remove(current);
		}
	}

	@Override
	public boolean visit(IResourceDelta delta) throws CoreException {
		IResource resource = delta.getResource();
		if (resource == null) {
			return false;
		}
		switch (resource.getType()) {
		case IResource.ROOT:
			return true;
		case IResource.PROJECT:
			IProject project = (IProject) resource;
			if (project.isOpen() && delta.getKind() == IResourceDelta.CHANGED
                    && ((delta.getFlags() & IResourceDelta.OPEN) != 0)) {
				// System.err.println("Open");
			}
			// Continue if project has defined file listeners.
			return fileListeners.containsKey(resource);
		case IResource.FOLDER:
			return true;
		case IResource.FILE:
			IFile file = (IFile) resource;
			synchronized (fileListeners) {
				Map<String, List<IFileWatcherListener>> listenersForFilename = fileListeners.get(file.getProject());
				List<IFileWatcherListener> listeners = listenersForFilename.get(file.getName());
				if (listeners != null) {
					for (IFileWatcherListener listener : listeners) {
						switch (delta.getKind()) {
						case IResourceDelta.ADDED:							
							// handle added resource
							listener.onAdded(file);
							break;
						case IResourceDelta.REMOVED:
							// handle removed resource
							listener.onDeleted(file);
							break;
						default:
							listener.onChanged(file);
						}
					}
				}
			}
			return false;
		}
		return false;
	}

}
