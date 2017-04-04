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
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.tm.terminal.view.core.interfaces.constants.ITerminalsConnectorConstants;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.ide.undo.CreateProjectOperation;
import org.eclipse.ui.ide.undo.WorkspaceUndoUtil;
import org.eclipse.ui.internal.ide.DialogUtil;
import org.eclipse.ui.internal.wizards.newresource.ResourceMessages;
import org.osgi.service.prefs.BackingStoreException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import ts.eclipse.ide.core.TypeScriptCorePlugin;
import ts.eclipse.ide.core.preferences.TypeScriptCorePreferenceConstants;
import ts.eclipse.ide.core.utils.TypeScriptResourceUtil;
import ts.eclipse.ide.internal.ui.TypeScriptUIMessages;
import ts.eclipse.ide.terminal.interpreter.CommandTerminalService;
import ts.eclipse.ide.ui.TypeScriptUIImageResource;
import ts.eclipse.ide.ui.wizards.AbstractNewProjectWizard;
import ts.resources.jsonconfig.TsconfigJson;
import ts.utils.IOUtils;
import ts.utils.StringUtils;

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

	private static final String WIZARD_NAME = "NewTypeScriptProjectWizard";
	private TSConfigWizardPage tsconfigPage;
	private TypeScriptRuntimeAndNodejsWizardPage tsRuntimeAndNodeJsPage;
	private TSLintWizardPage tslintPage;

	public NewTypeScriptProjectWizard() {
		super(WIZARD_NAME, TypeScriptUIMessages.NewTypeScriptProjectWizard_newProjectTitle,
				TypeScriptUIMessages.NewTypeScriptProjectWizard_newProjectDescription);
	}

	@Override
	public void init(IWorkbench workbench, IStructuredSelection currentSelection) {
		super.init(workbench, currentSelection);
		setNeedsProgressMonitor(true);
		setWindowTitle(TypeScriptUIMessages.NewTypeScriptProjectWizard_windowTitle);
	}

	@Override
	protected void initializeDefaultPageImageDescriptor() {
		ImageDescriptor desc = TypeScriptUIImageResource
				.getImageDescriptor(TypeScriptUIImageResource.IMG_TS_PROJECT_WIZBAN);// $NON-NLS-1$
		setDefaultPageImageDescriptor(desc);
	}

	@Override
	public void addPages() {
		super.addPages();
		tsconfigPage = new TSConfigWizardPage();
		addPage(tsconfigPage);
		tsRuntimeAndNodeJsPage = new TypeScriptRuntimeAndNodejsWizardPage();
		addPage(tsRuntimeAndNodeJsPage);
		tslintPage = new TSLintWizardPage();
		addPage(tslintPage);
	}

	@Override
	protected IRunnableWithProgress getRunnable(IProject newProjectHandle, IProjectDescription description,
			IPath projectLocationPath) {
		// Update tsconfig.json
		TsconfigJson tsconfig = new TsconfigJson();
		tsconfig.setCompileOnSave(true);
		tsconfigPage.updateTsconfig(tsconfig);
		tslintPage.updateTsconfig(tsconfig);
		
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
				Gson gson = new GsonBuilder().setPrettyPrinting().create();
				String content = gson.toJson(tsconfig);
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
				
				IEclipsePreferences preferences = new ProjectScope(newProjectHandle)
						.getNode(TypeScriptCorePlugin.PLUGIN_ID);				

				// Update node.js preferences
				tsRuntimeAndNodeJsPage.updateNodeJSPreferences(preferences);
				
				// Install TypeScript/tslint if needed
				List<String> commands = new ArrayList<>();
				boolean customTypeScript = tsRuntimeAndNodeJsPage.updateCommand(commands);				
				boolean customTslint = tslintPage.updateCommand(commands);
			
				if (!commands.isEmpty()) {
					// Prepare terminal properties
					String terminalId = "TypeScript Projects";
					Map<String, Object> properties = new HashMap<String, Object>();
					properties.put(ITerminalsConnectorConstants.PROP_TITLE, terminalId);
					properties.put(ITerminalsConnectorConstants.PROP_ENCODING, StandardCharsets.UTF_8.name());
					properties.put(ITerminalsConnectorConstants.PROP_PROCESS_WORKING_DIR,
							projectLocationPath.toString());
					properties.put(ITerminalsConnectorConstants.PROP_DELEGATE_ID,
							"ts.eclipse.ide.terminal.interpreter.LocalInterpreterLauncherDelegate");
					properties.put(ITerminalsConnectorConstants.PROP_TERMINAL_CONNECTOR_ID,
							"org.eclipse.tm.terminal.connector.local.LocalConnector");

					CommandTerminalService.getInstance().executeCommand(commands, terminalId, properties, null);

					if (customTypeScript || customTslint) {						
						if (customTypeScript) {
							preferences.putBoolean(TypeScriptCorePreferenceConstants.USE_EMBEDDED_TYPESCRIPT, false);
							preferences.put(TypeScriptCorePreferenceConstants.INSTALLED_TYPESCRIPT_PATH,
									"${project_loc:node_modules/typescript}");
						}						
					}
				}
				try {
					preferences.flush();
				} catch (BackingStoreException e) {
					e.printStackTrace();
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