package ts.server;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;

import ts.TSException;
import ts.internal.FileTempHelper;
import ts.internal.SequenceHelper;
import ts.internal.server.ICallbackItem;
import ts.internal.server.RequestItem;
import ts.server.completions.ITypeScriptCompletionCollector;
import ts.server.definition.ITypeScriptDefinitionCollector;
import ts.server.geterr.ITypeScriptGeterrCollector;
import ts.server.nodejs.INodejsProcess;
import ts.server.nodejs.INodejsProcessListener;
import ts.server.nodejs.NodejsProcessAdapter;
import ts.server.nodejs.NodejsProcessManager;
import ts.server.protocol.ChangeRequest;
import ts.server.protocol.CloseRequest;
import ts.server.protocol.CompletionsRequest;
import ts.server.protocol.DefinitionRequest;
import ts.server.protocol.GeterrRequest;
import ts.server.protocol.OpenRequest;
import ts.server.protocol.QuickInfoRequest;
import ts.server.protocol.ReloadRequest;
import ts.server.protocol.Request;
import ts.server.protocol.SignatureHelpRequest;
import ts.server.quickinfo.ITypeScriptQuickInfoCollector;
import ts.server.signaturehelp.ITypeScriptSignatureHelpCollector;

public class TypeScriptServiceClient implements ITypeScriptServiceClient {

	private boolean dispose;
	private final List<ITypeScriptServerListener> listeners;
	private ReentrantReadWriteLock stateLock = new ReentrantReadWriteLock();
	private List<IInterceptor> interceptors;

	private final ExecutorService pool = Executors.newFixedThreadPool(2);
	private final List<RequestItem> requestQueue;
	private final AtomicInteger pendingResponses;
	private final Map<Integer, ICallbackItem> callbacks;
	private final Map<String, ICallbackItem> diagCallbacks;

	private final File projectDir;
	private INodejsProcess process;
	private List<INodejsProcessListener> nodeListeners;

	private final INodejsProcessListener listener = new NodejsProcessAdapter() {

		@Override
		public void onStart(INodejsProcess process) {
			TypeScriptServiceClient.this.fireStartServer();
		}

		@Override
		public void onStop(INodejsProcess process) {
			dispose();
			fireEndServer();
		}

		public void onMessage(INodejsProcess process, String message) {
			JsonObject response = Json.parse(message).asObject();
			TypeScriptServiceClient.this.dispatchMessage(response);
		};

	};

	public TypeScriptServiceClient(File projectDir, File tsserverFile, File nodeFile) throws TSException {
		this(projectDir, NodejsProcessManager.getInstance().create(projectDir, tsserverFile, nodeFile));
	}

	public TypeScriptServiceClient(File projectDir, INodejsProcess process) {
		this.listeners = new ArrayList<ITypeScriptServerListener>();
		this.requestQueue = new ArrayList<RequestItem>();
		this.pendingResponses = new AtomicInteger(0);
		this.callbacks = new HashMap<Integer, ICallbackItem>();
		this.diagCallbacks = new HashMap<String, ICallbackItem>();

		this.projectDir = projectDir;
		this.process = process;
		process.addProcessListener(listener);
		initProcess(process);
	}

	public File getProjectDir() {
		return projectDir;
	}

	private void initProcess(INodejsProcess process) {

	}

	private INodejsProcess getProcess() throws TSException {
		if (process == null) {
			process = NodejsProcessManager.getInstance().create(getProjectDir());
			process.addProcessListener(listener);
		}
		initProcess(process);
		if (!process.isStarted()) {
			process.start();
		}
		return process;
	}

	@Override
	public void openFile(String fileName, String contents) throws TSException {
		Request request = new OpenRequest(fileName, contents);
		execute(request, false, null);
	}

	@Override
	public void closeFile(String fileName) throws TSException {
		Request request = new CloseRequest(fileName);
		execute(request, false, null);
	}

	// ---------------- Completions

