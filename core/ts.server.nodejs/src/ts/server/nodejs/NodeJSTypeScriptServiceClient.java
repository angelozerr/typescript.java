package ts.server.nodejs;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.eclipsesource.json.JsonObject;

import ts.TSException;
import ts.server.AbstractTypeScriptServiceClient;
import ts.server.nodejs.internal.process.NodeJSProcess;
import ts.server.nodejs.process.INodejsProcess;
import ts.server.nodejs.process.INodejsProcessListener;
import ts.server.nodejs.process.NodejsProcessAdapter;
import ts.server.nodejs.process.NodejsProcessManager;
import ts.server.protocol.Request;

/**
 * @see https://github.com/Microsoft/TypeScript/blob/master/src/server/client.ts
 *
 */
public class NodeJSTypeScriptServiceClient extends AbstractTypeScriptServiceClient {

	private final File projectDir;
	private INodejsProcess process;
	private List<INodejsProcessListener> listeners;
	
	private final INodejsProcessListener listener = new NodejsProcessAdapter() {

		@Override
		public void onStart(INodejsProcess server) {
			NodeJSTypeScriptServiceClient.this.fireStartServer();
		}

		@Override
		public void onStop(INodejsProcess server) {
			dispose();
			fireEndServer();
		}

	};

	public NodeJSTypeScriptServiceClient(File projectDir, File tsserverFile, File nodeFile) throws TSException {
		this(projectDir, new NodeJSProcess(projectDir, tsserverFile, nodeFile));
	}

	public NodeJSTypeScriptServiceClient(File projectDir, INodejsProcess process) {
		this.projectDir = projectDir;
		this.process = process;
		process.addProcessListener(listener);
		initProcess(process);
	}

	private void initProcess(INodejsProcess process) {
		if (!process.isStarted()) {
			process.start();
		}
	}

	private INodejsProcess getProcess() throws TSException {
		if (process == null) {
			process = NodejsProcessManager.getInstance().create(getProjectDir());
			process.addProcessListener(listener);
		}
		initProcess(process);
		return process;
	}

	@Override
	protected void processVoidRequest(Request request) throws TSException {
		try {
			getProcess().sendRequest(request);
		} catch (Exception e) {
			if (e instanceof TSException) {
				throw (TSException) e;
			}
			if (e.getCause() instanceof TSException) {
				throw (TSException) e.getCause();
			}
			throw new TSException(e);
		}
	}

	@Override
	protected JsonObject processRequest(Request request) throws TSException {
		try {
			return getProcess().sendRequestSyncResponse(request);
		} catch (Exception e) {
			if (e instanceof TSException) {
				throw (TSException) e;
			}
			if (e.getCause() instanceof TSException) {
				throw (TSException) e.getCause();
			}
			throw new TSException(e);
		}
	}
	
	public void addProcessListener(INodejsProcessListener listener) {
		beginWriteState();
		try {
			if (listeners == null) {
				listeners = new ArrayList<INodejsProcessListener>();
			}
			listeners.add(listener);
			if (process != null) {
				process.addProcessListener(listener);
			}
		} finally {
			endWriteState();
		}
	}

	public void removeProcessListener(INodejsProcessListener listener) {
		beginWriteState();
		try {
			if (listeners != null && listener != null) {
				listeners.remove(listener);
			}
			if (process != null) {
				process.removeProcessListener(listener);
			}
		} finally {
			endWriteState();
		}
	}

	public void join() throws InterruptedException {
		if (process != null) {
			this.process.join();
		}
	}

	@Override
	public void doDispose() {
		beginWriteState();
		try {
			if (process != null) {
				process.kill();
			}
			this.process = null;
		} finally {
			endWriteState();
		}
		this.process.kill();
	}

	public File getProjectDir() {
		return projectDir;
	}
}
