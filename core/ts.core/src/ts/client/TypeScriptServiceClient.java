/**
 *  Copyright (c) 2015-2017 Angelo ZERR.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 *  Lorenzo Dalla Vecchia <lorenzo.dallavecchia@webratio.com> - adjusted usage of CompletableFuture, added required requests, openExternalProject
 */
package ts.client;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import ts.TypeScriptException;
import ts.TypeScriptNoContentAvailableException;
import ts.client.codefixes.CodeAction;
import ts.client.compileonsave.CompileOnSaveAffectedFileListSingleProject;
import ts.client.completions.CompletionEntry;
import ts.client.completions.CompletionEntryDetails;
import ts.client.completions.ICompletionEntryFactory;
import ts.client.completions.ICompletionEntryMatcherProvider;
import ts.client.configure.ConfigureRequestArguments;
import ts.client.diagnostics.Diagnostic;
import ts.client.diagnostics.DiagnosticEvent;
import ts.client.diagnostics.DiagnosticEventBody;
import ts.client.installtypes.BeginInstallTypesEventBody;
import ts.client.installtypes.EndInstallTypesEventBody;
import ts.client.installtypes.IInstallTypesListener;
import ts.client.jsdoc.TextInsertion;
import ts.client.navbar.NavigationBarItem;
import ts.client.occurrences.OccurrencesResponseItem;
import ts.client.projectinfo.ProjectInfo;
import ts.client.quickinfo.QuickInfo;
import ts.client.references.ReferencesResponseBody;
import ts.client.rename.RenameResponseBody;
import ts.client.signaturehelp.SignatureHelpItems;
import ts.internal.FileTempHelper;
import ts.internal.SequenceHelper;
import ts.internal.client.protocol.ChangeRequest;
import ts.internal.client.protocol.CloseRequest;
import ts.internal.client.protocol.CodeFixRequest;
import ts.internal.client.protocol.CompileOnSaveAffectedFileListRequest;
import ts.internal.client.protocol.CompileOnSaveEmitFileRequest;
import ts.internal.client.protocol.CompletionDetailsRequest;
import ts.internal.client.protocol.CompletionsRequest;
import ts.internal.client.protocol.ConfigureRequest;
import ts.internal.client.protocol.DefinitionRequest;
import ts.internal.client.protocol.DocCommentTemplateRequest;
import ts.internal.client.protocol.FormatRequest;
import ts.internal.client.protocol.GetSupportedCodeFixesRequest;
import ts.internal.client.protocol.GeterrForProjectRequest;
import ts.internal.client.protocol.GeterrRequest;
import ts.internal.client.protocol.GsonHelper;
import ts.internal.client.protocol.IRequestEventable;
import ts.internal.client.protocol.ImplementationRequest;
import ts.internal.client.protocol.MessageType;
import ts.internal.client.protocol.NavBarRequest;
import ts.internal.client.protocol.NavTreeRequest;
import ts.internal.client.protocol.OccurrencesRequest;
import ts.internal.client.protocol.OpenExternalProjectRequest;
import ts.internal.client.protocol.OpenRequest;
import ts.internal.client.protocol.ProjectInfoRequest;
import ts.internal.client.protocol.QuickInfoRequest;
import ts.internal.client.protocol.ReferencesRequest;
import ts.internal.client.protocol.ReloadRequest;
import ts.internal.client.protocol.RenameRequest;
import ts.internal.client.protocol.Request;
import ts.internal.client.protocol.Response;
import ts.internal.client.protocol.SemanticDiagnosticsSyncRequest;
import ts.internal.client.protocol.SignatureHelpRequest;
import ts.internal.client.protocol.SyntacticDiagnosticsSyncRequest;
import ts.nodejs.INodejsLaunchConfiguration;
import ts.nodejs.INodejsProcess;
import ts.nodejs.INodejsProcessListener;
import ts.nodejs.NodejsProcessAdapter;
import ts.nodejs.NodejsProcessManager;
import ts.repository.TypeScriptRepositoryManager;
import ts.utils.FileUtils;

/**
 * TypeScript service client implementation.
 *
 */
public class TypeScriptServiceClient implements ITypeScriptServiceClient {

	private static final String NO_CONTENT_AVAILABLE = "No content available.";
	private static final String TSSERVER_FILE_TYPE = "tsserver";

