package ts.eclipse.ide.core.resources.buildpath;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;

import ts.eclipse.ide.core.resources.jsconfig.IDETsconfigJson;

public interface ITsconfigBuildPath {

	IFile getTsconfigFile();

	IDETsconfigJson getTsconfig() throws CoreException;

	Object[] members();
}
