package ts.eclipse.ide.internal.core.resources.buildpath;

import org.eclipse.core.resources.IContainer;

import ts.eclipse.ide.core.resources.buildpath.ITypeScriptRootContainer;

public class TypeScriptRootContainer extends TypeScriptContainer implements ITypeScriptRootContainer {

	public TypeScriptRootContainer(IContainer container) {
		super(container);
	}

}