	private INodejsProcess process;
	private List<INodejsProcessListener> nodeListeners;
	private final List<ITypeScriptClientListener> listeners;
	private final List<IInstallTypesListener> installTypesListener;
	private final ReentrantReadWriteLock stateLock;
	private boolean dispose;

	private final Map<Integer, PendingRequestInfo> sentRequestMap;
	private final Map<String, PendingRequestEventInfo> receivedRequestMap;
	private List<IInterceptor> interceptors;

	private ICompletionEntryMatcherProvider completionEntryMatcherProvider;

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
			if (message.startsWith("{")) {
				TypeScriptServiceClient.this.dispatchMessage(message);
			}
		}

	};

	private static abstract class RequestInfo {
		final Request<?> requestMessage;
		final long startTime;
		final CompletableFuture<?> requiredResult;

		RequestInfo(Request<?> requestMessage, CompletableFuture<?> requiredResult) {
			this.requestMessage = requestMessage;
			this.startTime = System.nanoTime();
			this.requiredResult = requiredResult;
		}
	}

	private static class PendingRequestInfo extends RequestInfo {
		final Consumer<Response<?>> responseHandler;

		PendingRequestInfo(Request<?> requestMessage, Consumer<Response<?>> responseHandler,
				CompletableFuture<?> requiredResult) {
			super(requestMessage, requiredResult);
			this.responseHandler = responseHandler;
		}
	}

	private static class PendingRequestEventInfo extends RequestInfo {
		final Consumer<Event<?>> eventHandler;

		PendingRequestEventInfo(Request<?> requestMessage, Consumer<Event<?>> eventHandler,
				CompletableFuture<?> requiredResult) {
			super(requestMessage, requiredResult);
			this.eventHandler = eventHandler;
		}
	}

	public TypeScriptServiceClient(final File projectDir, File tsserverFile, File nodeFile) throws TypeScriptException {
		this(projectDir, tsserverFile, nodeFile, false, false, null);
	}

	public TypeScriptServiceClient(final File projectDir, File typescriptDir, File nodeFile, boolean enableTelemetry,
			boolean disableAutomaticTypingAcquisition, File tsserverPluginsFile) throws TypeScriptException {
		this(NodejsProcessManager.getInstance().create(projectDir,
				tsserverPluginsFile != null ? tsserverPluginsFile
						: TypeScriptRepositoryManager.getTsserverFile(typescriptDir),
				nodeFile, new INodejsLaunchConfiguration() {

					@Override
					public List<String> createNodeArgs() {
						List<String> args = new ArrayList<String>();
						// args.add("-p");
						// args.add(FileUtils.getPath(projectDir));
						if (enableTelemetry) {
							args.add("--enableTelemetry");
						}
						if (disableAutomaticTypingAcquisition) {
							args.add("--disableAutomaticTypingAcquisition");
						}
						if (tsserverPluginsFile != null) {
							args.add("--typescriptDir");
							args.add(FileUtils.getPath(typescriptDir));
						}
						// args.add("--useSingleInferredProject");
						return args;
					}
				}, TSSERVER_FILE_TYPE));
	}

	public TypeScriptServiceClient(INodejsProcess process) {
		this.listeners = new ArrayList<>();
		this.installTypesListener = new ArrayList<>();
		this.stateLock = new ReentrantReadWriteLock();
		this.dispose = false;
		this.sentRequestMap = new LinkedHashMap<>();
		this.receivedRequestMap = new LinkedHashMap<>();
		this.process = process;
		process.addProcessListener(listener);
		setCompletionEntryMatcherProvider(ICompletionEntryMatcherProvider.LCS_PROVIDER);
	}

	private void dispatchMessage(String message) {
		JsonObject json = GsonHelper.parse(message).getAsJsonObject();
		JsonElement typeElement = json.get("type");
		if (typeElement != null) {
			MessageType messageType = MessageType.getType(typeElement.getAsString());
			if (messageType == null) {
				throw new IllegalStateException("Unknown response type message " + json);
			}
			switch (messageType) {
			case response:
				int seq = json.get("request_seq").getAsInt();
				PendingRequestInfo pendingRequestInfo;
				synchronized (sentRequestMap) {
					pendingRequestInfo = sentRequestMap.remove(seq);
				}
				if (pendingRequestInfo == null) {
					// throw new IllegalStateException("Unmatched response
					// message " + json);
					return;
				}
				Response<?> responseMessage = pendingRequestInfo.requestMessage.parseResponse(json);
				if (responseMessage == null) {
					responseMessage = GsonHelper.DEFAULT_GSON.fromJson(json, Response.class);
				}
				try {
					handleResponse(responseMessage, message, pendingRequestInfo.startTime);
					pendingRequestInfo.responseHandler.accept(responseMessage);
				} catch (RuntimeException e) {
					// LOG.log(Level.WARNING, "Handling repsonse
					// "+responseMessage+" threw an exception.", e);
				}

				break;
			case event:
				String event = json.get("event").getAsString();
				if ("syntaxDiag".equals(event) || "semanticDiag".equals(event)) {
					DiagnosticEvent response = GsonHelper.DEFAULT_GSON.fromJson(json, DiagnosticEvent.class);
					PendingRequestEventInfo pendingRequestEventInfo;
					synchronized (receivedRequestMap) {
						pendingRequestEventInfo = receivedRequestMap.remove(response.getKey());
					}
					if (pendingRequestEventInfo != null) {
						pendingRequestEventInfo.eventHandler.accept(response);
					}
				} else if ("telemetry".equals(event)) {
					// TelemetryEventBody telemetryData =
					// GsonHelper.DEFAULT_GSON.fromJson(json,
					// TelemetryEvent.class)
					// .getBody();
					//
					JsonObject telemetryData = json.get("body").getAsJsonObject();
					JsonObject payload = telemetryData.has("payload") ? telemetryData.get("payload").getAsJsonObject()
							: null;
					if (payload != null) {
						String telemetryEventName = telemetryData.get("telemetryEventName").getAsString();
						fireLogTelemetry(telemetryEventName, payload);
					}
				} else if ("beginInstallTypes".equals(event)) {
					BeginInstallTypesEventBody data = GsonHelper.DEFAULT_GSON.fromJson(json,
							BeginInstallTypesEventBody.class);
					fireBeginInstallTypes(data);
				} else if ("endInstallTypes".equals(event)) {
					EndInstallTypesEventBody data = GsonHelper.DEFAULT_GSON.fromJson(json,
							EndInstallTypesEventBody.class);
					fireEndInstallTypes(data);
				}
				break;
			default:
				// Do nothing
			}
		}
	}

	@Override
	public void openFile(String fileName, String content) throws TypeScriptException {
		openFile(fileName, content, null);
	}

	@Override
	public void openFile(String fileName, String content, ScriptKindName scriptKindName) throws TypeScriptException {
		waitForFuture(executeNoResult(new OpenRequest(fileName, null, content, scriptKindName)));
	}

	@Override
	public void closeFile(String fileName) throws TypeScriptException {
		waitForFuture(executeNoResult(new CloseRequest(fileName)));
	}

	// @Override
	// public void changeFile(String fileName, int position, int endPosition,
	// String insertString) {
	// execute(new ChangeRequest(fileName, position, endPosition, insertString),
	// false);
	// }

	@Override
	public void changeFile(String fileName, int line, int offset, int endLine, int endOffset, String insertString)
			throws TypeScriptException {
		waitForFuture(executeNoResult(new ChangeRequest(fileName, line, offset, endLine, endOffset, insertString)));
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
		String tempFileName = FileTempHelper.updateTempFile(newText, seq);
		try {
			execute(new ReloadRequest(fileName, tempFileName, seq)).get(5000, TimeUnit.MILLISECONDS);
		} catch (Exception e) {
			if (e instanceof TypeScriptException) {
				throw (TypeScriptException) e;
			}
			throw new TypeScriptException(e);
		}
	}

	// @Override
	// public CompletableFuture<List<CompletionEntry>> completions(String
	// fileName, int position) {
	// return execute(new CompletionsRequest(fileName, position));
	// }

	@Override
	public CompletableFuture<List<CompletionEntry>> completions(String fileName, int line, int offset) {
		return completions(fileName, line, offset, ICompletionEntryFactory.DEFAULT);
	}

	@Override
	public CompletableFuture<List<CompletionEntry>> completions(String fileName, int line, int offset,
			ICompletionEntryFactory factory) {
		return execute(
				new CompletionsRequest(fileName, line, offset, getCompletionEntryMatcherProvider(), this, factory));
	}

	@Override
	public CompletableFuture<List<CompletionEntryDetails>> completionEntryDetails(String fileName, int line, int offset,
			String[] entryNames, CompletionEntry completionEntry) {
		return execute(new CompletionDetailsRequest(fileName, line, offset, null, entryNames));
	}

	@Override
	public CompletableFuture<List<FileSpan>> definition(String fileName, int line, int offset) {
		return execute(new DefinitionRequest(fileName, line, offset));
	}

	@Override
	public CompletableFuture<SignatureHelpItems> signatureHelp(String fileName, int line, int offset) {
		return execute(new SignatureHelpRequest(fileName, line, offset));
	}

	@Override
	public CompletableFuture<QuickInfo> quickInfo(String fileName, int line, int offset) {
		return execute(new QuickInfoRequest(fileName, line, offset));
	}

	@Override
	public CompletableFuture<List<DiagnosticEvent>> geterr(String[] files, int delay) {
		return execute(new GeterrRequest(files, delay));
	}

	@Override
	public CompletableFuture<List<DiagnosticEvent>> geterrForProject(String file, int delay, ProjectInfo projectInfo) {
		return execute(new GeterrForProjectRequest(file, delay, projectInfo));
	}

	@Override
	public CompletableFuture<List<CodeEdit>> format(String fileName, int line, int offset, int endLine, int endOffset) {
		return execute(new FormatRequest(fileName, line, offset, endLine, endOffset));
	}

	@Override
	public CompletableFuture<ReferencesResponseBody> references(String fileName, int line, int offset) {
		return execute(new ReferencesRequest(fileName, line, offset));
	}

	@Override
	public CompletableFuture<List<OccurrencesResponseItem>> occurrences(String fileName, int line, int offset) {
		return execute(new OccurrencesRequest(fileName, line, offset));
	}

	@Override
	public CompletableFuture<RenameResponseBody> rename(String file, int line, int offset, Boolean findInComments,
			Boolean findInStrings) {
		return execute(new RenameRequest(file, line, offset, findInComments, findInStrings));
	}

	@Override
	public CompletableFuture<List<NavigationBarItem>> navbar(String fileName, IPositionProvider positionProvider) {
		return execute(new NavBarRequest(fileName, positionProvider));
	}

	@Override
	public void configure(ConfigureRequestArguments arguments) throws TypeScriptException {
		waitForFuture(execute(new ConfigureRequest(arguments)));
	}

	@Override
	public CompletableFuture<ProjectInfo> projectInfo(String file, String projectFileName, boolean needFileNameList) {
		return execute(new ProjectInfoRequest(file, needFileNameList));
	}

	@Override
	public CompletableFuture<Void> openExternalProject(String projectFileName, List<String> rootFileNames) {
		return executeCausingWait(new OpenExternalProjectRequest(projectFileName, rootFileNames));
	}

	// Since 2.0.3

	@Override
	public CompletableFuture<DiagnosticEventBody> semanticDiagnosticsSync(String file, Boolean includeLinePosition) {
		return execute(new SemanticDiagnosticsSyncRequest(file, includeLinePosition)).thenApply(d -> {
			return new DiagnosticEventBody(file, (List<Diagnostic>) d);
		});
	}

	@Override
	public CompletableFuture<DiagnosticEventBody> syntacticDiagnosticsSync(String file, Boolean includeLinePosition) {
		return execute(new SyntacticDiagnosticsSyncRequest(file, includeLinePosition)).thenApply(d -> {
			return new DiagnosticEventBody(file, (List<Diagnostic>) d);
		});
	}

	// Since 2.0.5

	@Override
	public CompletableFuture<Boolean> compileOnSaveEmitFile(String fileName, Boolean forced) {
		return execute(new CompileOnSaveEmitFileRequest(fileName, forced));
	}

	@Override
	public CompletableFuture<List<CompileOnSaveAffectedFileListSingleProject>> compileOnSaveAffectedFileList(
			String fileName) {
		return execute(new CompileOnSaveAffectedFileListRequest(fileName));
	}

	// Since 2.0.6

	@Override
	public CompletableFuture<NavigationBarItem> navtree(String fileName, IPositionProvider positionProvider) {
		return execute(new NavTreeRequest(fileName, positionProvider));
	}

	@Override
	public CompletableFuture<TextInsertion> docCommentTemplate(String fileName, int line, int offset) {
		return execute(new DocCommentTemplateRequest(fileName, line, offset));
	}

	// Since 2.1.0

	@Override
	public CompletableFuture<List<CodeAction>> getCodeFixes(String fileName, IPositionProvider positionProvider,
			int startLine, int startOffset, int endLine, int endOffset, List<Integer> errorCodes) {
		return execute(new CodeFixRequest(fileName, startLine, startOffset, endLine, endOffset, errorCodes));
	}

	@Override
	public CompletableFuture<List<String>> getSupportedCodeFixes() {
		return execute(new GetSupportedCodeFixesRequest());
	}

	@Override
	public CompletableFuture<List<FileSpan>> implementation(String fileName, int line, int offset) {
		return execute(new ImplementationRequest(fileName, line, offset));
	}

	/**
	 * Executes a request that does not expect a result. A future is returned
	 * (even if there is no result) in order to handle the possible waiting for
	 * required requests and for their exceptions.
	 * 
	 * @param request
	 */
	private CompletableFuture<Void> executeNoResult(Request<?> request) {
		return this._execute(request, false, false);
	}

	/**
	 * Executes a request that expects a result. The waiting for any pending
	 * request is chained with the completable future result that the caller has
	 * to handle anyway.
	 * 
	 * @param request
	 * @return
	 */
	private <T> CompletableFuture<T> execute(Request<?> request) {
		return this._execute(request, true, false);
	}

	/**
	 * Executes a request that expects a result, causing all subsequent requests
	 * to wait for this one to complete.
	 * 
	 * @param request
	 * @return
	 */
	private <T> CompletableFuture<T> executeCausingWait(Request<?> request) {
		return this._execute(request, true, true);
	}

	private <T> CompletableFuture<T> _execute(Request<?> request, boolean expectsResult, boolean causesWait) {

		// prepare a future for waiting for all required requests
		final CompletableFuture<?> requiredFuture = computeRequiredRequestsFuture();

		// send the request after waiting for previous required requests
		return requiredFuture.thenCompose(requiredResult -> {
			CompletableFuture<T> result = new CompletableFuture<>();

			// augment the future with response handling, if expected
			if (expectsResult) {
				// remove request info after completing exceptionally
				result.whenComplete((requestResult, e) -> {
					if (e != null) {
						if (request instanceof IRequestEventable) {
							List<String> keys = ((IRequestEventable) request).getKeys();
							synchronized (receivedRequestMap) {
								for (String key : keys) {
									receivedRequestMap.remove(key);
								}
							}
						} else {
							synchronized (sentRequestMap) {
								sentRequestMap.remove(request.getSeq());
							}
						}
					}
				});
				// register request info in maps for handling the result
				if (request instanceof IRequestEventable) {
					Consumer<Event<?>> responseHandler = (event) -> {
						if (((IRequestEventable) request).accept(event)) {
							result.complete((T) ((IRequestEventable) request).getEvents());
						}
					};
					List<String> keys = ((IRequestEventable) request).getKeys();
					PendingRequestEventInfo info = new PendingRequestEventInfo(request, responseHandler,
							causesWait ? result : null);
					synchronized (receivedRequestMap) {
						for (String key : keys) {
							receivedRequestMap.put(key, info);
						}
					}
				} else {
					Consumer<Response<?>> responseHandler = (response) -> {
						if (response.isSuccess()) {
							// tsserver response with success
							result.complete((T) response.getBody());
						} else {
							// tsserver response with error
							result.completeExceptionally(createException(response.getMessage()));
						}
					};
					int seq = request.getSeq();
					synchronized (sentRequestMap) {
						sentRequestMap.put(seq,
								new PendingRequestInfo(request, responseHandler, causesWait ? result : null));
					}
				}
			}

			// send the request
			try {
				sendRequest(request);
			} catch (TypeScriptException e) {
				result.completeExceptionally(e);
			}

			// complete immediately, if no result expected
			if (!expectsResult) {
				result.complete(null);
			}

			return result;
		});
	}

	private CompletableFuture<Void> computeRequiredRequestsFuture() {
		synchronized (receivedRequestMap) {
			synchronized (sentRequestMap) {
				if (receivedRequestMap.isEmpty() && sentRequestMap.isEmpty()) {
					return CompletableFuture.completedFuture(null);
				}
				List<CompletableFuture<?>> cfs = new ArrayList<>();
				for (RequestInfo info : receivedRequestMap.values()) {
					if (info.requiredResult != null) {
						cfs.add(info.requiredResult);
					}
				}
				for (RequestInfo info : sentRequestMap.values()) {
					if (info.requiredResult != null) {
						cfs.add(info.requiredResult);
					}
				}
				return CompletableFuture.allOf(cfs.toArray(new CompletableFuture<?>[cfs.size()]));
			}
		}
	}

	private static <T> T waitForFuture(Future<T> future) throws TypeScriptException {
		try {
			return future.get(5000, TimeUnit.MILLISECONDS);
		} catch (ExecutionException e) {
			Throwable cause = e.getCause();
			if (cause instanceof TypeScriptException) {
				throw (TypeScriptException) cause;
			}
			throw new TypeScriptException(cause);
		} catch (Exception e) {
			throw new TypeScriptException(e);
		}
	}

	private TypeScriptException createException(String message) {
		if (NO_CONTENT_AVAILABLE.equals(message)) {
			return new TypeScriptNoContentAvailableException(message);
		}
		return new TypeScriptException(message);
	}

	private void sendRequest(Request<?> request) throws TypeScriptException {
		String req = GsonHelper.DEFAULT_GSON.toJson(request);
		handleRequest(request, req);
		getProcess().sendRequest(req);
	}

	private INodejsProcess getProcess() throws TypeScriptException {
		if (!process.isStarted()) {
			process.start();
		}
		return process;
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

	private void fireStartServer() {
		synchronized (listeners) {
			for (ITypeScriptClientListener listener : listeners) {
				listener.onStart(this);
			}
		}
	}

	private void fireEndServer() {
		synchronized (listeners) {
			for (ITypeScriptClientListener listener : listeners) {
				listener.onStop(this);
			}
		}
	}

	@Override
	public void addInstallTypesListener(IInstallTypesListener listener) {
		synchronized (installTypesListener) {
			installTypesListener.add(listener);
		}
	}

	@Override
	public void removeInstallTypesListener(IInstallTypesListener listener) {
		synchronized (installTypesListener) {
			installTypesListener.remove(listener);
		}
	}

	private void fireBeginInstallTypes(BeginInstallTypesEventBody body) {
		synchronized (installTypesListener) {
			for (IInstallTypesListener listener : installTypesListener) {
				listener.onBegin(body);
			}
		}
	}

	private void fireEndInstallTypes(EndInstallTypesEventBody body) {
		synchronized (installTypesListener) {
			for (IInstallTypesListener listener : installTypesListener) {
				listener.onEnd(body);
			}
		}
	}

	private void fireLogTelemetry(String telemetryEventName, JsonObject payload) {
		synchronized (installTypesListener) {
			for (IInstallTypesListener listener : installTypesListener) {
				listener.logTelemetry(telemetryEventName, payload);
			}
		}
	}

	@Override
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

	@Override
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

	@Override
	public boolean isDisposed() {
		return dispose;
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
			}
		} finally {
			endWriteState();
		}
	}

	private void beginReadState() {
		stateLock.readLock().lock();
	}

	private void endReadState() {
		stateLock.readLock().unlock();
	}

	private void beginWriteState() {
		stateLock.writeLock().lock();
	}

	private void endWriteState() {
		stateLock.writeLock().unlock();
	}

	public void setCompletionEntryMatcherProvider(ICompletionEntryMatcherProvider completionEntryMatcherProvider) {
		this.completionEntryMatcherProvider = completionEntryMatcherProvider;
	}

	public ICompletionEntryMatcherProvider getCompletionEntryMatcherProvider() {
		return completionEntryMatcherProvider;
	}

	// --------------------------- Handler for Request/response/Error
	// ------------------------------------

	/**
	 * Handle the given request.
	 * 
	 * @param request
	 */
	private void handleRequest(Request<?> request, String json) {
		if (interceptors == null) {
			return;
		}
		for (IInterceptor interceptor : interceptors) {
			interceptor.handleRequest(request, json, this);
		}
	}

	/**
	 * Handle the given reponse.
	 * 
	 * @param request
	 * @param response
	 * @param startTime
	 */
	private void handleResponse(Response<?> response, String json, long startTime) {
		if (interceptors == null) {
			return;
		}
		long ellapsedTime = getElapsedTimeInMs(startTime);
		for (IInterceptor interceptor : interceptors) {
			interceptor.handleResponse(response, json, ellapsedTime, this);
		}
	}

	/**
	 * Handle the given error.
	 * 
	 * @param request
	 * @param e
	 * @param startTime
	 */
	private void handleError(String command, Throwable e, long startTime) {
		if (interceptors == null) {
			return;
		}
		long ellapsedTime = getElapsedTimeInMs(startTime);
		for (IInterceptor interceptor : interceptors) {
			interceptor.handleError(e, this, command, ellapsedTime);
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
