package ts.eclipse.ide.core.builder;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

import ts.compiler.AbstractTypeScriptCompilerReporter;
import ts.nodejs.INodejsProcess;

public class IDETypeScriptCompilerReporter extends AbstractTypeScriptCompilerReporter {

	private final IContainer container;
	private final List<IFile> filesToRefresh;

	public IDETypeScriptCompilerReporter(IContainer container) {
		this.container = container;
		this.filesToRefresh = new ArrayList<IFile>();
	}

	@Override
	public void onStart(INodejsProcess process) {
		System.out.println("start tsc");
		super.onStart(process);
	}

	@Override
	public void onStop(INodejsProcess process) {
		System.out.println("stop tsc");
		super.onStop(process);
	}

	@Override
	protected void addFile(String file) {
		IPath path = new Path(file);
		if (container.exists(path)) {
			filesToRefresh.add(container.getFile(path));
		}
	}

	public List<IFile> getFilesToRefresh() {
		return filesToRefresh;
	}

}
