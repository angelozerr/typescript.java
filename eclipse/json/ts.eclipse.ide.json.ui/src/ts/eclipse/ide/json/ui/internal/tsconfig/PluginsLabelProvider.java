package ts.eclipse.ide.json.ui.internal.tsconfig;

import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

public class PluginsLabelProvider extends LabelProvider implements ILabelDecorator {

	private static final PluginsLabelProvider INSTANCE = new PluginsLabelProvider();

	public static PluginsLabelProvider getInstance() {
		return INSTANCE;
	}

	@Override
	public Image decorateImage(Image arg0, Object arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String decorateText(String arg0, Object arg1) {
		// TODO Auto-generated method stub
		return null;
	}

}
