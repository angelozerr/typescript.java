package ts.eclipse.ide.json.ui.internal;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.json.jsonpath.IJSONPath;
import org.eclipse.json.jsonpath.JSONPath;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;

public abstract class AbstractFormPage extends FormPage {

	private DataBindingContext bindingContext;
	private List<TextAndBrowseButton> textAndBrowseButtons;

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

	protected TextAndBrowseButton createTextAndBrowseButton(Composite parent, String label, IJSONPath path,
			boolean isFile) {
		IEditorInput input = getEditorInput();
		IFile tsconfigFile = (input instanceof IFileEditorInput) ? ((IFileEditorInput) input).getFile() : null;
		TextAndBrowseButton control = new TextAndBrowseButton(label, getToolkit(), tsconfigFile, isFile, parent,
				SWT.NONE);
		control.bind(path, getEditor().getDocument(), getBindingContext());
		if (textAndBrowseButtons == null) {
			textAndBrowseButtons = new ArrayList<TextAndBrowseButton>();
		}
		textAndBrowseButtons.add(control);
		return control;
	}

	protected CCombo createCombo(Composite parent, String label, IJSONPath path, String[] values) {
		return createCombo(parent, label, path, values, null);
	}

	protected CCombo createCombo(Composite parent, String label, IJSONPath path, String[] values, String defaultValue) {
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

		bind(combo, path, defaultValue);
		return combo;
	}

	protected Text createText(Composite parent, String label, JSONPath path) {
		return createText(parent, label, path, null, null);
	}

	protected Text createText(Composite parent, String label, JSONPath path, String defaultValue, String message) {
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

		Text text = toolkit.createText(composite, "");
		if (message != null) {
			text.setMessage(message);
		}
		text.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		bind(text, path, defaultValue);
		return text;
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
		if (textAndBrowseButtons != null) {
			for (TextAndBrowseButton control : textAndBrowseButtons) {
				control.updateEnable();
			}
		}
	}

	protected void bindExists(Button checkbox, IJSONPath jsonPath, Object defaultValue) {
		JSONBindingUIHelper.bindExists(checkbox, jsonPath, defaultValue, getEditor().getDocument(),
				getBindingContext());
	}

	protected void bind(Button checkbox, IJSONPath jsonPath) {
		bind(checkbox, jsonPath, null);
	}

	protected void bind(Button checkbox, IJSONPath jsonPath, Boolean defaultValue) {
		JSONBindingUIHelper.bind(checkbox, jsonPath, defaultValue, getEditor().getDocument(), getBindingContext());
	}

	protected void bind(CCombo combo, IJSONPath path, String defaultValue) {
		JSONBindingUIHelper.bind(combo, path, defaultValue, getEditor().getDocument(), getBindingContext());
	}

	protected void bind(Text text, IJSONPath path, String defaultValue) {
		JSONBindingUIHelper.bind(text, path, defaultValue, getEditor().getDocument(), getBindingContext());
	}
}
