package ts.eclipse.ide.json.ui.internal;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.json.jsonpath.IJSONPath;
import org.eclipse.json.jsonpath.JSONPath;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.wst.json.core.databinding.JSONProperties;

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

	protected void bind(Button checkbox, JSONPath jsonPath) {
		bind(checkbox, jsonPath, null);
	}

	protected void bind(Button checkbox, IJSONPath jsonPath, Boolean defaultValue) {
		getBindingContext().bindValue(WidgetProperties.selection().observe(checkbox),
				JSONProperties.value(jsonPath, defaultValue).observe(getEditor().getDocument()));
	}

	protected Button createCheckbox(Composite parent, String label, IJSONPath path) {
		return createCheckbox(parent, label, path, null);
	}

	protected Button createCheckbox(Composite parent, String label, IJSONPath path, Boolean defaultValue) {
		Button checkbox = getToolkit().createButton(parent, label, SWT.CHECK);
		bind(checkbox, path, defaultValue);
		return checkbox;
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

	protected void updateUIBindings() {
		getBindingContext().updateTargets();
	}
}
