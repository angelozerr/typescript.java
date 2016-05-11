package ts.eclipse.ide.internal.core.compiler;

import java.io.File;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.runtime.CoreException;

import ts.compiler.TypeScriptCompilerHelper;
import ts.eclipse.ide.core.compiler.IDETypeScriptCompilerMessageHandler;
import ts.nodejs.INodejsProcess;
import ts.nodejs.INodejsProcessListener;

public class IDETypeScriptCompilerReporter extends IDETypeScriptCompilerMessageHandler
		implements INodejsProcessListener {

	public IDETypeScriptCompilerReporter(IContainer container) throws CoreException {
		super(container);
	}

	@Override
	public void onCreate(INodejsProcess process, List<String> commands, File projectDir) {
	}

	@Override
	public void onStart(INodejsProcess process) {
	}

	@Override
	public void onMessage(INodejsProcess process, String response) {
		TypeScriptCompilerHelper.processMessage(response, this);
	}

	@Override
	public void onStop(INodejsProcess process) {
	}

	@Override
	public void onError(INodejsProcess process, String line) {
	}

}
