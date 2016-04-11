package ts.nodejs;

import ts.TypeScriptException;
import ts.internal.client.protocol.Request;

public interface INodejsProcess {

	/**
	 * Joint to the stdout thread;
	 * 
	 * @throws InterruptedException
	 */
	void join() throws InterruptedException;

	/**
	 * Add the given process listener.
	 * 
	 * @param listener
	 */
	void addProcessListener(INodejsProcessListener listener);

	/**
	 * Remove the given process listener.
	 * 
	 * @param listener
	 */
	void removeProcessListener(INodejsProcessListener listener);

	/**
	 * Kill the process.
	 */
	public void kill();

	boolean isStarted();

	void start();

	void sendRequest(Request request) throws TypeScriptException;
}
