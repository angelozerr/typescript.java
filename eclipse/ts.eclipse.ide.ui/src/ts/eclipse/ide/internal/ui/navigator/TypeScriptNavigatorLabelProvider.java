package ts.eclipse.ide.internal.ui.navigator;

import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.navigator.ICommonContentExtensionSite;
import org.eclipse.ui.navigator.ICommonLabelProvider;

public class TypeScriptNavigatorLabelProvider implements ICommonLabelProvider {

	@Override
	public Image getImage(Object element) {
		return null;
	}

	@Override
	public String getText(Object element) {
		return null;
	}

	@Override
	public void addListener(ILabelProviderListener paramILabelProviderListener) {

	}

	@Override
	public void dispose() {

	}

	@Override
	public boolean isLabelProperty(Object paramObject, String paramString) {
		return false;
	}

	@Override
	public void removeListener(ILabelProviderListener paramILabelProviderListener) {

	}

	@Override
	public void restoreState(IMemento paramIMemento) {

	}

	@Override
	public void saveState(IMemento paramIMemento) {

	}

	@Override
	public String getDescription(Object paramObject) {
		return null;
	}

	@Override
	public void init(ICommonContentExtensionSite paramICommonContentExtensionSite) {

	}

}
