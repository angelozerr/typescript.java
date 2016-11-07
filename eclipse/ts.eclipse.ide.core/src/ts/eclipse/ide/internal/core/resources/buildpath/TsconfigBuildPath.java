package ts.eclipse.ide.internal.core.resources.buildpath;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceProxy;
import org.eclipse.core.resources.IResourceProxyVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;

import ts.eclipse.ide.core.resources.buildpath.ITsconfigBuildPath;
import ts.eclipse.ide.core.resources.jsconfig.IDETsconfigJson;
import ts.eclipse.ide.core.utils.TypeScriptResourceUtil;

public class TsconfigBuildPath implements ITsconfigBuildPath, IResourceProxyVisitor, IAdaptable {

	private final IFile tsconfigFile;
	private IDETsconfigJson tsconfig;
	private List<Object> members;

	public TsconfigBuildPath(IFile tsconfigFile) {
		this.tsconfigFile = tsconfigFile;
		this.members = new ArrayList<Object>();
	}

	@Override
	public IFile getTsconfigFile() {
		return tsconfigFile;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof ITsconfigBuildPath) {
			return ((ITsconfigBuildPath) obj).getTsconfigFile().equals(this.getTsconfigFile());
		} else if (obj instanceof IFile) {
			return obj.equals(this.getTsconfigFile());
		}
		return false;
	}

	@Override
	public Object[] members() {
		try {
			tsconfig = TypeScriptResourceUtil.getTsconfig(tsconfigFile);
			members.clear();
			tsconfigFile.getParent().accept(this, IResource.NONE);
			return members.toArray();
		} catch (CoreException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public boolean visit(IResourceProxy proxy) throws CoreException {
		IResource resource = proxy.requestResource();
		if (tsconfigFile.equals(resource)) {
			return true;
		}
		int type = proxy.getType();
		if (type == IResource.FILE && !(TypeScriptResourceUtil.isTsOrTsxFile(resource)
				|| TypeScriptResourceUtil.isJsOrJsMapFile(resource))) {
			return false;
		}
		if (tsconfig.isInScope(resource)) {
			if (type == IResource.PROJECT || type == IResource.FOLDER) {
				return true;
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
		// ts.eclipse.ide.internal.core.resources.buildpath.TsconfigBuildPath
		// when Run shortcut si opened.
		// to avoid this error, type="org.eclipse.core.runtime.IAdaptable"
		/*
		 * <propertyTester namespace="org.eclipse.debug.ui"
		 * properties="matchesPattern, projectNature, matchesContentType"
		 * type="org.eclipse.core.runtime.IAdaptable"
		 * class="org.eclipse.debug.internal.ui.ResourceExtender"
		 * id="org.eclipse.debug.ui.IResourceExtender">
		 */
		return null;
	}

	@Override
	public IDETsconfigJson getTsconfig() throws CoreException {
		return TypeScriptResourceUtil.getTsconfig(tsconfigFile);
	}

}