	@Override
	public void completions(String fileName, int line, int offset, String prefix,
			ITypeScriptCompletionCollector collector) throws TSException {
		CompletionsRequest request = new CompletionsRequest(fileName, line, offset, prefix);
		JsonObject response = execute(request, true, null).asObject();
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
		JsonObject response = execute(request, true, null).asObject();
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
		JsonObject response = execute(request, true, null).asObject();
		collectSignatureHelp(response, collector);
	}

	private void collectSignatureHelp(JsonObject response, ITypeScriptSignatureHelpCollector collector) {
		// TODO Auto-generated method stub

	}

	// ---------------- QuickInfo

	@Override
	public void quickInfo(String fileName, int line, int offset, ITypeScriptQuickInfoCollector collector)
			throws TSException {
		QuickInfoRequest request = new QuickInfoRequest(fileName, line, offset);
		JsonObject response = execute(request, true, null).asObject();
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
		execute(request, false, null);
	}

	@Override
	public void geterr(String[] files, int delay, ITypeScriptGeterrCollector collector) throws TSException {
		Request request = new GeterrRequest(files, delay);
		if (delay == 0) {
			JsonObject response;
			JsonArray result = execute(request, true, null).asArray();
			for (JsonValue r : result) {
				response = (JsonObject) r;
				collect(response, collector);
			}
		} else {
			// TODO
			execute(request, false, null);
		}
	}

