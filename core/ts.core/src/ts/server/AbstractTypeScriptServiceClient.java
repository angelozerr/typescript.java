package ts.server;

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;

import ts.CompletionEntry;
import ts.CompletionInfo;
import ts.ICompletionCollector;
import ts.ICompletionEntry;
import ts.ICompletionInfo;
import ts.INavigationBarItem;
import ts.TSException;
import ts.internal.FileTempHelper;
import ts.internal.SequenceHelper;
import ts.server.protocol.ChangeRequest;
import ts.server.protocol.CloseRequest;
import ts.server.protocol.CompletionsRequest;
import ts.server.protocol.NavBarRequest;
import ts.server.protocol.OpenRequest;
import ts.server.protocol.ReloadRequest;
import ts.server.protocol.Request;

public abstract class AbstractTypeScriptServiceClient implements ITypeScriptServiceClient {

	public AbstractTypeScriptServiceClient() {
	}

	@Override
	public void openFile(String fileName) throws TSException {
		Request request = new OpenRequest(fileName);
		processVoidRequest(request);
	}
	
	@Override
	public void closeFile(String fileName) throws TSException {
		Request request = new CloseRequest(fileName);
		processVoidRequest(request);		
	}

	@Override
	public void completions(String fileName, int line, int offset, String prefix, ICompletionCollector collector) throws TSException {
		CompletionsRequest request = new CompletionsRequest(fileName, line, offset, prefix);
		JsonObject response = processRequest(request);
		collectCompletions(response, collector);
		
	}
	
	private void collectCompletions(JsonObject response, ICompletionCollector collector) {
		JsonArray items = response.get("body").asArray();
		JsonObject obj = null;
		for (JsonValue item : items) {
			obj = (JsonObject) item;
			collector.addCompletionEntry(obj.getString("name", ""), obj.getString("kind", ""),
					obj.getString("kindModifiers", ""), obj.getString("sortText", ""));
		}
	}

	@Override
	public void changeFile(String fileName, int start, int end, String newText) throws TSException {
		// TODO : implement that?
		int line = 0;
		int offset = 0;
		int endLine = 0;
		int endOffset = 0;
		changeFile(fileName, line, offset, endLine, endOffset, newText);
	}

	@Override
	public void changeFile(String fileName, int line, int offset, int endLine, int endOffset, String newText)
			throws TSException {
		Request request = new ChangeRequest(fileName, line, offset, endLine, endOffset, newText);
		processVoidRequest(request);
	}

	@Override
	public ICompletionInfo getCompletionsAtPosition(String fileName, int position, String prefix) throws TSException {
		// var lineOffset = this.positionToOneBasedLineOffset(fileName,
		// position);
		// TODO : implement that?
		int line = 0;
		int offset = 0;
		return getCompletionsAtLineOffset(fileName, line, offset, prefix);
	}

	@Override
	public ICompletionInfo getCompletionsAtLineOffset(String fileName, int line, int offset, String prefix)
			throws TSException {
		CompletionsRequest request = new CompletionsRequest(fileName, line, offset, prefix);
		JsonObject response = processRequest(request);
		return createCompletionInfo(response);
	}

	@Override
	public INavigationBarItem[] getNavigationBarItems(String fileName) throws TSException {
		NavBarRequest request = new NavBarRequest(fileName);
		JsonObject response = this.processRequest(request);
		return null;
	}

	/**
	 * Write the buffer of editor content to a temporary file and have the
	 * server reload it
	 * 
	 * @param fileName
	 * @param newText
	 */
	@Override
	public void updateFile(String fileName, String newText) throws TSException {
		int seq = SequenceHelper.getRequestSeq();
		String tempFileName = null;
		int requestSeq = -1;
		try {
			tempFileName = FileTempHelper.updateTempFile(newText, seq);
			Request request = new ReloadRequest(fileName, tempFileName, seq);
			JsonObject response = this.processRequest(request);
			requestSeq = response.getInt("request_seq", -1);
		} finally {
			if (requestSeq != -1) {
				FileTempHelper.freeTempFile(requestSeq);
			}
		}

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