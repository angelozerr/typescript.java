package ts.eclipse.ide.core.resources.buildpath;

import org.eclipse.core.runtime.CoreException;

import ts.eclipse.ide.core.resources.jsconfig.IDETsconfigJson;

public interface ITypeScriptRootContainer extends ITypeScriptContainer {

	IDETsconfigJson getTsconfig() throws CoreException;
}
