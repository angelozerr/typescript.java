package ts.eclipse.ide.core.resources.jsconfig;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceProxy;
import org.eclipse.core.resources.IResourceProxyVisitor;
import org.eclipse.core.runtime.CoreException;

import ts.utils.FileUtils;

public abstract class AbstractTsconfigJsonCollector implements IResourceProxyVisitor {

	@Override
	public boolean visit(IResourceProxy proxy) throws CoreException {
		if (FileUtils.TSCONFIG_JSON.equals(proxy.getName())) {
			collect(proxy.requestResource());
		}
		return true;
	}

	protected abstract void collect(IResource tsconfigFile);

}
