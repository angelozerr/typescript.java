package ts.compiler;

import java.io.File;
import java.util.List;

import ts.TypeScriptException;
import ts.client.protocol.Message;
import ts.nodejs.INodejsLaunchConfiguration;
import ts.nodejs.INodejsProcess;
import ts.nodejs.INodejsProcessListener;
import ts.nodejs.NodejsProcessAdapter;
import ts.nodejs.NodejsProcessManager;

public class TypeScriptCompiler implements ITypeScriptCompiler {

	private final File projectDir;
	private final File tscFile;
	private final File nodejsFile;

	public TypeScriptCompiler(File projectDir, File tscFile, File nodejsFile) {
		this.projectDir = projectDir;
		this.tscFile = tscFile;
		this.nodejsFile = nodejsFile;
	}

	@Override
	public void compile(String filename) throws TypeScriptException {
		INodejsProcess process = NodejsProcessManager.getInstance().create(projectDir, tscFile, nodejsFile, new INodejsLaunchConfiguration() {

			@Override
			public List<String> createNodeArgs() {
				// TODO Auto-generated method stub
				return null;
			}
		});
		
		process.addProcessListener(new NodejsProcessAdapter() {
			@Override
			public void onStart(INodejsProcess process) {
			System.out.println("starts tsc");
			}
			
			@Override
			public void onStop(INodejsProcess process) {
				System.out.println("end tsc");
			}
			
			@Override
			public void onError(INodejsProcess process, String line) {
				System.err.println(line);
			}
			
			@Override
			public void onMessage(INodejsProcess process, String response) {
				System.out.println(response);
			}
		});
		process.start();
	}

}
