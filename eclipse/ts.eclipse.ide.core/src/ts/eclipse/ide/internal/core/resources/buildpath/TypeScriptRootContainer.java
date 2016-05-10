package ts.eclipse.ide.internal.core.resources.buildpath;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.runtime.CoreException;

import ts.eclipse.ide.core.resources.buildpath.ITypeScriptRootContainer;
import ts.eclipse.ide.core.resources.jsconfig.IDETsconfigJson;
import ts.eclipse.ide.core.utils.TypeScriptResourceUtil;

public class TypeScriptRootContainer extends TypeScriptContainer implements ITypeScriptRootContainer {

	public TypeScriptRootContainer(IContainer container) {
		super(container);
	}

	@Override
	public IDETsconfigJson getTsconfig() throws CoreException {
		return TypeScriptResourceUtil.findTsconfig(getContainer());
	}
}
