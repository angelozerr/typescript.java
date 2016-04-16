/**
 *  Copyright (c) 2015-2016 Angelo ZERR.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package ts.client;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
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

import ts.TypeScriptException;
import ts.client.completions.ITypeScriptCompletionCollector;
import ts.client.completions.ITypeScriptCompletionEntryDetailsCollector;
import ts.client.definition.ITypeScriptDefinitionCollector;
import ts.client.format.ITypeScriptFormatCollector;
import ts.client.geterr.ITypeScriptGeterrCollector;
import ts.client.occurrences.ITypeScriptOccurrencesCollector;
import ts.client.quickinfo.ITypeScriptQuickInfoCollector;
import ts.client.references.ITypeScriptReferencesCollector;
import ts.client.signaturehelp.ITypeScriptSignatureHelpCollector;
import ts.internal.FileTempHelper;
import ts.internal.SequenceHelper;
import ts.internal.client.ICallbackItem;
import ts.internal.client.RequestItem;
import ts.internal.client.protocol.ChangeRequest;
import ts.internal.client.protocol.CloseRequest;
import ts.internal.client.protocol.CompletionDetailsRequest;
import ts.internal.client.protocol.CompletionsRequest;
import ts.internal.client.protocol.DefinitionRequest;
import ts.internal.client.protocol.FormatRequest;
import ts.internal.client.protocol.GeterrRequest;
import ts.internal.client.protocol.OccurrencesRequest;
import ts.internal.client.protocol.OpenRequest;
import ts.internal.client.protocol.QuickInfoRequest;
import ts.internal.client.protocol.ReferencesRequest;
import ts.internal.client.protocol.ReloadRequest;
import ts.internal.client.protocol.Request;
import ts.internal.client.protocol.SignatureHelpRequest;
import ts.nodejs.INodejsLaunchConfiguration;
import ts.nodejs.INodejsProcess;
import ts.nodejs.INodejsProcessListener;
import ts.nodejs.NodejsProcessAdapter;
import ts.nodejs.NodejsProcessManager;
import ts.utils.FileUtils;
import ts.utils.JsonHelper;

/**
 * TypeScript service client implementation.
 * 
 */
public class TypeScriptServiceClient implements ITypeScriptServiceClient {

	private static final String TSSERVER_FILE_TYPE = "tsserver";
	private boolean dispose;
	private final List<ITypeScriptClientListener> listeners;
	private ReentrantReadWriteLock stateLock = new ReentrantReadWriteLock();
	private List<IInterceptor> interceptors;

	private final ExecutorService pool = Executors.newFixedThreadPool(2);
	private final ExecutorService diagPool = Executors.newFixedThreadPool(2);

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

	public TypeScriptServiceClient(final File projectDir, File tsserverFile, File nodeFile) throws TypeScriptException {
		this(projectDir, NodejsProcessManager.getInstance().create(projectDir, tsserverFile, nodeFile,
				new INodejsLaunchConfiguration() {

					@Override
					public List<String> createNodeArgs() {
						List<String> args = new ArrayList<String>();
						args.add("-p");
						args.add(FileUtils.getPath(projectDir));
						return args;
					}
				}, TSSERVER_FILE_TYPE));
	}

