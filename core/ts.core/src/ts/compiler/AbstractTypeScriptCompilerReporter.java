package ts.compiler;

import ts.nodejs.INodejsProcess;
import ts.nodejs.NodejsProcessAdapter;
import ts.utils.FileUtils;

public abstract class AbstractTypeScriptCompilerReporter extends NodejsProcessAdapter {

	@Override
	public void onMessage(INodejsProcess process, String message) {
		System.out.println(message);
		if (message.endsWith(FileUtils.TS_EXTENSION) || message.endsWith(FileUtils.TSX_EXTENSION)) {
			addFile(message);
		} else if (message.contains("error")) {

		}		
	}

	protected abstract void addFile(String file);
}
