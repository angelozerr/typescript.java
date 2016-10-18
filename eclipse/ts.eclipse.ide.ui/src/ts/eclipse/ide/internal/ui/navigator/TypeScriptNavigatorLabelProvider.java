package ts.eclipse.ide.internal.ui.navigator;

import org.eclipse.core.resources.IContainer;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.ui.navigator.ICommonContentExtensionSite;
import org.eclipse.ui.navigator.ICommonLabelProvider;

import ts.eclipse.ide.core.resources.buildpath.ITypeScriptContainer;
import ts.eclipse.ide.core.resources.buildpath.ITypeScriptRootContainer;
import ts.eclipse.ide.core.utils.TypeScriptResourceUtil;
import ts.eclipse.ide.internal.ui.TypeScriptUIMessages;
import ts.eclipse.ide.ui.TypeScriptUIImageResource;
import ts.resources.ITypeScriptProject;

public class TypeScriptNavigatorLabelProvider implements ICommonLabelProvider {

	private static final WorkbenchLabelProvider INSTANCE = new WorkbenchLabelProvider();

	@Override
	public Image getImage(Object element) {
		if (element instanceof ITypeScriptProject) {
			return TypeScriptUIImageResource.getImage(TypeScriptUIImageResource.IMG_TYPESCRIPT_RESOURCES);
		} else if (element instanceof ITypeScriptContainer) {
			IContainer container = ((ITypeScriptContainer) element).getContainer();
			return INSTANCE.getImage(container);
		}
		return null;
	}

	@Override
	public String getText(Object element) {
		if (element instanceof ITypeScriptProject) {
			ITypeScriptProject tsProject = ((ITypeScriptProject) element);
			String tsVersion = tsProject.getProjectSettings().getTypeScriptVersion();
			return NLS.bind(TypeScriptUIMessages.TypeScriptResources, tsVersion);
		} else if (element instanceof ITypeScriptRootContainer) {
			IContainer container = ((ITypeScriptRootContainer) element).getContainer();
			return TypeScriptResourceUtil.getBuildPathLabel(container);
		} else if (element instanceof ITypeScriptContainer) {
			IContainer container = ((ITypeScriptContainer) element).getContainer();
			return INSTANCE.getText(container);
		}
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