	public TypeScriptServiceClient(File projectDir, INodejsProcess process) {
		this.listeners = new ArrayList<ITypeScriptClientListener>();
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

	private INodejsProcess getProcess() throws TypeScriptException {
		if (!process.isStarted()) {
			process.start();
		}
		return process;
	}

	@Override
	public void openFile(String fileName, String contents) throws TypeScriptException {
		Request request = new OpenRequest(fileName, contents);
		execute(request, false, null);
	}

	@Override
	public void closeFile(String fileName) throws TypeScriptException {
		Request request = new CloseRequest(fileName);
		execute(request, false, null);
	}

	// ---------------- Completions

	@Override
	public void completions(String fileName, int line, int offset, String prefix,
			ITypeScriptCompletionCollector collector) throws TypeScriptException {
		CompletionsRequest request = new CompletionsRequest(fileName, line, offset, prefix);
		JsonObject response = execute(request, true, null).asObject();
		collectCompletions(response, fileName, line, offset, collector);
	}

	private void collectCompletions(JsonObject response, String fileName, int line, int offset,
			ITypeScriptCompletionCollector collector) {
		JsonArray items = response.get("body").asArray();
		JsonObject obj = null;
		for (JsonValue item : items) {
			obj = (JsonObject) item;
			collector.addCompletionEntry(obj.getString("name", ""), obj.getString("kind", ""),
					obj.getString("kindModifiers", ""), obj.getString("sortText", ""), fileName, line, offset, this);
		}
	}

	@Override
	public void completionEntryDetails(String fileName, int line, int offset, String[] entryNames,
			ITypeScriptCompletionEntryDetailsCollector collector) throws TypeScriptException {
		CompletionDetailsRequest request = new CompletionDetailsRequest(fileName, line, offset, entryNames);
		JsonObject response = execute(request, true, null).asObject();
		collectCompletionDetails(response, collector);
	}

	private void collectCompletionDetails(JsonObject response, ITypeScriptCompletionEntryDetailsCollector collector) {
		JsonArray body = response.get("body").asArray();
		if (body != null && body.size() > 0) {
			JsonObject obj = body.get(0).asObject();
			collector.setEntryDetails(obj.getString("name", ""), obj.getString("kind", ""),
					obj.getString("kindModifiers", ""));
			// displayParts
			JsonArray displayParts = JsonHelper.getArray(obj, "displayParts");
			JsonObject o = null;
			if (displayParts != null) {
				for (JsonValue part : displayParts) {
					o = part.asObject();
					collector.addDisplayPart(o.getString("text", ""), o.getString("kind", ""));
				}
			}
			// documentation
			JsonArray documentation = JsonHelper.getArray(obj, "documentation");
			if (documentation != null) {
				for (JsonValue part : documentation) {
					o = part.asObject();
					collector.addDisplayPart(o.getString("text", ""), o.getString("kind", ""));
				}
			}
		}
	}

	// ---------------- Definition

	@Override
	public void definition(String fileName, int line, int offset, ITypeScriptDefinitionCollector collector)
			throws TypeScriptException {
		DefinitionRequest request = new DefinitionRequest(fileName, line, offset);
		JsonObject response = execute(request, true, null).asObject();
		collectDefinition(response, collector);
	}

	private void collectDefinition(JsonObject response, ITypeScriptDefinitionCollector collector)
			throws TypeScriptException {
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
			throws TypeScriptException {
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
			throws TypeScriptException {
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
			throws TypeScriptException {
		Request request = new ChangeRequest(fileName, line, offset, endLine, endOffset, newText);
		execute(request, false, null);
	}

	@Override
	public void geterr(String[] files, int delay, ITypeScriptGeterrCollector collector) throws TypeScriptException {
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

	// -------------------------- Format

	@Override
	public void format(String fileName, int line, int offset, int endLine, int endOffset,
			ITypeScriptFormatCollector collector) throws TypeScriptException {
		Request request = new FormatRequest(fileName, line, offset, endLine, endOffset);
		JsonObject response = execute(request, true, null).asObject();
		collectFormat(response.get("body").asArray(), collector);
	}

	private void collectFormat(JsonArray body, ITypeScriptFormatCollector collector) throws TypeScriptException {
		JsonObject item = null;
		String newText = null;
		JsonObject start = null;
		JsonObject end = null;
		for (JsonValue b : body) {
			item = b.asObject();
			start = item.get("start").asObject();
			end = item.get("end").asObject();
			newText = item.getString("newText", null);
			collector.format(start.getInt("line", -1), start.getInt("offset", -1), end.getInt("line", -1),
					end.getInt("offset", -1), newText);
		}
	}

	// ----------------- Find References

	@Override
	public void references(String fileName, int line, int offset, ITypeScriptReferencesCollector collector)
			throws TypeScriptException {
		ReferencesRequest request = new ReferencesRequest(fileName, line, offset, collector);
		execute(request);
	}

	// ----------------- Occurrences

	@Override
	public void occurrences(String fileName, int line, int offset, ITypeScriptOccurrencesCollector collector)
			throws TypeScriptException {
		OccurrencesRequest request = new OccurrencesRequest(fileName, line, offset, collector);
		execute(request);
	}

	private void execute(Request request) throws TypeScriptException {
		if (!request.isAsynch()) {
			JsonObject response = execute(request, true, null).asObject();
			request.collect(response);
		} else {
			execute(request, true, null);
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
	public void updateFile(String fileName, String newText) throws TypeScriptException {
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
	public void addClientListener(ITypeScriptClientListener listener) {
		synchronized (listeners) {
			listeners.add(listener);
		}
	}

	@Override
	public void removeClientListener(ITypeScriptClientListener listener) {
		synchronized (listeners) {
			listeners.remove(listener);
		}
	}

	protected void fireStartServer() {
		synchronized (listeners) {
			for (ITypeScriptClientListener listener : listeners) {
				listener.onStart(this);
			}
		}
	}

	protected void fireEndServer() {
		synchronized (listeners) {
			for (ITypeScriptClientListener listener : listeners) {
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
				if (!diagPool.isShutdown()) {
					diagPool.shutdown();
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

	@Override
	public void join() throws InterruptedException {
		if (process != null) {
			this.process.join();
		}
	}

	/**
	 * Execute the given request and returns the result of the request if it
	 * needed and null otherwise.
	 * 
	 * @param request
	 *            the request to execute
	 * @param expectsResult
	 *            true if a result must be returned and false otherwise.
	 * @param token
	 * @return the result of the request if it needed and null otherwise.
	 * @throws TypeScriptException
	 */
	private JsonValue execute(Request request, boolean expectsResult, ICancellationToken token)
			throws TypeScriptException {
		boolean eventRequest = (request instanceof GeterrRequest);
		RequestItem requestInfo = null;
		Future<JsonValue> result = null;
		if (expectsResult) {
			requestInfo = new RequestItem(request, request);
			if (eventRequest) {
				result = diagPool.submit(requestInfo.callbacks);
			} else {
				result = pool.submit(requestInfo.callbacks);
			}
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
			TypeScriptException tse = getTypeScriptException(e);
			if (tse != null) {
//				if (tse instanceof TypeScriptTimeoutException) {
//					// when time out exception, we must be sure that the request
//					// is removed
//					tryCancelRequest(((TypeScriptTimeoutException) tse).getRequest().getSeq());
//				}
				throw (TypeScriptException) tse;
			}
			throw new TypeScriptException(e);
		}
	}

	private TypeScriptException getTypeScriptException(Exception e) {
		if (e instanceof TypeScriptException) {
			return (TypeScriptException) e;
		}
		if (e.getCause() instanceof TypeScriptException) {
			return (TypeScriptException) e.getCause();
		}
		return null;
	}

	private boolean tryCancelRequest(int seq) {
		for (RequestItem requestItem : requestQueue) {
			if (requestItem.request.getSeq() == seq) {
				synchronized (requestQueue) {
					requestQueue.remove(requestItem);
				}
				return true;
			}
		}
		return false;
	}

	private void sendNextRequests() {
		RequestItem requestItem = null;
		while (this.pendingResponses.get() == 0 && !this.requestQueue.isEmpty()) {
			synchronized (requestQueue) {
				requestItem = this.requestQueue.remove(0); // shift
			}
			this.sendRequest(requestItem);
		}
	}

	private void sendRequest(RequestItem requestItem) {
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
		} catch (TypeScriptException e) {
			if (eventRequest) {
				synchronized (this.callbacks) {
					GeterrRequest err = (GeterrRequest) serverRequest;
					synchronized (this.diagCallbacks) {
						for (String file : err.getFiles()) {
							this.diagCallbacks.remove(file);
						}
					}
				}
			} else {
				synchronized (this.callbacks) {
					ICallbackItem callback = this.callbacks.get(serverRequest.getSeq());
					if (callback != null) {
						callback.error(e);
						this.callbacks.remove(serverRequest.getSeq());
					}
					this.pendingResponses.getAndDecrement();
				}
			}
		}

	}

	private void dispatchMessage(JsonObject response) {
		try {
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
		finally {
			this.sendNextRequests();
		}
	}

	// --------------------------- Handler for Request/response/Error
	// ------------------------------------

	/**
	 * Handle the given request.
	 * 
	 * @param request
	 */
	private void handleRequest(Request request) {
		if (interceptors == null) {
			return;
		}
		for (IInterceptor interceptor : interceptors) {
			interceptor.handleRequest(request, this, request.getCommand());
		}
	}

	/**
	 * Handle the given reponse.
	 * 
	 * @param request
	 * @param response
	 * @param startTime
	 */
	private void handleResponse(Request request, JsonObject response, long startTime) {
		if (interceptors == null) {
			return;
		}
		long ellapsedTime = getElapsedTimeInMs(startTime);
		for (IInterceptor interceptor : interceptors) {
			interceptor.handleResponse(response, this, request.getCommand(), ellapsedTime);
		}
	}

	/**
	 * Handle the given error.
	 * 
	 * @param request
	 * @param e
	 * @param startTime
	 */
	private void handleError(Request request, Throwable e, long startTime) {
		if (interceptors == null) {
			return;
		}
		long ellapsedTime = getElapsedTimeInMs(startTime);
		for (IInterceptor interceptor : interceptors) {
			interceptor.handleError(e, this, request.getCommand(), ellapsedTime);
		}
	}

	/**
	 * Returns the elappsed time in ms.
	 * 
	 * @param startTime
	 *            in nano time.
	 * @return the elappsed time in ms.
	 */
	private static long getElapsedTimeInMs(long startTime) {
		return ((System.nanoTime() - startTime) / 1000000L);
	}
}