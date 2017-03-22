package ts.eclipse.ide.internal.ui.dialogs;

import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

import ts.eclipse.ide.ui.TypeScriptUIImageResource;

public class VersionLabelProvider extends LabelProvider {

	private static final VersionLabelProvider INSTANCE = new VersionLabelProvider();

	public static VersionLabelProvider getInstance() {
		return INSTANCE;
	}

	@Override
	public String getText(Object element) {
		if (element instanceof IContentProposal) {
			return ((IContentProposal) element).getLabel();
		}
		return super.getText(element);
	}

	@Override
	public Image getImage(Object element) {
		return TypeScriptUIImageResource.getImage(TypeScriptUIImageResource.IMG_NPM);
	}

}
