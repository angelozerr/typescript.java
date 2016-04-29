package ts.eclipse.ide.internal.core.resources.buildpath;

import org.eclipse.core.resources.IContainer;

import ts.eclipse.ide.core.resources.buildpath.ITypeScriptRootContainer;

public class TypeScriptRootContainer implements ITypeScriptRootContainer {

	private final IContainer container;

	public TypeScriptRootContainer(IContainer container) {
		this.container = container;
	}

	@Override
	public IContainer getContainer() {
		return container;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof ITypeScriptRootContainer) {
			return ((ITypeScriptRootContainer) obj).getContainer().equals(this.getContainer());
		} else if (obj instanceof IContainer) {
			return obj.equals(this.getContainer());
		}
		return false;
	}
}