	private void collect(JsonObject response, ITypeScriptGeterrCollector collector) {
		String event = response.getString("event", null);
		JsonObject body = response.get("body").asObject();
		String file = body.getString("file", null);
		JsonArray diagnostics = body.get("diagnostics").asArray();
		
		JsonObject diagnostic = null;
		String text = null;
		JsonObject start = null;
		JsonObject end = null;
		for (JsonValue value : diagnostics) {
			diagnostic = value.asObject();
			text = diagnostic.getString("text", null);
			start = diagnostic.get("start").asObject();
			end = diagnostic.get("end").asObject();
			collector.addDiagnostic(event, file, text, start.getInt("line", -1), start.getInt("offset", -1),
					end.getInt("line", -1), end.getInt("offset", -1));
		}
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
			JsonObject response = execute(request, true, null).asObject();
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
				if (process != null) {
					process.kill();
				}
				this.process = null;
				if (!pool.isShutdown()) {
					pool.shutdown();
				}

			}
		} finally {
			endWriteState();
		}
	}

	@Override
	public boolean isDisposed() {
		return dispose;
	}

	public void addProcessListener(INodejsProcessListener listener) {
		beginWriteState();
		try {
			if (nodeListeners == null) {
				nodeListeners = new ArrayList<INodejsProcessListener>();
			}
			nodeListeners.add(listener);
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
			if (nodeListeners != null && listener != null) {
				nodeListeners.remove(listener);
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

	// private void internalProcessVoidRequest(Request request, boolean async)
	// throws TSException {
	// if (interceptors == null) {
	// processVoidRequest(request, async);
	// } else {
	// long startTime = System.nanoTime();
	// try {
	// handleRequest(request);
	// processVoidRequest(request, async);
	// } catch (Throwable e) {
	// handleError(request, e, startTime);
	// if (e instanceof TSException) {
	// throw (TSException) e;
	// }
	// throw new TSException(e);
	// }
	// }
	// }

	// private JsonObject internalProcessRequest(Request request) throws
	// TSException {
	// if (interceptors == null) {
	// return processRequest(request);
	// } else {
	// long startTime = System.nanoTime();
	// try {
	// handleRequest(request);
	// JsonObject response = processRequest(request);
	// handleResponse(request, response, startTime);
	// return response;
	// } catch (Throwable e) {
	// handleError(request, e, startTime);
	// if (e instanceof TSException) {
	// throw (TSException) e;
	// }
	// throw new TSException(e);
	// }
	// }
	// }

	private void handleResponse(Request request, JsonObject response, long startTime) {
		if (response == null) {
			return;
		}
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
	//
	// protected abstract void processVoidRequest(Request request, boolean
	// async) throws TSException;
	//
	// protected abstract JsonObject processRequest(Request request) throws
	// TSException;

	private JsonValue execute(Request request, boolean expectsResult, CancellationToken token) throws TSException {
		RequestItem requestInfo = null;
		Future<JsonValue> result = null;
		if (expectsResult) {
			requestInfo = new RequestItem(request, request);
			result = pool.submit(requestInfo.callbacks);
		} else {
			requestInfo = new RequestItem(request, null);
		}
		synchronized (requestQueue) {
			this.requestQueue.add(requestInfo);
		}
		this.sendNextRequests();
		try {
			return result == null ? null : result.get();
		} catch (Exception e) {
			handleError(request, e, request.getStartTime());
			if (e instanceof TSException) {
				throw (TSException) e;
			}
			throw new TSException(e);
		}
	}

	private void sendNextRequests() throws TSException {
		RequestItem requestItem = null;
		while (this.pendingResponses.get() == 0 && !this.requestQueue.isEmpty()) {
			synchronized (requestQueue) {
				requestItem = this.requestQueue.remove(0); // shift
			}
			this.sendRequest(requestItem);
		}
	}

	private void sendRequest(RequestItem requestItem) throws TSException {
		Request serverRequest = requestItem.request;
		// log request
		handleRequest(serverRequest);
		boolean eventRequest = (serverRequest instanceof GeterrRequest);
		ICallbackItem callbacks = requestItem.callbacks;
		if (callbacks != null) {
			if (eventRequest) {
				GeterrRequest err = (GeterrRequest) serverRequest;
				synchronized (this.diagCallbacks) {
					for (String file : err.getFiles()) {
						this.diagCallbacks.put(file, callbacks);
						// this.pendingResponses.incrementAndGet();
						// this.pendingResponses.incrementAndGet();
					}
				}
			} else {
				synchronized (this.callbacks) {
					this.callbacks.put(serverRequest.getSeq(), callbacks);
				}
				this.pendingResponses.incrementAndGet();
			}
		}
		try {
			getProcess().sendRequest(serverRequest);
		} catch (TSException e) {
			if (eventRequest) {
				synchronized (this.callbacks) {
					GeterrRequest err = (GeterrRequest) serverRequest;
					synchronized (this.diagCallbacks) {
						for (String file : err.getFiles()) {
							this.diagCallbacks.remove(file);
							// this.pendingResponses.getAndDecrement();
							// this.pendingResponses.getAndDecrement();
						}
					}
				}
			} else {
				synchronized (this.callbacks) {
					ICallbackItem callback = this.callbacks.get(serverRequest.getSeq());
					if (callback != null) {
						// callback.e(err);
						this.callbacks.remove(serverRequest.getSeq());
					}
					this.pendingResponses.getAndDecrement();
				}
			}
			throw e;
		}

	}

	private void handleRequest(Request request) {
		if (interceptors == null) {
			return;
		}
		for (IInterceptor interceptor : interceptors) {
			interceptor.handleRequest(request, this, request.getCommand());
		}
	}

	private void dispatchMessage(JsonObject response) {
		String type = response.getString("type", null);
		if ("response".equals(type)) {
			int seq = response.getInt("request_seq", -1);
			ICallbackItem p = null;
			synchronized (callbacks) {
				p = this.callbacks.remove(seq);
			}
			if (p != null) {
				this.pendingResponses.getAndDecrement();
				p.complete(response);
				handleResponse(((Request) p), response, ((Request) p).getStartTime());
			}
		} else if ("event".equals(type)) {
			String event = response.getString("event", null);
			if ("syntaxDiag".equals(event) || "semanticDiag".equals(event)) {
				JsonObject body = response.get("body").asObject();
				if (body != null) {
					String file = body.getString("file", null);
					if (file != null) {
						ICallbackItem p = null;
						synchronized (diagCallbacks) {
							p = diagCallbacks.get(file);
							if (p != null) {
								if (p.complete(response)) {
									diagCallbacks.remove(file);
								}
								handleResponse(((Request) p), response, ((Request) p).getStartTime());
							}
						}
					}
				}
			}
		}
	}
}