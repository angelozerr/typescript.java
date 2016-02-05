package ts.server.nodejs;

import ts.TSException;
import ts.server.protocol.Request;

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

	void sendRequest(Request request) throws TSException;
}
