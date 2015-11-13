package ts.server;

import ts.CompletionInfo;
import ts.NavigationBarItem;
import ts.TSException;

public interface ITSClient {

	void openFile(String fileName) throws TSException;

	CompletionInfo getCompletionsAtPosition(String fileName, int position) throws TSException;

	CompletionInfo getCompletionsAtLineOffset(String fileName, int line, int offset) throws TSException;

	NavigationBarItem[] getNavigationBarItems(String fileName) throws TSException;

	void join() throws InterruptedException;
}
