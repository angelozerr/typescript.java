package ts.server.nodejs;

import java.io.File;

import com.eclipsesource.json.JsonObject;

import ts.TSException;
import ts.server.AbstractTSClient;
import ts.server.nodejs.process.NodeJSProcess;
import ts.server.protocol.Request;

/**
 * @see https://github.com/Microsoft/TypeScript/blob/master/src/server/client.ts
 *
 */
public class NodeJSTSClient extends AbstractTSClient {

	private final NodeJSProcess process;

	public NodeJSTSClient(File projectDir, File tsserverFile, File nodeFile) {
		this.process = new NodeJSProcess(projectDir, tsserverFile, nodeFile);
		process.start();
	}

	@Override
	protected void processVoidRequest(Request request) throws TSException {
		try {
			System.out.println(request);
			process.sendRequest(request);
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
			System.out.println(request);
			return process.sendRequestSyncResponse(request);
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

	protected void processResponse(Request request) throws TSException {

	}

	public void join() throws InterruptedException {
		this.process.join();
	}

	@Override
	public void dispose() {
		this.process.kill();
	}

}
