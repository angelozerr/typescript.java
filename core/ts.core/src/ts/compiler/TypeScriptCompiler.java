package ts.compiler;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import ts.TypeScriptException;
import ts.nodejs.INodejsLaunchConfiguration;
import ts.nodejs.INodejsProcess;
import ts.nodejs.INodejsProcessListener;
import ts.nodejs.NodejsProcessManager;

public class TypeScriptCompiler implements ITypeScriptCompiler {

	private static final String TSC_FILE_TYPE = "tsc";
	private final File tscFile;
	private final File nodejsFile;

	public TypeScriptCompiler(File tscFile, File nodejsFile) {
		this.tscFile = tscFile;
		this.nodejsFile = nodejsFile;
	}

	@Override
	public void compile(File baseDir, INodejsProcessListener listener) throws TypeScriptException {
		INodejsProcess process = NodejsProcessManager.getInstance().create(baseDir, tscFile, nodejsFile,
				new INodejsLaunchConfiguration() {

					@Override
					public List<String> createNodeArgs() {
						List<String> args = new ArrayList<String>();
						args.add("--listFiles");
						// args.add("--watch");
						return args;
					}
				}, TSC_FILE_TYPE);

		process.addProcessListener(listener);
		process.start();
		try {
			process.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}
}
