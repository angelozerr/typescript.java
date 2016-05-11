package ts.eclipse.ide.core.compiler;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

import ts.compiler.ITypeScriptCompilerMessageHandler;
import ts.eclipse.ide.core.resources.jsconfig.IDETsconfigJson;
import ts.eclipse.ide.core.utils.TypeScriptResourceUtil;
import ts.eclipse.ide.internal.core.Trace;

public class IDETypeScriptCompilerMessageHandler implements ITypeScriptCompilerMessageHandler {

	private final IContainer container;
	private final IDETsconfigJson tsconfig;
	private final List<IFile> filesToRefresh;

	public IDETypeScriptCompilerMessageHandler(IContainer container) throws CoreException {
		this.container = container;
		this.tsconfig = TypeScriptResourceUtil.findTsconfig(container);
		this.filesToRefresh = new ArrayList<IFile>();
	}

	@Override
	public void addFile(String filePath) {
		IPath path = new Path(filePath);
		if (container.exists(path)) {
			IFile file = container.getFile(path);
			if (!filesToRefresh.contains(file)) {
				filesToRefresh.add(file);
			}
		}
	}

	public List<IFile> getFilesToRefresh() {
		return filesToRefresh;
	}

	@Override
	public void refreshFiles() {
		for (IFile tsFile : getFilesToRefresh()) {
			try {
				TypeScriptResourceUtil.refreshAndCollectEmittedFiles(tsFile, tsconfig, true, null);
			} catch (CoreException e) {
				Trace.trace(Trace.SEVERE, "Error while tsc compilation when ts file is refreshed", e);
			}
		}
	}
	
	public IDETsconfigJson getTsconfig() {
		return tsconfig;
	}

}
