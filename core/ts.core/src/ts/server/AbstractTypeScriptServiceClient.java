package ts.server;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;

import ts.TSException;
import ts.internal.FileTempHelper;
import ts.internal.SequenceHelper;
import ts.server.completions.ITypeScriptCompletionCollector;
import ts.server.definition.ITypeScriptDefinitionCollector;
import ts.server.protocol.ChangeRequest;
import ts.server.protocol.CloseRequest;
import ts.server.protocol.CompletionsRequest;
import ts.server.protocol.DefinitionRequest;
import ts.server.protocol.OpenRequest;
import ts.server.protocol.QuickInfoRequest;
import ts.server.protocol.ReloadRequest;
import ts.server.protocol.Request;
import ts.server.protocol.SignatureHelpRequest;
import ts.server.quickinfo.ITypeScriptQuickInfoCollector;
import ts.server.signaturehelp.ITypeScriptSignatureHelpCollector;

public abstract class AbstractTypeScriptServiceClient implements ITypeScriptServiceClient {

	private boolean dispose;
	private final List<ITypeScriptServerListener> listeners;
	private ReentrantReadWriteLock stateLock = new ReentrantReadWriteLock();
	private List<IInterceptor> interceptors;

	public AbstractTypeScriptServiceClient() {
		this.listeners = new ArrayList<ITypeScriptServerListener>();
	}

	@Override
	public void openFile(String fileName) throws TSException {
		Request request = new OpenRequest(fileName);
		internalProcessVoidRequest(request);
	}

	@Override
	public void closeFile(String fileName) throws TSException {
		Request request = new CloseRequest(fileName);
		internalProcessVoidRequest(request);
	}

	// ---------------- Completions

	@Override
	public void completions(String fileName, int line, int offset, String prefix,
			ITypeScriptCompletionCollector collector) throws TSException {
		CompletionsRequest request = new CompletionsRequest(fileName, line, offset, prefix);
		JsonObject response = internalProcessRequest(request);
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
	public void definition(String fileName, int line, int offset, ITypeScriptDefinitionCollector collector)
			throws TSException {
		DefinitionRequest request = new DefinitionRequest(fileName, line, offset);
		JsonObject response = internalProcessRequest(request);
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
			collector.addDefinition(def.getString("file", null), start.getInt("line", -1), start.getInt("offset", -1),
					end.getInt("line", -1), end.getInt("offset", -1));
		}
	}

	// ---------------- Signature Help

	@Override
	public void signatureHelp(String fileName, int line, int offset, ITypeScriptSignatureHelpCollector collector)
			throws TSException {
		SignatureHelpRequest request = new SignatureHelpRequest(fileName, line, offset);
		JsonObject response = internalProcessRequest(request);
		collectSignatureHelp(response, collector);
	}

	private void collectSignatureHelp(JsonObject response, ITypeScriptSignatureHelpCollector collector) {
		// TODO Auto-generated method stub

	}

	// ---------------- Signature Help

	@Override
	public void quickInfo(String fileName, int line, int offset, ITypeScriptQuickInfoCollector collector)
			throws TSException {
		QuickInfoRequest request = new QuickInfoRequest(fileName, line, offset);
		JsonObject response = internalProcessRequest(request);
		collectQuickInfo(response, collector);
	}

	private void collectQuickInfo(JsonObject response, ITypeScriptQuickInfoCollector collector) {
		JsonObject body = response.get("body").asObject();
		if (body != null) {
			String kind = body.getString("kind", null);
			String kindModifiers = body.getString("kindModifiers", null);
			JsonObject start = body.get("start").asObject();
			JsonObject end = body.get("end").asObject();
			String displayString = body.getString("displayString", null);
			String documentation = body.getString("documentation", null);
			collector.setInfo(kind, kindModifiers, start.getInt("line", -1), start.getInt("offset", -1),
					end.getInt("line", -1), end.getInt("offset", -1), displayString, documentation);
		}
	}

