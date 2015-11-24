package ts.server;

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;

import ts.ICompletionEntry;
import ts.ICompletionInfo;
import ts.TSException;
import ts.internal.CompletionEntry;
import ts.internal.CompletionInfo;
import ts.internal.FileTempHelper;
import ts.internal.SequenceHelper;
import ts.server.completions.ITypeScriptCompletionCollector;
import ts.server.definition.ITypeScriptDefinitionCollector;
import ts.server.protocol.ChangeRequest;
import ts.server.protocol.CloseRequest;
import ts.server.protocol.CompletionsRequest;
import ts.server.protocol.DefinitionRequest;
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

	// ---------------- Completions

	@Override
	public void completions(String fileName, int line, int offset, String prefix, ITypeScriptCompletionCollector collector)
			throws TSException {
		CompletionsRequest request = new CompletionsRequest(fileName, line, offset, prefix);
		JsonObject response = processRequest(request);
		collectCompletions(response, collector);
	}

	private void collectCompletions(JsonObject response, ITypeScriptCompletionCollector collector) {
		JsonArray items = response.get("body").asArray();
		JsonObject obj = null;
		for (JsonValue item : items) {
			obj = (JsonObject) item;
			collector.addCompletionEntry(obj.getString("name", ""), obj.getString("kind", ""),
					obj.getString("kindModifiers", ""), obj.getString("sortText", ""));
		}
	}

	// ---------------- Definition

	@Override
	public void definition(String fileName, int line, int offset, ITypeScriptDefinitionCollector collector) throws TSException {
		DefinitionRequest request = new DefinitionRequest(fileName, line, offset);
		JsonObject response = processRequest(request);
		collectDefinition(response, collector);
	}
	
	private void collectDefinition(JsonObject response, ITypeScriptDefinitionCollector collector) throws TSException {
		JsonArray items = response.get("body").asArray();
		JsonObject def = null;
		JsonObject start = null;
		JsonObject end = null;
		for (JsonValue item : items) {
			def = (JsonObject) item;
			start = def.get("start").asObject();
			end = def.get("end").asObject();
			collector.addDefinition(def.getString("file", null), start.getInt("line", -1),
					start.getInt("offset", -1), end.getInt("line", -1),
					end.getInt("offset", -1));
		}	}

	@Override
	public void changeFile(String fileName, int line, int offset, int endLine, int endOffset, String newText)
			throws TSException {
		Request request = new ChangeRequest(fileName, line, offset, endLine, endOffset, newText);
		processVoidRequest(request);
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

}