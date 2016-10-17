/*******************************************************************************
 * Copyright (c) 2000, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package ts.eclipse.ide.ui.launch;

import java.io.File;

import org.eclipse.core.externaltools.internal.IExternalToolConstants;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.variables.IStringVariableManager;
import org.eclipse.core.variables.VariablesPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.debug.ui.StringVariableSelectionDialog;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.accessibility.AccessibleAdapter;
import org.eclipse.swt.accessibility.AccessibleEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ContainerSelectionDialog;

import ts.eclipse.ide.internal.ui.TypeScriptUIMessages;
import ts.eclipse.ide.ui.TypeScriptUIPlugin;

/**
 * Abstract Main tab for launch which uses nodejs. It is composed with:
 * 
 * <ul>
 * <li>node</li>
 * <li>command</li>
 * <li>arguments</li>
 * </ul>
 *
 */
public abstract class AbstractMainTab extends AbstractLaunchConfigurationTab {

	private final static String FIRST_EDIT = "editedByMainTab"; //$NON-NLS-1$

	protected Text workDirectoryField;
	protected Button fileWorkingDirectoryButton;
	protected Button workspaceWorkingDirectoryButton;
	protected Button variablesWorkingDirectoryButton;
	protected Combo commandsCommbo;

	protected boolean fInitializing = false;
	private boolean userEdited = false;

	protected WidgetListener fListener = new WidgetListener();

	/**
	 * A listener to update for text modification and widget selection.
	 */
	protected class WidgetListener extends SelectionAdapter implements ModifyListener {
		@Override
		public void modifyText(ModifyEvent e) {
			if (!fInitializing) {
				setDirty(true);
				userEdited = true;
				updateLaunchConfigurationDialog();
			}
		}

		@Override
		public void widgetSelected(SelectionEvent e) {
			setDirty(true);
			Object source = e.getSource();
			if (source == workspaceWorkingDirectoryButton) {
				handleWorkspaceWorkingDirectoryButtonSelected();
			} else if (source == fileWorkingDirectoryButton) {
				handleFileWorkingDirectoryButtonSelected();
			} else if (source == variablesWorkingDirectoryButton) {
				handleVariablesButtonSelected(workDirectoryField);
			}
		}

	}

	@Override
	public void createControl(Composite parent) {
		Composite mainComposite = new Composite(parent, SWT.NONE);
		setControl(mainComposite);
		mainComposite.setFont(parent.getFont());
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		mainComposite.setLayout(layout);
		mainComposite.setLayoutData(gridData);

		createBodyComponents(mainComposite);
		createVerticalSpacer(mainComposite, 1);

		Dialog.applyDialogFont(parent);
	}

	protected void createBodyComponents(Composite parent) {
		createWorkDirectoryComponent(parent);
	}

	/**
	 * Creates the controls needed to edit the working directory attribute of an
	 * external tool
	 *
	 * @param parent
	 *            the composite to create the controls in
	 */
	protected void createWorkDirectoryComponent(Composite parent) {
		Group group = new Group(parent, SWT.NONE);
		String groupName = TypeScriptUIMessages.Launch_MainTab_workingDir;
		group.setText(groupName);
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		group.setLayout(layout);
		group.setLayoutData(gridData);

		workDirectoryField = new Text(group, SWT.BORDER);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.widthHint = IDialogConstants.ENTRY_FIELD_WIDTH;
		workDirectoryField.setLayoutData(data);
		workDirectoryField.addModifyListener(fListener);
		addControlAccessibleListener(workDirectoryField, group.getText());

		Composite buttonComposite = new Composite(group, SWT.NONE);
		layout = new GridLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		layout.numColumns = 3;
		gridData = new GridData(GridData.HORIZONTAL_ALIGN_END);
		buttonComposite.setLayout(layout);
		buttonComposite.setLayoutData(gridData);
		buttonComposite.setFont(parent.getFont());

		workspaceWorkingDirectoryButton = createPushButton(buttonComposite,
				TypeScriptUIMessages.Browse_Workspace_button, null);
		workspaceWorkingDirectoryButton.addSelectionListener(fListener);
		addControlAccessibleListener(workspaceWorkingDirectoryButton,
				group.getText() + " " + workspaceWorkingDirectoryButton.getText()); //$NON-NLS-1$

		fileWorkingDirectoryButton = createPushButton(buttonComposite, TypeScriptUIMessages.Browse_FileSystem_button,
				null);
		fileWorkingDirectoryButton.addSelectionListener(fListener);
		addControlAccessibleListener(fileWorkingDirectoryButton, group.getText()); // $NON-NLS-1$

		variablesWorkingDirectoryButton = createPushButton(buttonComposite, TypeScriptUIMessages.Variables_button,
				null);
		variablesWorkingDirectoryButton.addSelectionListener(fListener);
		addControlAccessibleListener(variablesWorkingDirectoryButton,
				group.getText() + " " + variablesWorkingDirectoryButton.getText()); //$NON-NLS-1$
	}

