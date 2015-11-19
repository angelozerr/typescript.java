/**
 *  Copyright (c) 2013-2015 Angelo ZERR.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package ts.server;

import ts.ICompletionCollector;
import ts.ICompletionInfo;
import ts.INavigationBarItem;
import ts.TSException;

/**
 * TypeScript client API which communicates with tsserver.
 * 
 * @see https://github.com/Microsoft/TypeScript/blob/master/src/server/client.ts
 *
 */
public interface ITypeScriptServiceClient {

	void openFile(String fileName) throws TSException;

	void closeFile(String fileName) throws TSException;

	void updateFile(String fileName, String newText) throws TSException;

	void completions(String fileName, int line, int offset, String prefix, ICompletionCollector collector)
			throws TSException;

	void changeFile(String fileName, int start, int end, String newText) throws TSException;

	void changeFile(String fileName, int line, int offset, int endLine, int endOffset, String newText)
			throws TSException;

	ICompletionInfo getCompletionsAtPosition(String fileName, int position, String prefix) throws TSException;

	ICompletionInfo getCompletionsAtLineOffset(String fileName, int line, int offset, String prefix) throws TSException;

	INavigationBarItem[] getNavigationBarItems(String fileName) throws TSException;

	void join() throws InterruptedException;

	void dispose();

}
