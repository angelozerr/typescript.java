package ts.eclipse.ide.json.ui;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.text.IDocument;
import org.eclipse.json.jsonpath.IJSONPath;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wst.json.core.databinding.JSONProperties;

public class JSONBindingUIHelper {

	public static void bindExists(Button checkbox, IJSONPath jsonPath, Object defaultValue, IDocument document,
			DataBindingContext context) {
		context.bindValue(WidgetProperties.selection().observe(checkbox),
				JSONProperties.valueExists(jsonPath, defaultValue).observe(document));
	}

	public static void bind(Button checkbox, IJSONPath jsonPath, IDocument document, DataBindingContext context) {
		bind(checkbox, jsonPath, null, document, context);
	}

	public static void bind(Button checkbox, IJSONPath jsonPath, Boolean defaultValue, IDocument document,
			DataBindingContext context) {
		context.bindValue(WidgetProperties.selection().observe(checkbox),
				JSONProperties.value(jsonPath, defaultValue).observe(document));
	}

	public static void bind(CCombo combo, IJSONPath path, String defaultValue, IDocument document,
			DataBindingContext context) {
		context.bindValue(WidgetProperties.selection().observe(combo),
				JSONProperties.value(path, defaultValue).observe(document));
	}

	public static void bind(Text text, IJSONPath path, String defaultValue, IDocument document,
			DataBindingContext context) {
		context.bindValue(WidgetProperties.text(SWT.Modify).observe(text),
				JSONProperties.value(path, defaultValue).observe(document));
	}

}
