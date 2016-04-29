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
package ts.eclipse.ide.internal.ui.navigator;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Control;

import ts.eclipse.ide.core.TypeScriptCorePlugin;
import ts.eclipse.ide.core.resources.IIDETypeScriptProject;
import ts.eclipse.ide.core.resources.ITypeScriptElementChangedListener;
import ts.eclipse.ide.core.resources.buildpath.ITypeScriptBuildPath;
import ts.eclipse.ide.core.resources.buildpath.ITypeScriptRootContainer;
import ts.eclipse.ide.core.utils.TypeScriptResourceUtil;
import ts.resources.ITypeScriptProject;

/**
 * TypeScript navigator used to display for *.ts file, *.js and *.js.map files
 * as children.
 *
 */
public class TypeScriptNavigatorContentProvider
		implements ITreeContentProvider/* , ITreePathContentProvider */, ITypeScriptElementChangedListener {

	public static final Object[] NO_CHILDREN = new Object[0];

	private Viewer viewer;

	public TypeScriptNavigatorContentProvider() {
		TypeScriptCorePlugin.getDefault().addTypeScriptElementChangedListener(this);
	}

	@Override
	public Object[] getElements(Object element) {
		return NO_CHILDREN;
	}

	@Override
	public Object[] getChildren(Object element) {
		Object[] children = getChildrenOrNull(element);
		return children != null ? children : NO_CHILDREN;
	}

	private Object[] getChildrenOrNull(Object element) {
		if (element instanceof IResource) {
			IResource resource = (IResource) element;
			Object[] children = getChildren(resource);
			return children;
		} else if ((element instanceof IIDETypeScriptProject)) {
			IIDETypeScriptProject tsProject = (IIDETypeScriptProject) element;
			return tsProject.getTypeScriptBuildPath().getRootContainers();
		} else if ((element instanceof ITypeScriptRootContainer)) {
			return null; // super.getChildren(((ContainerWrapper)
							// element).getContainer());
		}
		return null;
	}

	private Object[] getChildren(IResource resource) {
		switch (resource.getType()) {
		case IResource.PROJECT:
			return getTypescriptResources((IProject) resource);
		case IResource.FILE:
			return getEmmitedFiles((IFile) resource);
		}
		return null; // super.getChildren(resource);
	}

	private Object[] getTypescriptResources(IProject project) {
		if (TypeScriptResourceUtil.isTypeScriptProject(project)) {
			try {
				IIDETypeScriptProject tsProject = TypeScriptResourceUtil.getTypeScriptProject(project);
				return new Object[] { tsProject };
			} catch (CoreException e) {

			}
		}
		return null;
	}

	private Object[] getEmmitedFiles(IFile file) {
		try {
			return TypeScriptResourceUtil.getEmittedFiles(file);
		} catch (CoreException e) {
			return null;
		}
	}

	@Override
	public boolean hasChildren(Object element) {
		if (element instanceof IResource) {
			// for performance, returns true to avoid loading twice compiled
			// resources *.js and *.js.map
			return TypeScriptResourceUtil.isTsOrTsxFile(element);
		} else if (element instanceof ITypeScriptProject) {
			return true;
		} else if ((element instanceof ITypeScriptRootContainer)) {
			// TODO: fill with *.ts files according tsconfig.json config (files,
			// exclude).
			return false;
		}
		return false;
	}

	@Override
	public void dispose() {
		TypeScriptCorePlugin.getDefault().removeTypeScriptElementChangedListener(this);
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		this.viewer = viewer;
	}

	@Override
	public void buildPathChanged(IIDETypeScriptProject tsProject, ITypeScriptBuildPath oldBuildPath,
			ITypeScriptBuildPath newBuildPath) {
		Control ctrl = viewer.getControl();
		if (ctrl == null || ctrl.isDisposed()) {
			return;
		}

		final Collection<Runnable> runnables = new ArrayList<Runnable>();
		processChanged(tsProject, oldBuildPath, newBuildPath, runnables);

		if (runnables.isEmpty()) {
			return;
		}

		// Are we in the UIThread? If so spin it until we are done
		if (ctrl.getDisplay().getThread() == Thread.currentThread()) {
			runUpdates(runnables);
		} else {
			ctrl.getDisplay().asyncExec(new Runnable() {

				@Override
				public void run() {
					// Abort if this happens after disposes
					Control ctrl = viewer.getControl();
					if (ctrl == null || ctrl.isDisposed()) {
						return;
					}

					runUpdates(runnables);
				}
			});
		}

	}

	private void processChanged(final IIDETypeScriptProject tsProject, final ITypeScriptBuildPath oldBuildPath,
			final ITypeScriptBuildPath newBuildPath, Collection<Runnable> runnables) {
		// he widget may have been destroyed
		// by the time this is run. Check for this and do nothing if so.
		Control ctrl = viewer.getControl();
		if (ctrl == null || ctrl.isDisposed()) {
			return;
		}

		final IProject project = tsProject.getProject();
		Runnable addAndRemove = new Runnable() {
			public void run() {
				if (viewer instanceof AbstractTreeViewer) {
					AbstractTreeViewer treeViewer = (AbstractTreeViewer) viewer;
					// Disable redraw until the operation is finished so we
					// don't
					// get a flash of both the new and old item (in the case of
					// rename)
					// Only do this if we're both adding and removing files (the
					// rename case)
					/*
					 * if (hasRename) {
					 * treeViewer.getControl().setRedraw(false); }
					 */
					try {
						/*
						 * if (oldBuildPath != null) { treeViewer.remove(new
						 * Object[] { oldBuildPath }); } if (newBuildPath !=
						 * null && newBuildPath.getContainers().size() > 0) {
						 * treeViewer.add(project, new Object[] { newBuildPath
						 * }); }
						 */
						if (!hasBuildPath(newBuildPath)) {
							treeViewer.remove(project, new Object[] { tsProject });
						} else {
							if (!hasBuildPath(oldBuildPath)) {
								treeViewer.add(project, new Object[] { tsProject });
							} else {
								treeViewer.refresh(tsProject);
							}
						}
					} finally {
						/*
						 * if (hasRename) {
						 * treeViewer.getControl().setRedraw(true); }
						 */
					}
				} else {
					((StructuredViewer) viewer).refresh(project);
				}
			}

			private boolean hasBuildPath(final ITypeScriptBuildPath buildPath) {
				return buildPath != null && buildPath.hasRootContainers();
			}
		};
		runnables.add(addAndRemove);
	}

	/**
	 * Run all of the runnables that are the widget updates
	 * 
	 * @param runnables
	 */
	private void runUpdates(Collection<Runnable> runnables) {
		for (Runnable runnable : runnables) {
			runnable.run();
		}

	}

	@Override
	public Object getParent(Object element) {
		return null;
	}

}
