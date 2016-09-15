package ts.eclipse.ide.json.ui.internal;

import java.util.Properties;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;

public abstract class AbstractFormPage extends FormPage {

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
		createUI(managedForm, toolkit);
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

	protected static class InputField {

		private final Text text;
		private final ControlDecoration decoration;
		private final String propertyName;
		private final IFile file;

		public InputField(Text text, ControlDecoration decoration, String propertyName, IFile file) {
			this.text = text;
			this.decoration = decoration;
			this.propertyName = propertyName;
			this.file = file;
		}

		public Text getText() {
			return text;
		}

		public ControlDecoration getDecoration() {
			return decoration;
		}

		public void refresh() {
			decoration.setDescriptionText("");
			decoration.hide();

			text.setText("");
			if (file.exists()) {
				Properties properties = new Properties();
				try {
					properties.load(file.getContents());
					String value = (String) properties.get(propertyName);
					if (value != null) {
						text.setText(value);
					}
				} catch (Exception e) {
					decoration.setDescriptionText(e.getMessage());
					decoration.show();
				}
			} else {
				decoration.setDescriptionText("Le fichier de propriété '" + file.getLocation() + "' n'existe pas.");
				decoration.show();
			}
		}
	}

	protected abstract void createUI(IManagedForm managedForm, FormToolkit toolkit);
}