	// protected void handleProjectButtonSelected() {
	// IProject project = DialogUtils.openProjectDialog(null,
	// Display.getDefault().getActiveShell());
	// if (project != null) {
	// workingDirText.setText(AngularCLILaunchHelper.getWorkingDir(project));
	// }
	// }

	// private void createCommandComponent(Composite parent) {
	// Group group = new Group(parent, SWT.NONE);
	// String groupName =
	// AngularCLIMessages.AngularCLILaunchTabGroup_MainTab_command;
	// group.setText(groupName);
	// GridLayout layout = new GridLayout();
	// layout.numColumns = 1;
	// GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
	// group.setLayout(layout);
	// group.setLayoutData(gridData);
	//
	// commandsCommbo = new Combo(group, SWT.BORDER | SWT.H_SCROLL);
	//// String[] items = new String[NgCommand.values().length];
	//// for (int i = 0; i < items.length; i++) {
	//// items[i] = NgCommand.values()[i].getAliases()[0];
	//// }
	// commandsCommbo.setItems(items);
	// commandsCommbo.addModifyListener(fListener);
	// }

	@Override
	public final void initializeFrom(ILaunchConfiguration configuration) {
		fInitializing = true;
		doInitializeFrom(configuration);
		fInitializing = false;
		setDirty(false);
	}

	protected void doInitializeFrom(ILaunchConfiguration configuration) {
		updateWorkingDirectory(configuration);
	}

	/**
	 * Updates the working directory widgets to match the state of the given
	 * launch configuration.
	 */
	protected void updateWorkingDirectory(ILaunchConfiguration configuration) {
		String workingDir = IExternalToolConstants.EMPTY_STRING;
		try {
			workingDir = configuration.getAttribute(IExternalToolConstants.ATTR_WORKING_DIRECTORY,
					IExternalToolConstants.EMPTY_STRING);
		} catch (CoreException ce) {
			TypeScriptUIPlugin.log("Error while reading ng configuration", ce);
		}
		workDirectoryField.setText(workingDir);
	}

