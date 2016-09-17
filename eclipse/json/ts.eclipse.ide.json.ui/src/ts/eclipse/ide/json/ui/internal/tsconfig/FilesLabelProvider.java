package ts.eclipse.ide.json.ui.internal.tsconfig;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

import ts.eclipse.ide.core.utils.TypeScriptResourceUtil;
import ts.eclipse.ide.ui.TypeScriptUIImageResource;

public class FilesLabelProvider extends LabelProvider {

	private static final FilesLabelProvider INSTANCE = new FilesLabelProvider();

	public static FilesLabelProvider getInstance() {
		return INSTANCE;
	}

	@Override
	public Image getImage(Object element) {
		if (TypeScriptResourceUtil.isTsxOrJsxFile(element)) {
			return TypeScriptUIImageResource.getImage(TypeScriptUIImageResource.IMG_JSX);
		} else if (TypeScriptResourceUtil.isTsOrTsxFile(element)) {
			return TypeScriptUIImageResource.getImage(TypeScriptUIImageResource.IMG_TS);
		}
		return super.getImage(element);
	}
}
