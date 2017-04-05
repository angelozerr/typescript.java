/**
 *  Copyright (c) 2015-2017 Angelo ZERR.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package ts.eclipse.ide.terminal.interpreter;

/**
 * Command interpreter listener.
 *
 */
public interface ICommandInterpreterListener extends ITerminalCommandListener{

	void onOpenTerminal(String initialWorkingDir, String initialCommand, String userHome);

	void onProcessText(String text, int columns);

	void onCarriageReturnLineFeed();

	void onContentReadFromStream(byte[] byteBuffer, int bytesRead, String encoding);

}
