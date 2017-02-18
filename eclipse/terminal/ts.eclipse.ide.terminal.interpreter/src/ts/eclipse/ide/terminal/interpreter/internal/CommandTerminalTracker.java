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
package ts.eclipse.ide.terminal.interpreter.internal;

import java.io.File;
import java.util.List;

/**
 * Command terminal tracker.
 *
 */
public abstract class CommandTerminalTracker {

	private final String initialWorkingDir;
	private final String initialCommand;

	private boolean commandToSubmit;
	private LineCommand lineCommand;

	public CommandTerminalTracker(String initialWorkingDir, String initialCommand) {
		this.initialWorkingDir = initialWorkingDir;
		this.initialCommand = initialCommand;
	}

	public void processLines(List<String> lines, boolean processAnsiCommand_n) {
		// trace(lines, processAnsiCommand_n);
		if (processAnsiCommand_n) {
			this.commandToSubmit = true;
		}
		for (String line : lines) {
			processLine(line);
		}
		submitIfNeeded();
	}

	private void submitIfNeeded() {
		if (commandToSubmit && lineCommand != null && lineCommand.hasCommand()) {
			lineCommand.submit();
		}
	}

	private void processLine(String line) {
		if (line == null) {
			return;
		}
		if (lineCommand == null) {
			// search line command from the initial workingDir and command
			lineCommand = tryToCreateLineCommand(line, initialWorkingDir, initialCommand);
		} else {
			lineCommand.update(line);
		}
	}

	private void trace(List<String> lines, boolean commandToSubmit) {
		StringBuilder code = new StringBuilder("test.processLines(");
		code.append("Arrays.asList(");
		boolean empty = true;
		for (String line : lines) {
			if (!empty) {
				code.append(", ");
			}
			code.append("\"");
			code.append(line.replaceAll("[\"]", "\\\"").replaceAll("\\\\", "\\\\\\\\"));
			code.append("\"");
			empty = false;
		}
		code.append(")");
		code.append(", ");
		code.append(commandToSubmit);
		code.append(");");
		System.err.println(code.toString());
	}

	private void doSubmitCommand(LineCommand lineCommand) {
		try {
			submitCommand(lineCommand);
		} finally {
			commandToSubmit = false;
		}
	}

	private enum LineCommandState {
		INITIALIZED, SUBMITTED, TERMINATED;
	}

	public class LineCommand

	{
		private final String beforeWorkingDir;
		private final String afterWorkingDir;
		private LineCommandState state;
		private String workingDir;
		private String command;
		private String newWorkingDir;

		LineCommand(String workingDir, String command, String beforeWorkingDir, String afterWorkingDir) {
			this.workingDir = workingDir;
			this.command = command;
			this.beforeWorkingDir = beforeWorkingDir;
			this.afterWorkingDir = afterWorkingDir;
			this.state = state.INITIALIZED;
		}

		public void submit() {
			if (hasCommand()) {
				CommandTerminalTracker.this.doSubmitCommand(this);
				this.state = LineCommandState.SUBMITTED;
			}
		}

		public String getWorkingDir() {
			return workingDir;
		}

		public String getNewWorkingDir() {
			return newWorkingDir;
		}

		public String getCommand() {
			return command;
		}

		public boolean hasCommand() {
			return command != null && command.length() > 0;
		}

		public void update(String line) {
			if (updateLineCommand(line)) {
				// It's a line command
				if (state == LineCommandState.SUBMITTED) {
					this.terminate();
				}
			} else {
				if (state == LineCommandState.SUBMITTED) {
					this.executing(line);
				}
			}
		}

		private void executing(String line) {
			CommandTerminalTracker.this.executingCommand(line, this);
		}

		private void terminate() {
			try {
				CommandTerminalTracker.this.terminateCommand(this);
				this.workingDir = this.newWorkingDir;
				this.command = null;
			} finally {
				state = LineCommandState.INITIALIZED;
			}
		}

		private boolean updateLineCommand(String line) {
			if (!line.startsWith(beforeWorkingDir)) {
				return false;
			}
			int index = line.indexOf(afterWorkingDir);
			if (index == -1) {
				return false;
			}
			String workinDir = line.substring(beforeWorkingDir.length(), index);
			if (!new File(workinDir).exists()) {
				return false;
			}
			String command = line.substring(index + 1, line.length());
			if (state == LineCommandState.SUBMITTED) {
				// Case when command is terminated.
				this.newWorkingDir = workinDir;
			} else {
				// Case when user is typing a command.
				this.command = command;
			}
			return true;
		}
	}

	private LineCommand tryToCreateLineCommand(String line, String initialWorkingDir, String initialCommand) {
		if (line == null) {
			return null;
		}
		int index = line.indexOf(initialWorkingDir);
		if (index == -1) {
			return null;
		}
		// line contains working dir, compute line command working dir
		String beforeWorkingDir = line.substring(0, index);
		int initialCommandIndex = -1;
		if (initialCommand != null) {
			initialCommandIndex = line.indexOf(initialCommand);
		}
		String afterWorkingDir = line.substring(index + initialWorkingDir.length(),
				(initialCommandIndex != -1 ? initialCommandIndex : line.length()));
		return new LineCommand(initialWorkingDir, initialCommand, beforeWorkingDir, afterWorkingDir);
	}

	/**
	 * Call when a line command was submitted.
	 * 
	 * @param lineCommand
	 */
	protected abstract void submitCommand(LineCommand lineCommand);

	/**
	 * Call when a line command is executing.
	 * 
	 * @param line
	 * @param lineCommand
	 */
	protected abstract void executingCommand(String line, LineCommand lineCommand);

	/**
	 * Call when a line command is terminated.
	 * 
	 * @param lineCommand
	 */
	protected abstract void terminateCommand(LineCommand lineCommand);
}
