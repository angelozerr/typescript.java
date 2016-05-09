package ts.eclipse.ide.core.resources.buildpath;

import org.eclipse.core.resources.IResource;

import ts.eclipse.ide.core.resources.IContainerProvider;

public interface ITypeScriptContainer extends IContainerProvider {

	Object[] members();
}
