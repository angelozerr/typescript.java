package ts.server.nodejs.internal.process;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;

import ts.TSException;
import ts.server.nodejs.process.AbstractNodejsProcess;
import ts.server.protocol.GeterrRequest;
import ts.server.protocol.Request;
import ts.server.protocol.Request;
import ts.server.protocol.SimpleRequest;

public class NodeJSProcess extends AbstractNodejsProcess {

	private final File tsserverFile;

	/**
	 * node.js process.
	 */
	private Process process;

	/**
	 * StdOut thread.
	 */
	private Thread outThread;

	/**
	 * StdErr thread.
	 */
	private Thread errThread;

	private PrintStream out;

	private final Map<Integer, SimpleRequest> requestsMap;
	private final Map<String, GeterrRequest> diagRequestsMap;
	private final ExecutorService pool = Executors.newFixedThreadPool(2);

	/**
	 * StdOut of the node.js process.
	 */
	private class StdOut implements Runnable {

		@Override
		public void run() {
			try {
				try {
					notifyStartProcess(0);
					BufferedReader r = new BufferedReader(new InputStreamReader(process.getInputStream()));
					String line = null;
					while ((line = r.readLine()) != null) {
						if (line.startsWith("{")) {
							dispatch(line);
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
				if (process != null) {
					process.waitFor();
				}
				kill();
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}
	}

	/**
	 * StdErr of the node.js process.
	 */
	private class StdErr implements Runnable {
		@Override
		public void run() {
			String line = null;
			InputStream is = process.getErrorStream();
			InputStreamReader isr = new InputStreamReader(is);
			BufferedReader br = new BufferedReader(isr);
			try {
				while ((line = br.readLine()) != null) {
					notifyErrorProcess(line);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public NodeJSProcess(File projectDir, File tsserverFile, File nodejsFile) throws TSException {
		super(nodejsFile, projectDir);
		this.tsserverFile = tsserverFile;
		this.requestsMap = new HashMap<Integer, SimpleRequest>();
		this.diagRequestsMap = new HashMap<String, GeterrRequest>();
	}

	public void notifyErrorProcess(String line) {
		System.err.println(line);
	}

	@Override
	public void start() {
		try {
			List<String> commands = createCommands();
			ProcessBuilder builder = new ProcessBuilder(commands);
			builder.directory(getProjectDir());

			this.process = builder.start();
			this.out = new PrintStream(process.getOutputStream());

			outThread = new Thread(new StdOut());
			outThread.setDaemon(true);
			outThread.start();

			errThread = new Thread(new StdErr());
			errThread.setDaemon(true);
			errThread.start();

			// add a shutdown hook to destroy the node process in case its not
			// properly disposed
			Runtime.getRuntime().addShutdownHook(new ShutdownHookThread());

		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean isStarted() {
		return process != null;
	}

	private class ShutdownHookThread extends Thread {
		@Override
		public void run() {
			Process process = NodeJSProcess.this.process;
			if (process != null) {
				kill();
			}
		}
	}

	/**
	 * Create process commands to start tern with node.js
	 * 
	 * @return
	 * @throws IOException
	 */
	private List<String> createCommands() {
		List<String> commands = new LinkedList<String>();
		if (nodejsFile == null) {
			// for osx, path of node.js should be setted?
			if (new File("/usr/local/bin/node").exists()) {
				commands.add("/usr/local/bin/node");
			}
			if (new File("/opt/local/bin/node").exists()) {
				commands.add("/opt/local/bin/node");
			} else {
				commands.add("node");
			}
		} else {
			commands.add(nodejsFile.getPath());
		}
		try {
			commands.add(tsserverFile.getCanonicalPath());
		} catch (IOException e) {
			commands.add(tsserverFile.getPath());
		}
		commands.add("-p");
		try {
			commands.add(getProjectDir().getCanonicalPath());
		} catch (IOException e) {
			commands.add(getProjectDir().getPath());
		}
		// commands.addAll(createTernServerArgs());
		return commands;
	}

	public File getProjectDir() {
		return projectDir;
	}

	/**
	 * Kill the process.
	 */
	public void kill() {
		if (out != null) {
			out.close();
			out = null;
		}
		if (process != null) {
			process.destroy();
			process = null;
			notifyStopProcess();
		}
		if (outThread != null) {
			outThread.interrupt();
			outThread = null;
		}
		if (errThread != null) {
			errThread.interrupt();
			errThread = null;
		}
		if (!pool.isShutdown()) {
			pool.shutdown();
		}
	}

	/**
	 * Join to the stdout thread;
	 * 
	 * @throws InterruptedException
	 */
	public void join() throws InterruptedException {
		if (outThread != null) {
			outThread.join();
		}
	}

	@Override
	public void sendRequestAsyncResponse(Request r) throws TSException {
		if (r instanceof GeterrRequest) {
			synchronized (diagRequestsMap) {
				GeterrRequest request = ((GeterrRequest) r);
				Collection<String> files = request.getFiles();
				GeterrRequest oldErrRequest = null;
				// register the GeterrRequest for each file
				for (String file : files) {
					// check if an old GeterrRequest was registered for the
					// given file
					oldErrRequest = diagRequestsMap.get(file);
					if (oldErrRequest != null) {
						oldErrRequest.dispose(file);
					}
					diagRequestsMap.put(file, request);
				}
			}
		} else {
			synchronized (requestsMap) {
				SimpleRequest request = (SimpleRequest) r;
				requestsMap.put(request.getSeq(), request);
			}
		}
		sendRequest(r);
	}

	@Override
	public void sendRequest(Request request) throws TSException {
		out.println(request); // add \n for "readline" used by tsserver
		out.flush();
	}

	@Override
	public JsonObject sendRequestSyncResponse(Request request) throws TSException {
		sendRequestAsyncResponse(request);
		Future<JsonObject> f = pool.submit(request);
		JsonObject response = null;
		try {
			response = f.get();
		} catch (Exception e) {
			if (e instanceof TSException) {
				throw (TSException) e;
			}
			throw new TSException(e);
		}
		return response;
	}

	private void dispatch(String message) {
		JsonObject response = Json.parse(message).asObject();
		String type = response.getString("type", null);

		if ("event".equals(type)) {
			String event = response.getString("event", null);
			if ("syntaxDiag".equals(event) || "semanticDiag".equals(event)) {
				JsonObject body = response.get("body").asObject();
				if (body != null) {
					String file = body.getString("file", null);
					if (file != null) {
						GeterrRequest request = null;
						synchronized (diagRequestsMap) {
							request = diagRequestsMap.get(file);
							if (request != null) {
								JsonArray diagnostics = body.get("diagnostics").asArray();
								boolean remove = request.handleResponse(event, file, diagnostics);
								if (remove) {
									diagRequestsMap.remove(file);
								}
							}
						}
					}
				}
			}
		} else if ("response".equals(type)) {
			int seq = response.getInt("request_seq", -1);
			if (seq != -1) {
				synchronized (requestsMap) {
					SimpleRequest request = requestsMap.remove(seq);
					if (request != null) {
						request.handleResponse(response);
					}
				}
			}
		}

	}

}
