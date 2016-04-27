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
package ts.eclipse.ide.internal.core.resources;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.osgi.util.NLS;
import org.osgi.service.prefs.BackingStoreException;

import ts.eclipse.ide.core.TypeScriptCorePlugin;
import ts.eclipse.ide.internal.core.TypeScriptCoreMessages;

/**
 * Job to save project preferences.
 *
 */
class SaveProjectPreferencesJob extends WorkspaceJob {

	private final IEclipsePreferences preferences;
	private final IProject project;

	public SaveProjectPreferencesJob(IEclipsePreferences preferences, IProject project) {
		super(TypeScriptCoreMessages.SaveProjectPreferencesJob_name);
		this.preferences = preferences;
		this.project = project;
	}

	@Override
	public IStatus runInWorkspace(IProgressMonitor monitor) throws CoreException {
		if (monitor.isCanceled()) {
			return Status.CANCEL_STATUS;
		}
		monitor.beginTask(NLS.bind(TypeScriptCoreMessages.SaveProjectPreferencesJob_taskName, project.getName()), 1);

		try {
			preferences.flush();
		} catch (BackingStoreException e) {
			IStatus status = new Status(Status.ERROR, TypeScriptCorePlugin.PLUGIN_ID,
					"Error while saving  project preferences", e);
			throw new CoreException(status);
		}

		monitor.worked(1);
		monitor.done();

		return Status.OK_STATUS;
	}

}
