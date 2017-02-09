/**
 *  Copyright (c) 2013-2016 Angelo ZERR.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package ts.eclipse.ide.ui.utils;

import java.util.Collection;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;
import org.eclipse.ui.dialogs.SelectionDialog;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;

import ts.eclipse.ide.internal.ui.dialogs.FolderSelectionDialog;
import ts.eclipse.ide.internal.ui.dialogs.OpenResourceDialog;
import ts.eclipse.ide.internal.ui.dialogs.OpenTypeScriptResourceDialog;
import ts.utils.StringUtils;

/***
 * Dialog utilities to open resources.
 * 
 */
public class DialogUtils {

	private DialogUtils() {
	}

	public static IResource openResourceDialog(IProject project, Shell shell, int typesMask) {
		OpenResourceDialog dialog = new OpenResourceDialog(shell, false, project, typesMask);
		if (dialog.open() != Window.OK) {
			return null;
		}
		Object[] results = dialog.getResult();
		if (results != null && results.length > 0) {
			return (IResource) results[0];
		}
		return null;
	}

	public static IResource openResourceDialog(IProject project, Shell shell) {
		return openResourceDialog(project, shell, IResource.FILE | IResource.FOLDER);
	}

	public static Object[] openResourcesDialog(IProject project, Shell shell) {
		OpenResourceDialog dialog = new OpenResourceDialog(shell, true, project, IResource.FILE | IResource.FOLDER);
		if (dialog.open() != Window.OK) {
			return null;
		}
		return dialog.getResult();
	}

	public static Object[] openTypeScriptResourcesDialog(IProject project, Collection<IResource> existingFiles,
			Shell shell) {
		OpenTypeScriptResourceDialog dialog = new OpenTypeScriptResourceDialog(shell, true, project, existingFiles,
				IResource.FILE);
		if (dialog.open() != Window.OK) {
			return null;
		}
		return dialog.getResult();
	}

	public static IProject openProjectDialog(String initialProject, Shell shell) {
		SelectionDialog dialog = createFolderDialog(initialProject, null, true, false, shell);
		if (dialog.open() != Window.OK) {
			return null;
		}
		Object[] results = dialog.getResult();
		if (results != null && results.length > 0) {
			return (IProject) results[0];
		}
		return null;
	}

	public static IResource openFolderDialog(String initialFolder, IProject project, boolean showAllProjects,
			Shell shell) {
		SelectionDialog dialog = createFolderDialog(initialFolder, project, showAllProjects, true, shell);
		if (dialog.open() != Window.OK) {
			return null;
		}
		Object[] results = dialog.getResult();
		if (results != null && results.length > 0) {
			return (IResource) results[0];
		}
		return null;
	}

	public static ElementTreeSelectionDialog createFolderDialog(String initialFolder, final IProject project,
			final boolean showAllProjects, final boolean showFolder, Shell shell) {

		ILabelProvider lp = new WorkbenchLabelProvider();
		ITreeContentProvider cp = new WorkbenchContentProvider();
		FolderSelectionDialog dialog = new FolderSelectionDialog(shell, lp, cp);
		// dialog.setTitle(TypeScriptUIMessages.TernModuleOptionsPanel_selectPathDialogTitle);
		IContainer folder = StringUtils.isEmpty(initialFolder) ? project
				: (project != null ? project.getFolder(initialFolder)
						: ResourcesPlugin.getWorkspace().getRoot().getFolder(new Path(initialFolder)));
		if (folder != null && folder.exists()) {
			dialog.setInitialSelection(folder);
		}
		dialog.setInput(ResourcesPlugin.getWorkspace().getRoot());
		ViewerFilter filter = new ViewerFilter() {

			@Override
			public boolean select(Viewer viewer, Object parentElement, Object element) {
				if (element instanceof IProject) {
					if (showAllProjects)
						return true;
					IProject p = (IProject) element;
					return (p.equals(project));
				} else if (element instanceof IContainer) {
					IContainer container = (IContainer) element;
					if (showFolder && container.getType() == IResource.FOLDER) {
						return true;
					}
					return false;
				}
				return false;
			}
		};
		dialog.addFilter(filter);
		return dialog;
	}

}
