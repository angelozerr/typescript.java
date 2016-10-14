package ts.eclipse.ide.json.ui;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.text.IDocument;
import org.eclipse.json.jsonpath.IJSONPath;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;

import ts.eclipse.ide.core.utils.WorkbenchResourceUtil;
import ts.eclipse.ide.json.ui.internal.tsconfig.TsconfigEditorMessages;
import ts.eclipse.ide.ui.utils.DialogUtils;

public class TextAndBrowseButton extends Composite {

	private final String checkBoxLabel;
	private final FormToolkit toolkit;
	private final IFile tsconfigFile;
	private final boolean isFile;
	private Text textField;
	private Button checkbox;
	private Button browseButton;

	public TextAndBrowseButton(String checkBoxLabel, FormToolkit toolkit, IFile tsconfigFile, boolean isFile,
			Composite parent, int style) {
		super(parent, style);
		this.checkBoxLabel = checkBoxLabel;
		this.toolkit = toolkit;
		this.tsconfigFile = tsconfigFile;
		this.isFile = isFile;
		createBody(this);
	}

	private void createBody(Composite parent) {
		boolean fromWorkspace = tsconfigFile != null;
		Composite composite = parent;
		composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		GridLayout layout = new GridLayout(fromWorkspace ? 3 : 2, false);
		layout.marginWidth = 0;
		layout.marginBottom = 0;
		layout.marginTop = 0;
		layout.marginHeight = 0;
		layout.verticalSpacing = 0;
		composite.setLayout(layout);

		if (fromWorkspace) {
			// Display checkbox if tsconfig file comes from workspace (and from
			// file system)
			checkbox = getToolkit().createButton(composite, checkBoxLabel, SWT.CHECK);
		} else {
			// Otherwise, display a simple label.
			getToolkit().createLabel(composite, checkBoxLabel, SWT.NONE);
		}

		textField = getToolkit().createText(composite, "");
		textField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		if (fromWorkspace) {
			browseButton = getToolkit().createButton(composite, TsconfigEditorMessages.Button_browse,
					SWT.PUSH);

			textField.setEnabled(false);
			browseButton.setEnabled(false);

			if (checkbox != null) {
				checkbox.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						updateEnable();
						if (!checkbox.getSelection()) {
							textField.setText("");
						}
					}
				});
			}

			if (!isFile) {
				browseButton.addSelectionListener(new SelectionAdapter() {

					@Override
					public void widgetSelected(SelectionEvent e) {
						IResource resource = DialogUtils.openFolderDialog(textField.getText(),
								tsconfigFile.getProject(), false, browseButton.getShell());
						if (resource != null) {
							IPath path = WorkbenchResourceUtil.getRelativePath(resource, tsconfigFile.getParent());
							textField.setText(path.toString());
						}
					}
				});
			} else {
				browseButton.addSelectionListener(new SelectionAdapter() {

					@Override
					public void widgetSelected(SelectionEvent e) {
						IResource resource = DialogUtils.openResourceDialog(tsconfigFile.getProject(),
								browseButton.getShell());
						if (resource != null) {
							IPath path = WorkbenchResourceUtil.getRelativePath(resource, tsconfigFile.getParent());
							textField.setText(path.toString());
						}
					}
				});
			}

		}
	}

	public void bind(IJSONPath path, IDocument document, DataBindingContext context) {
		if (checkbox != null) {
			bindExists(checkbox, path, document, context);
		}
		bind(textField, path, document, context);
	}

	private void bindExists(Button checkbox, IJSONPath path, IDocument document, DataBindingContext context) {
		JSONBindingUIHelper.bindExists(checkbox, path, null, document, context);
	}

	private void bind(Text text, IJSONPath path, IDocument document, DataBindingContext context) {
		JSONBindingUIHelper.bind(text, path, "", document, context);
	}

	public FormToolkit getToolkit() {
		return toolkit;
	}

	public void updateEnable() {
		textField.setEnabled(checkbox.getSelection());
		browseButton.setEnabled(checkbox.getSelection());
	}
}
