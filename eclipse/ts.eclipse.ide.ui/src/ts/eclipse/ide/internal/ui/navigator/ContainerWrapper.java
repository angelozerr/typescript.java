package ts.eclipse.ide.internal.ui.navigator;

import org.eclipse.core.resources.IContainer;

public class ContainerWrapper {

	private final IContainer container;

	public ContainerWrapper(IContainer container) {
		this.container = container;
	}

	public IContainer getContainer() {
		return container;
	}
}
