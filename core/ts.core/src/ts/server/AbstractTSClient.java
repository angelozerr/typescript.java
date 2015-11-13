package ts.server;

import java.util.concurrent.atomic.AtomicInteger;

import com.eclipsesource.json.JsonObject;

import ts.CompletionInfo;
import ts.NavigationBarItem;
import ts.TSException;
import ts.server.protocol.CompletionsRequest;
import ts.server.protocol.ISequenceProvider;
import ts.server.protocol.NavBarRequest;
import ts.server.protocol.OpenRequest;
import ts.server.protocol.Request;

public abstract class AbstractTSClient implements ITSClient, ISequenceProvider {

	private AtomicInteger requestCount = new AtomicInteger();

	@Override
	public void openFile(String fileName) throws TSException {
		Request request = new OpenRequest(fileName, this);
		processVoidRequest(request);
	}

	@Override
	public CompletionInfo getCompletionsAtPosition(String fileName, int position) throws TSException {
		// var lineOffset = this.positionToOneBasedLineOffset(fileName,
		// position);
		// TODO : implement that?
		int line = 0;
		int offset = 0;
		return getCompletionsAtLineOffset(fileName, line, offset);
	}

	@Override
	public CompletionInfo getCompletionsAtLineOffset(String fileName, int line, int offset) throws TSException {
		CompletionsRequest request = new CompletionsRequest(fileName, line, offset, null, this);
		JsonObject response = processRequest(request);
		return createCompletionInfo(response);
	}

	@Override
	public NavigationBarItem[] getNavigationBarItems(String fileName) throws TSException {
		NavBarRequest request = new NavBarRequest(fileName, this);
		JsonObject response = this.processRequest(request);
		return null;
	}

	@Override
	public int getSequence() {
		return requestCount.getAndIncrement();
	}

	protected abstract void processVoidRequest(Request request) throws TSException;

	protected abstract JsonObject processRequest(Request request) throws TSException;

	private CompletionInfo createCompletionInfo(JsonObject response) {
System.err.println(response);
		return null;
	}
}