package ts.nodejs;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import ts.TypeScriptException;

public abstract class AbstractNodejsProcess implements INodejsProcess {

	/**
	 * The node.js base dir.
	 */
	protected final File nodejsFile;

	/**
	 * The project dir where tsconfig.json is hosted.
	 */
	protected final File projectDir;

	private final INodejsLaunchConfiguration launchConfiguration;

	/**
	 * Process listeners.
	 */
	protected final List<INodejsProcessListener> listeners;

	private boolean hasError;

	public AbstractNodejsProcess(File nodejsFile, File projectDir) throws TypeScriptException {
		this(nodejsFile, projectDir, null);
	}

	/**
	 * Nodejs process constructor.
	 * 
	 * @param nodejsFile
	 *            the node.exe file.
	 * @param projectDir
	 *            the project base dir where tsconfig.json is hosted.
	 * @throws TernException
	 */
	public AbstractNodejsProcess(File nodejsFile, File projectDir, INodejsLaunchConfiguration launchConfiguration)
			throws TypeScriptException {
		this.projectDir = projectDir;
		this.nodejsFile = nodejsFile;
		this.listeners = new ArrayList<INodejsProcessListener>();
		this.hasError = false;
		this.launchConfiguration = launchConfiguration;
	}

	protected List<String> createNodeArgs() {
		if (launchConfiguration == null) {
			return null;
		}
		return launchConfiguration.createNodeArgs();
	}

	/**
	 * return the project dir where tsconfig.json is hosted.
	 * 
	 * @return
	 */
	public File getProjectDir() {
		return projectDir;
	}

	/**
	 * Add the given process listener.
	 * 
	 * @param listener
	 */
	public void addProcessListener(INodejsProcessListener listener) {
		synchronized (listeners) {
			listeners.add(listener);
		}
	}

	/**
	 * Remove the given process listener.
	 * 
	 * @param listener
	 */
	public void removeProcessListener(INodejsProcessListener listener) {
		synchronized (listeners) {
			listeners.remove(listener);
		}
	}

	/**
	 * Notify start process.
	 */
	protected void notifyCreateProcess(List<String> commands, File projectDir) {
		synchronized (listeners) {
			for (INodejsProcessListener listener : listeners) {
				listener.onCreate(this, commands, projectDir);
			}
		}
	}

	/**
	 * Notify start process.
	 * 
	 * @param startTime
	 *            time when node.js process is started.
	 */
	protected void notifyStartProcess(long startTime) {
		synchronized (listeners) {
			for (INodejsProcessListener listener : listeners) {
				listener.onStart(this);
			}
		}
	}

	/**
	 * Notify stop process.
	 */
	protected void notifyStopProcess() {
		synchronized (listeners) {
			for (INodejsProcessListener listener : listeners) {
				listener.onStop(this);
			}
		}
	}

	/**
	 * Notify data process.
	 * 
	 * @param jsonObject
	 */
	protected void notifyMessage(String message) {
		synchronized (listeners) {
			for (INodejsProcessListener listener : listeners) {
				listener.onMessage(this, message);
			}
		}
	}

	/**
	 * Notify error process.
	 */
	protected void notifyErrorProcess(String line) {
		this.hasError = true;
		synchronized (listeners) {
			for (INodejsProcessListener listener : listeners) {
				listener.onError(AbstractNodejsProcess.this, line);
			}
		}
	}

}
