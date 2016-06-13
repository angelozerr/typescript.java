package ts.eclipse.ide.ui.preferences;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;

import ts.eclipse.ide.core.TypeScriptCorePlugin;
import ts.eclipse.ide.internal.ui.TypeScriptUIMessages;
import ts.eclipse.ide.internal.ui.dialogs.WorkspaceResourceSelectionDialog;
import ts.eclipse.ide.internal.ui.dialogs.WorkspaceResourceSelectionDialog.Mode;
import ts.utils.StringUtils;

public class BrowseButtonsComposite extends Composite {

	private Button browseFileSystemButton;
	private Button browseWorkspaceButton;

	public BrowseButtonsComposite(final Composite parent, final Combo filePathCombo, final IProject project, int style) {
		super(parent, style);
		super.setLayout(new GridLayout(2, false));
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		gd.horizontalAlignment = SWT.RIGHT;
		super.setLayoutData(gd);

		browseFileSystemButton = new Button(this, SWT.NONE);
		browseFileSystemButton.setText(TypeScriptUIMessages.Browse_FileSystem_button);
		browseFileSystemButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				DirectoryDialog dialog = new DirectoryDialog(parent.getShell());
				dialog.setFilterPath(filePathCombo.getText());
				String result = dialog.open();
				if (!StringUtils.isEmpty(result)) {
					filePathCombo.setText(result);
				}

			}
		});

		browseWorkspaceButton = new Button(this, SWT.NONE);
		browseWorkspaceButton.setText(TypeScriptUIMessages.Browse_Workspace_button);
		browseWorkspaceButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				WorkspaceResourceSelectionDialog dialog = new WorkspaceResourceSelectionDialog(parent.getShell(),
						Mode.FILE_FOLDER);
				IResource initialResource = TypeScriptCorePlugin.getTypeScriptRepositoryManager()
						.getResource(filePathCombo.getText(), project);
				if (initialResource != null) {
					dialog.setInitialSelection(initialResource);
				}
				if (dialog.open() == Window.OK) {
					IResource resource = (IResource) dialog.getFirstResult();
					filePathCombo.setText(TypeScriptCorePlugin.getTypeScriptRepositoryManager()
							.generateFileName(resource, project));
				}
			}
		});
	}

	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		this.browseFileSystemButton.setEnabled(enabled);
		this.browseWorkspaceButton.setEnabled(enabled);
	}

}
