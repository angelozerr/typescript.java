package ts.server.nodejs;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import ts.CompletionInfo;
import ts.NavigationBarItem;
import ts.TSException;
import ts.server.ITSClient;
import ts.server.nodejs.process.NodeJSProcess;
import ts.server.protocol.CompletionsRequest;
import ts.server.protocol.ISequenceProvider;
import ts.server.protocol.NavBarRequest;
import ts.server.protocol.OpenRequest;
import ts.server.protocol.Request;

/**
 * @see https://github.com/Microsoft/TypeScript/blob/master/src/server/client.ts
 *
 */
public class NodeJSTSClient implements ITSClient, ISequenceProvider {

	private NodeJSProcess process;
	private AtomicInteger requestCount = new AtomicInteger();

	public NodeJSTSClient(File projectDir, File tsserverFile, File nodeFile) {
		this.process = new NodeJSProcess(projectDir, tsserverFile, nodeFile);
		process.start();
	}

	@Override
	public void openFile(String fileName) throws TSException {
		Request request = new OpenRequest(fileName, this);
		processRequest(request);
	}

	@Override
	public CompletionInfo getCompletionsAtPosition(String fileName, int position) throws TSException {
		// var lineOffset = this.positionToOneBasedLineOffset(fileName,
		// position);
		// TODO : implement that?
		int line = 0;
		int offset = 0;
		return getCompletionsAtLineOfsset(fileName, line, offset);
	}

	@Override
	public CompletionInfo getCompletionsAtLineOfsset(String fileName, int line, int offset) throws TSException {
		CompletionsRequest request = new CompletionsRequest(fileName, line, offset, null, this);
		processRequest(request);
		return null;
	}

	@Override
	public NavigationBarItem[] getNavigationBarItems(String fileName) throws TSException {
		NavBarRequest request = new NavBarRequest(fileName, this);
		this.processRequest(request);
		// var response = this.processResponse<NavBarResponse>(request);
		return null;
	}

	protected void processRequest(Request request) throws TSException {
		try {
			System.out.println(request);
			process.writeMessage(request);
		} catch (IOException e) {
			throw new TSException(e);
		}
	}

	protected void processResponse(Request request) throws TSException {

	}

	public void join() throws InterruptedException {
		this.process.join();
	}

	@Override
	public int getSequence() {
		return requestCount.getAndIncrement();
	}
}
