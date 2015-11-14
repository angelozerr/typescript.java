package ts.server;

import ts.ICompletionInfo;
import ts.INavigationBarItem;
import ts.TSException;

/**
 * TypeScript client API which communicate with tsserver.
 * 
 * @see https://github.com/Microsoft/TypeScript/blob/master/src/server/client.ts
 *
 */
public interface ITSClient {

	void openFile(String fileName) throws TSException;

	void changeFile(String fileName, int start, int end, String newText) throws TSException;

	void changeFile(String fileName, int line, int offset, int endLine, int endOffset, String newText)
			throws TSException;

	ICompletionInfo getCompletionsAtPosition(String fileName, int position) throws TSException;

	ICompletionInfo getCompletionsAtLineOffset(String fileName, int line, int offset) throws TSException;

	INavigationBarItem[] getNavigationBarItems(String fileName) throws TSException;

	void join() throws InterruptedException;

	void dispose();
}
