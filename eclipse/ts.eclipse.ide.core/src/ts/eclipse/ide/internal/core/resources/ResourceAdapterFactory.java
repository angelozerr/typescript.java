package ts.eclipse.ide.internal.core.resources;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdapterFactory;

import ts.eclipse.ide.core.resources.IIDETypeScriptProject;
import ts.eclipse.ide.core.resources.buildpath.ITypeScriptRootContainer;
import ts.eclipse.ide.core.utils.TypeScriptResourceUtil;

public class ResourceAdapterFactory implements IAdapterFactory {

	private static Class[] PROPERTIES = new Class[] { ITypeScriptRootContainer.class };

	public Class[] getAdapterList() {
		return PROPERTIES;
	}

	public Object getAdapter(Object element, Class key) {
		if (ITypeScriptRootContainer.class.equals(key)) {
			if (element instanceof IContainer) {
				IContainer container = (IContainer) element;
				try {
					IIDETypeScriptProject tsProject = TypeScriptResourceUtil
							.getTypeScriptProject(container.getProject());
					if (tsProject != null) {
						return tsProject.getTypeScriptBuildPath().getRootContainer(container);
					}
				} catch (CoreException e) {
					e.printStackTrace();
				}

			}
		}
		return null;
	}
}
