package ts.server;

import ts.ICompletionInfo;
import ts.INavigationBarItem;
import ts.TSException;

public interface ITSClient {

	void openFile(String fileName) throws TSException;

	ICompletionInfo getCompletionsAtPosition(String fileName, int position) throws TSException;

	ICompletionInfo getCompletionsAtLineOffset(String fileName, int line, int offset) throws TSException;

	INavigationBarItem[] getNavigationBarItems(String fileName) throws TSException;

	void join() throws InterruptedException;
}
