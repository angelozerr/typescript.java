package ts.eclipse.ide.internal.core.resources.buildpath;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceProxy;
import org.eclipse.core.resources.IResourceProxyVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;

import ts.eclipse.ide.core.resources.buildpath.ITypeScriptContainer;
import ts.eclipse.ide.core.resources.buildpath.ITypeScriptRootContainer;
import ts.eclipse.ide.core.resources.jsconfig.IDETsconfigJson;
import ts.eclipse.ide.core.utils.TypeScriptResourceUtil;

public class TypeScriptContainer implements ITypeScriptContainer, IResourceProxyVisitor, IAdaptable {

	private final IContainer container;
	private IDETsconfigJson tsconfig;
	private List<Object> members;

	public TypeScriptContainer(IContainer container) {
		this.container = container;
		this.members = new ArrayList<Object>();
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

	@Override
	public Object[] members() {
		try {
			tsconfig = TypeScriptResourceUtil.findTsconfig(container);
			members.clear();
			container.accept(this, IResource.NONE);
			return members.toArray();
		} catch (CoreException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public boolean visit(IResourceProxy proxy) throws CoreException {
		IResource resource = proxy.requestResource();
		if (container.equals(resource)) {
			return true;
		}
		int type = proxy.getType();
		if (type == IResource.FILE && !(TypeScriptResourceUtil.isTsOrTsxFile(resource)
				|| TypeScriptResourceUtil.isJsOrJsMapFile(resource))) {
			return false;
		}
		if (tsconfig.isInScope(resource)) {
			if (type == IResource.FOLDER) {
				members.add(new TypeScriptContainer((IContainer) resource));
				return false;
			} else {
				members.add(resource);
			}
			return true;
		}
		return false;
	}

	// @Override
	public <T> T getAdapter(Class<T> clazz) {
		// Implement IAdaptable to avoid having error
		// No property tester contributes a property
		// org.eclipse.debug.ui.matchesContentType to type class
		// ts.eclipse.ide.internal.core.resources.buildpath.TypeScriptRootContainer
		// when Run shortcut si opened.
		// to avoid this error, type="org.eclipse.core.runtime.IAdaptable"
		/* <propertyTester
  		namespace="org.eclipse.debug.ui"
        properties="matchesPattern, projectNature, matchesContentType"
        type="org.eclipse.core.runtime.IAdaptable"
        class="org.eclipse.debug.internal.ui.ResourceExtender"
        id="org.eclipse.debug.ui.IResourceExtender">
        */
		return null;
	}

}
