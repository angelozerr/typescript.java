/**
 *  Copyright (c) 2015-2017 Angelo ZERR.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 *  
 */
package ts.eclipse.ide.terminal.interpreter;

import ts.eclipse.ide.terminal.interpreter.internal.CommandTerminalTracker;

/**
 * Command tracker test.
 *
 */
public class TrackerTest extends CommandTerminalTracker {

	private final StringBuilder result;

	public TrackerTest(String initialWorkingDir, String initialCommand) {
		super(initialWorkingDir, initialCommand);
		this.result = new StringBuilder();
	}

	@Override
	public void processText(String text) {
		super.processText(text);
	}

	@Override
	protected void processCarriageReturnLineFeed() {
		super.processCarriageReturnLineFeed();
	}

	@Override
	protected void submitCommand(LineCommand lineCommand) {
		write("SUBMIT: workingDir=" + lineCommand.getWorkingDir() + ", command=" + lineCommand.getCommand());
	}

	private void write(String value) {
		if (result.length() > 0) {
			result.append("\n");
		}
		result.append(value);
	}

	@Override
	protected void executingCommand(String line, LineCommand lineCommand) {
		write("EXECUTING:" + line);
	}

	@Override
	protected void terminateCommand(LineCommand lineCommand) {
		write("TERMINATE: workingDir=" + lineCommand.getNewWorkingDir() + ", command=" + lineCommand.getCommand());
	}

	@Override
	public String toString() {
		return result.toString();
	}
}
