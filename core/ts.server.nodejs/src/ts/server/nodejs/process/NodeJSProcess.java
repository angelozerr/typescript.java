package ts.server.nodejs.process;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;

import com.eclipsesource.json.JsonObject;

public class NodeJSProcess {

	private final File projectDir;
	private final File tsserverFile;
	private final File nodejsFile;

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

	/**
	 * StdOut of the node.js process.
	 */
	private class StdOut implements Runnable {

		@Override
		public void run() {
			try {
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
		}
	}

	private void dispatch(String input) {
		System.err.println(input);
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

	public NodeJSProcess(File projectDir, File tsserverFile, File nodejsFile) {
		this.projectDir = projectDir;
		this.tsserverFile = tsserverFile;
		this.nodejsFile = nodejsFile;
	}

	public void notifyErrorProcess(String line) {
		System.err.println(line);
	}

	public void start() {
		try {
			List<String> commands = createCommands();
			ProcessBuilder builder = new ProcessBuilder(commands);
			// builder.redirectErrorStream(true);
			builder.directory(getProjectDir());

			this.process = builder.start();

			outThread = new Thread(new StdOut());
			outThread.setDaemon(true);
			outThread.start();

			errThread = new Thread(new StdErr());
			errThread.setDaemon(true);
			errThread.start();

		} catch (Throwable e) {
			e.printStackTrace();
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
		// commands.addAll(createTernServerArgs());
		return commands;
	}

	public File getProjectDir() {
		return projectDir;
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

	public void writeMessage(JsonObject data) throws IOException {
		OutputStream out = this.process.getOutputStream();
		out.write(data.toString().getBytes());
		out.write("\n".getBytes()); // ad\n for "readline"
		out.flush();
	}
}
