package ts.server.nodejs;

import java.io.File;

import ts.server.ITypeScriptServiceClient;
import ts.server.ITypeScriptServiceClientFactory;

public class NodeJSTypeScriptServiceClientFactory implements ITypeScriptServiceClientFactory {

	private final File tsserverFile;
	private final File nodeFile;

	public NodeJSTypeScriptServiceClientFactory(File tsserverFile, File nodeFile) {
		this.tsserverFile = tsserverFile;
		this.nodeFile = nodeFile;
	}

	@Override
	public ITypeScriptServiceClient create(File projectDir) {
		return new NodeJSTypeScriptServiceClient(projectDir, tsserverFile, nodeFile);
	}

}
