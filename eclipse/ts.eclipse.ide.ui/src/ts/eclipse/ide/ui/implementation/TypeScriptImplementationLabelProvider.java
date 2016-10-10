package ts.eclipse.ide.ui.implementation;

import org.eclipse.jface.viewers.LabelProvider;

public class TypeScriptImplementationLabelProvider  extends LabelProvider{

	@Override
	public String getText(Object element) {
		if (element instanceof FileSpan) {
			
		}
		return super.getText(element);
	}
}
