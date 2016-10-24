package ts.eclipse.ide.internal.core.resources;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdapterFactory;

import ts.eclipse.ide.core.resources.IIDETypeScriptProject;
import ts.eclipse.ide.core.resources.buildpath.ITsconfigBuildPath;
import ts.eclipse.ide.core.utils.TypeScriptResourceUtil;

public class ResourceAdapterFactory implements IAdapterFactory {

	private static Class[] PROPERTIES = new Class[] { ITsconfigBuildPath.class };

	public Class[] getAdapterList() {
		return PROPERTIES;
	}

	public Object getAdapter(Object element, Class key) {
		if (ITsconfigBuildPath.class.equals(key)) {
			if (element instanceof IFile) {
				IFile file = (IFile) element;
				try {
					IIDETypeScriptProject tsProject = TypeScriptResourceUtil.getTypeScriptProject(file.getProject());
					if (tsProject != null) {
						return tsProject.getTypeScriptBuildPath().getTsconfigBuildPath(file);
					}
				} catch (CoreException e) {
					e.printStackTrace();
				}

			}
		}
		return null;
	}
}
