/**
 *  Copyright (c) 2015-2017 Angelo ZERR.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 *  Springrbua - TypeScript project wizard
 *
 */
package ts.eclipse.ide.internal.ui.wizards;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.ide.undo.CreateProjectOperation;
import org.eclipse.ui.ide.undo.WorkspaceUndoUtil;
import org.eclipse.ui.internal.ide.DialogUtil;
import org.eclipse.ui.internal.wizards.newresource.ResourceMessages;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import ts.eclipse.ide.core.utils.TypeScriptResourceUtil;
import ts.eclipse.ide.ui.wizards.AbstractNewProjectWizard;
import ts.resources.jsonconfig.TsconfigJson;
import ts.utils.IOUtils;

/**
 * Standard workbench wizard that creates a new TypeScript project resource in
 * the workspace.
 * <p>
 * This class may be instantiated and used without further configuration; this
 * class is not intended to be subclassed.
 * </p>
 * <p>
 * Example:
 *
 * <pre>
 * IWorkbenchWizard wizard = new NewTypeScriptProjectWizard();
 * wizard.init(workbench, selection);
 * WizardDialog dialog = new WizardDialog(shell, wizard);
 * dialog.open();
 * </pre>
 *
 * During the call to <code>open</code>, the wizard dialog is presented to the
 * user. When the user hits Finish, a project resource with the user-specified
 * name is created, the dialog closes, and the call to <code>open</code>
 * returns.
 * </p>
 *
 * @noextend This class is not intended to be subclassed by clients.
 */
public class NewTypeScriptProjectWizard extends AbstractNewProjectWizard {

	private TSConfigWizardPage tsconfigPage;
	private TypeScriptRuntimeAndNodejsWizardPage tsRuntimeAndNodeJsPage;
	
	public NewTypeScriptProjectWizard() {
		super("NewTypeScriptProjectWizard");
	}

	@Override
	public void addPages() {
		super.addPages();
		tsconfigPage = new TSConfigWizardPage();
		addPage(tsconfigPage);
		tsRuntimeAndNodeJsPage = new TypeScriptRuntimeAndNodejsWizardPage();
		addPage(tsRuntimeAndNodeJsPage);
	}

	@Override
	protected IRunnableWithProgress getRunnable(IProject newProjectHandle, IProjectDescription description,
			IPath projectLocation) {
		TsconfigJson json = new TsconfigJson();
		json.setCompileOnSave(true);
		tsconfigPage.addContents(json);
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		final String content = gson.toJson(json);
		return new IRunnableWithProgress() {

			@Override
			public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				final CreateProjectOperation op1 = new CreateProjectOperation(description,
						ResourceMessages.NewProject_windowTitle);
				try {
					// see bug
					// https://bugs.eclipse.org/bugs/show_bug.cgi?id=219901
					// directly execute the operation so that the undo state is
					// not preserved. Making this undoable resulted in too many
					// accidental file deletions.
					op1.execute(monitor, WorkspaceUndoUtil.getUIInfoAdapter(getShell()));
				} catch (ExecutionException e) {
					throw new InvocationTargetException(e);
				}

				// Add TypeScript builder
				try {
					TypeScriptResourceUtil.addTypeScriptBuilder(newProjectHandle);
				} catch (CoreException e) {
					throw new InvocationTargetException(e);
				}

				// Generate tsconfig.json
				IFile file = newProjectHandle.getFile(tsconfigPage.getPath());
				try {
					if (file.exists()) {
						file.setContents(IOUtils.toInputStream(content), 1, new NullProgressMonitor());
					} else {
						file.create(IOUtils.toInputStream(content), 1, new NullProgressMonitor());
					}
				} catch (CoreException e) {
					throw new InvocationTargetException(e);
				}

				getShell().getDisplay().syncExec(new Runnable() {

					@Override
					public void run() {
						selectAndReveal(file);

						// Open editor on new file.
						IWorkbenchWindow dw = getWorkbench().getActiveWorkbenchWindow();
						try {
							if (dw != null) {
								IWorkbenchPage page = dw.getActivePage();
								if (page != null) {
									IDE.openEditor(page, file, true);
								}
							}
						} catch (PartInitException e) {
							DialogUtil.openError(dw.getShell(), ResourceMessages.FileResource_errorMessage,
									e.getMessage(), e);
						}
					}
				});

			}
		};
	}
}