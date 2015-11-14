package ts.server;

import java.util.concurrent.atomic.AtomicInteger;

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;

import ts.CompletionEntry;
import ts.CompletionInfo;
import ts.ICompletionEntry;
import ts.ICompletionInfo;
import ts.INavigationBarItem;
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
	public ICompletionInfo getCompletionsAtPosition(String fileName, int position) throws TSException {
		// var lineOffset = this.positionToOneBasedLineOffset(fileName,
		// position);
		// TODO : implement that?
		int line = 0;
		int offset = 0;
		return getCompletionsAtLineOffset(fileName, line, offset);
	}

	@Override
	public ICompletionInfo getCompletionsAtLineOffset(String fileName, int line, int offset) throws TSException {
		CompletionsRequest request = new CompletionsRequest(fileName, line, offset, null, this);
		JsonObject response = processRequest(request);
		return createCompletionInfo(response);
	}

	@Override
	public INavigationBarItem[] getNavigationBarItems(String fileName) throws TSException {
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

	private ICompletionInfo createCompletionInfo(JsonObject response) {
		JsonArray items = response.get("body").asArray();
		ICompletionEntry[] entries = new ICompletionEntry[items.size()];
		int i = 0;
		JsonObject obj = null;
		for (JsonValue item : items) {
			obj = (JsonObject) item;
			entries[i++] = new CompletionEntry(obj.getString("name", ""), obj.getString("kind", ""),
					obj.getString("kindModifiers", ""), obj.getString("sortText", ""));
		}
		CompletionInfo completion = new CompletionInfo(false, false, entries);
		return completion;
	}
}