	@Override
	public void changeFile(String fileName, int line, int offset, int endLine, int endOffset, String newText)
			throws TSException {
		Request request = new ChangeRequest(fileName, line, offset, endLine, endOffset, newText);
		internalProcessVoidRequest(request);
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
			JsonObject response = this.internalProcessRequest(request);
			requestSeq = response.getInt("request_seq", -1);
		} finally {
			if (requestSeq != -1) {
				FileTempHelper.freeTempFile(requestSeq);
			}
		}

	}

	@Override
	public void addServerListener(ITypeScriptServerListener listener) {
		synchronized (listeners) {
			listeners.add(listener);
		}
	}

	@Override
	public void removeServerListener(ITypeScriptServerListener listener) {
		synchronized (listeners) {
			listeners.remove(listener);
		}
	}

	protected void fireStartServer() {
		synchronized (listeners) {
			for (ITypeScriptServerListener listener : listeners) {
				listener.onStart(this);
			}
		}
	}

	protected void fireEndServer() {
		synchronized (listeners) {
			for (ITypeScriptServerListener listener : listeners) {
				listener.onStop(this);
			}
		}
	}

	public void addInterceptor(IInterceptor interceptor) {
		beginWriteState();
		try {
			if (interceptors == null) {
				interceptors = new ArrayList<IInterceptor>();
			}
			interceptors.add(interceptor);
		} finally {
			endWriteState();
		}
	}

	public void removeInterceptor(IInterceptor interceptor) {
		beginWriteState();
		try {
			if (interceptors != null) {
				interceptors.remove(interceptor);
			}
		} finally {
			endWriteState();
		}
	}

	protected void beginReadState() {
		stateLock.readLock().lock();
	}

	protected void endReadState() {
		stateLock.readLock().unlock();
	}

	protected void beginWriteState() {
		stateLock.writeLock().lock();
	}

	protected void endWriteState() {
		stateLock.writeLock().unlock();
	}

	@Override
	public final void dispose() {
		beginWriteState();
		try {
			if (!isDisposed()) {
				this.dispose = true;
				doDispose();
			}
		} finally {
			endWriteState();
		}
	}

	@Override
	public boolean isDisposed() {
		return dispose;
	}

	private void internalProcessVoidRequest(Request request) throws TSException {
		if (interceptors == null) {
			processVoidRequest(request);
		} else {
			long startTime = System.nanoTime();
			try {
				handleRequest(request);
				processVoidRequest(request);
			} catch (Throwable e) {
				handleError(request, e, startTime);
				if (e instanceof TSException) {
					throw (TSException) e;
				}
				throw new TSException(e);
			}
		}
	}

	private JsonObject internalProcessRequest(Request request) throws TSException {
		if (interceptors == null) {
			return processRequest(request);
		} else {
			long startTime = System.nanoTime();
			try {
				handleRequest(request);
				JsonObject response = processRequest(request);
				handleResponse(request, response, startTime);
				return response;
			} catch (Throwable e) {
				handleError(request, e, startTime);
				if (e instanceof TSException) {
					throw (TSException) e;
				}
				throw new TSException(e);
			}
		}
	}

	private void handleRequest(Request request) {
		for (IInterceptor interceptor : interceptors) {
			interceptor.handleRequest(request, this, request.getCommand());
		}
	}

	private void handleResponse(Request request, JsonObject response, long startTime) {
		long ellapsedTime = getElapsedTimeInMs(startTime);
		for (IInterceptor interceptor : interceptors) {
			interceptor.handleResponse(response, this, request.getCommand(), ellapsedTime);
		}
	}

	private void handleError(Request request, Throwable e, long startTime) {
		long ellapsedTime = getElapsedTimeInMs(startTime);
		for (IInterceptor interceptor : interceptors) {
			interceptor.handleError(e, this, request.getCommand(), ellapsedTime);
		}
	}

	private static long getElapsedTimeInMs(long startTime) {
		return ((System.nanoTime() - startTime) / 1000000L);
	}

	protected abstract void processVoidRequest(Request request) throws TSException;

	protected abstract JsonObject processRequest(Request request) throws TSException;

	protected abstract void doDispose();
}