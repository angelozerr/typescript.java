package ts.eclipse.ide.json.ui.internal;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.json.jsonpath.IJSONPath;
import org.eclipse.json.jsonpath.JSONPath;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.wst.json.core.databinding.JSONProperties;

import ts.eclipse.ide.json.ui.internal.tsconfig.TsconfigEditorMessages;

public abstract class AbstractFormPage extends FormPage {

	private DataBindingContext bindingContext;

	public AbstractFormPage(AbstractFormEditor editor, String id, String title) {
		super(editor, id, title);
	}

	@Override
	protected void createFormContent(IManagedForm managedForm) {
		final ScrolledForm form = managedForm.getForm();
		FormToolkit toolkit = managedForm.getToolkit();
		toolkit.decorateFormHeading(form.getForm());

		IToolBarManager manager = form.getToolBarManager();
		if (contributeToToolbar(manager)) {
			form.updateToolBar();
		}
		String titleText = getFormTitleText();
		if (titleText != null) {
			form.setText(titleText);
		}
		Image titleImage = getFormTitleImage();
		if (titleImage != null) {
			form.setImage(titleImage);
		}
		toolkit.decorateFormHeading(form.getForm());
		createUI(managedForm);
	}

	protected boolean contributeToToolbar(IToolBarManager manager) {
		return false;
	}

	protected String getFormTitleText() {
		return null;
	}

	protected Image getFormTitleImage() {
		return null;
	}

	protected Button createCheckbox(Composite parent, String label, IJSONPath path) {
		return createCheckbox(parent, label, path, null);
	}

	protected Button createCheckbox(Composite parent, String label, IJSONPath path, Boolean defaultValue) {
		Button checkbox = getToolkit().createButton(parent, label, SWT.CHECK);
		bind(checkbox, path, defaultValue);
		return checkbox;
	}

	protected void createTextAndBrowseButton(Composite parent, String label, IJSONPath path, boolean file) {
		Composite composite = getToolkit().createComposite(parent, SWT.NONE);
		GridData gd = new GridData();
		// gd.heightHint=0;
		// composite.setLayoutData(gd);
		GridLayout layout = new GridLayout(3, false);
		layout.marginWidth = 0;
		layout.marginBottom = 0;
		layout.marginTop = 0;
		layout.marginHeight = 0;
		layout.verticalSpacing = 0;
		composite.setLayout(layout);
		final Button checkbox = getToolkit().createButton(composite, label, SWT.CHECK);
		// GridData gd = new GridData();
		// gd.verticalIndent=0;
		checkbox.setLayoutData(gd);
		final Text text = getToolkit().createText(composite, "");
		final Button browse = getToolkit().createButton(composite, TsconfigEditorMessages.Button_browse, SWT.PUSH);
		// return text;

		text.setEnabled(false);
		browse.setEnabled(false);

		checkbox.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				text.setEnabled(checkbox.getSelection());
				browse.setEnabled(checkbox.getSelection());
			}
		});
	}

	protected void createCombo(Composite parent, String label, IJSONPath path, String[] values) {
		FormToolkit toolkit = getToolkit();
		Composite composite = toolkit.createComposite(parent, SWT.NONE);
		composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		GridLayout layout = new GridLayout(2, false);
		layout.marginWidth = 0;
		layout.marginBottom = 0;
		layout.marginTop = 0;
		layout.marginHeight = 0;
		layout.verticalSpacing = 0;
		composite.setLayout(layout);

		toolkit.createLabel(composite, label);

		CCombo combo = new CCombo(composite, SWT.DROP_DOWN | SWT.READ_ONLY | SWT.BORDER);
		combo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		combo.setItems(values);
		toolkit.adapt(combo, true, false);

		bind(combo, path);

	}

	protected FormToolkit getToolkit() {
		return getManagedForm().getToolkit();
	}

	public DataBindingContext getBindingContext() {
		if (bindingContext == null) {
			bindingContext = new DataBindingContext();
		}
		return bindingContext;
	}

	@Override
	public AbstractFormEditor getEditor() {
		return (AbstractFormEditor) super.getEditor();
	}

	protected abstract void createUI(IManagedForm managedForm);

	// --------------------- Bindings methods

	protected void updateUIBindings() {
		getBindingContext().updateTargets();
	}

	protected void bind(Button checkbox, JSONPath jsonPath) {
		bind(checkbox, jsonPath, null);
	}

	protected void bind(Button checkbox, IJSONPath jsonPath, Boolean defaultValue) {
		getBindingContext().bindValue(WidgetProperties.selection().observe(checkbox),
				JSONProperties.value(jsonPath, defaultValue).observe(getEditor().getDocument()));
	}

	protected void bind(CCombo combo, IJSONPath path) {
		getBindingContext().bindValue(WidgetProperties.selection().observe(combo),
				JSONProperties.value(path, null).observe(getEditor().getDocument()));
	}
}