	@Override
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		String workingDirectory = workDirectoryField.getText().trim();
		if (workingDirectory.length() == 0) {
			configuration.setAttribute(IExternalToolConstants.ATTR_WORKING_DIRECTORY, (String) null);
		} else {
			configuration.setAttribute(IExternalToolConstants.ATTR_WORKING_DIRECTORY, workingDirectory);
		}
		if (userEdited) {
			configuration.setAttribute(FIRST_EDIT, (String) null);
		}
	}

	@Override
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
		configuration.setAttribute(FIRST_EDIT, true);
	}

	@Override
	public String getName() {
		return TypeScriptUIMessages.Launch_MainTab_title;
	}

	@Override
	public boolean isValid(ILaunchConfiguration launchConfig) {
		setErrorMessage(null);
		setMessage(null);
		boolean newConfig = false;
		try {
			newConfig = launchConfig.getAttribute(FIRST_EDIT, false);
		} catch (CoreException e) {
			// assume false is correct
		}
		return validate(newConfig);
	}

	protected boolean validate(boolean newConfig) {
		return validateWorkDirectory();
	}

	/**
	 * Validates the content of the working directory field.
	 */
	protected boolean validateWorkDirectory() {
		String dir = workDirectoryField.getText().trim();
		if (dir.length() <= 0) {
			return true;
		}

		String expandedDir = null;
		try {
			expandedDir = resolveValue(dir);
			if (expandedDir == null) { // a variable that needs to be resolved
										// at runtime
				return true;
			}
		} catch (CoreException e) {
			setErrorMessage(e.getStatus().getMessage());
			return false;
		}

		File file = new File(expandedDir);
		if (!file.exists()) { // The directory does not exist.
			setErrorMessage(TypeScriptUIMessages.Launch_MainTab_workingDir_does_not_exist_or_is_invalid);
			return false;
		}
		if (!file.isDirectory()) {
			setErrorMessage(TypeScriptUIMessages.Launch_MainTab_Not_a_directory);
			return false;
		}
		return true;
	}

	public void addControlAccessibleListener(Control control, String controlName) {
		// strip mnemonic (&)
		String[] strs = controlName.split("&"); //$NON-NLS-1$
		StringBuffer stripped = new StringBuffer();
		for (int i = 0; i < strs.length; i++) {
			stripped.append(strs[i]);
		}
		control.getAccessible().addAccessibleListener(new ControlAccessibleListener(stripped.toString()));
	}

	private class ControlAccessibleListener extends AccessibleAdapter {
		private String controlName;

		ControlAccessibleListener(String name) {
			controlName = name;
		}

		@Override
		public void getName(AccessibleEvent e) {
			e.result = controlName;
		}

	}

	/**
	 * Prompts the user to choose a working directory from the filesystem.
	 */
	protected void handleFileWorkingDirectoryButtonSelected() {
		DirectoryDialog dialog = new DirectoryDialog(getShell(), SWT.SAVE);
		dialog.setMessage(TypeScriptUIMessages.Launch_MainTab_select_workingDir);
		dialog.setFilterPath(workDirectoryField.getText());
		String text = dialog.open();
		if (text != null) {
			workDirectoryField.setText(text);
		}
	}

	/**
	 * A variable entry button has been pressed for the given text field. Prompt
	 * the user for a variable and enter the result in the given field.
	 */
	private void handleVariablesButtonSelected(Text textField) {
		String variable = getVariable();
		if (variable != null) {
			textField.insert(variable);
		}
	}

	/**
	 * Prompts the user to choose and configure a variable and returns the
	 * resulting string, suitable to be used as an attribute.
	 */
	private String getVariable() {
		StringVariableSelectionDialog dialog = new StringVariableSelectionDialog(getShell());
		dialog.open();
		return dialog.getVariableExpression();
	}

	/**
	 * Prompts the user for a working directory location within the workspace
	 * and sets the working directory as a String containing the workspace_loc
	 * variable or <code>null</code> if no location was obtained from the user.
	 */
	protected void handleWorkspaceWorkingDirectoryButtonSelected() {
		ContainerSelectionDialog containerDialog;
		containerDialog = new ContainerSelectionDialog(getShell(), ResourcesPlugin.getWorkspace().getRoot(), false,
				TypeScriptUIMessages.Launch_MainTab_select_workingDir);
		containerDialog.open();
		Object[] resource = containerDialog.getResult();
		String text = null;
		if (resource != null && resource.length > 0) {
			text = newVariableExpression("workspace_loc", ((IPath) resource[0]).toString()); //$NON-NLS-1$
		}
		if (text != null) {
			workDirectoryField.setText(text);
		}
	}

	/**
	 * Returns a new variable expression with the given variable and the given
	 * argument.
	 * 
	 * @see IStringVariableManager#generateVariableExpression(String, String)
	 */
	protected String newVariableExpression(String varName, String arg) {
		return VariablesPlugin.getDefault().getStringVariableManager().generateVariableExpression(varName, arg);
	}

	/**
	 * Validates the variables of the given string to determine if all variables
	 * are valid
	 *
	 * @param expression
	 *            expression with variables
	 * @exception CoreException
	 *                if a variable is specified that does not exist
	 */
	private void validateVaribles(String expression) throws CoreException {
		IStringVariableManager manager = VariablesPlugin.getDefault().getStringVariableManager();
		manager.validateStringVariables(expression);
	}

	private String resolveValue(String expression) throws CoreException {
		String expanded = null;
		try {
			expanded = getValue(expression);
		} catch (CoreException e) { // possibly just a variable that needs to be
									// resolved at runtime
			validateVaribles(expression);
			return null;
		}
		return expanded;
	}

	/**
	 * Validates the value of the given string to determine if any/all variables
	 * are valid
	 *
	 * @param expression
	 *            expression with variables
	 * @return whether the expression contained any variable values
	 * @exception CoreException
	 *                if variable resolution fails
	 */
	private String getValue(String expression) throws CoreException {
		IStringVariableManager manager = VariablesPlugin.getDefault().getStringVariableManager();
		return manager.performStringSubstitution(expression);
	}

}
