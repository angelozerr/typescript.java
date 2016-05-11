package ts.eclipse.ide.internal.core.launch;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.IStreamListener;
import org.eclipse.debug.core.model.IStreamMonitor;

import ts.compiler.TypeScriptCompilerHelper;
import ts.eclipse.ide.core.compiler.IDETypeScriptCompilerMessageHandler;
import ts.resources.jsonconfig.TsconfigJson;

public class TscStreamListener extends IDETypeScriptCompilerMessageHandler implements IStreamListener {

	public TscStreamListener(IContainer container) throws CoreException {
		super(container);
	}

	@Override
	public void streamAppended(String text, IStreamMonitor monitor) {
		TypeScriptCompilerHelper.processMessage(text, this);
	}

	public boolean isWatch() {
		TsconfigJson tsconfig = super.getTsconfig();
		return tsconfig != null && tsconfig.getCompilerOptions() != null && tsconfig.getCompilerOptions().isWatch();
	}

}
