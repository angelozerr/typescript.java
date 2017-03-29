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

import ts.eclipse.ide.terminal.interpreter.CommandTerminalService;
import ts.eclipse.ide.terminal.interpreter.ICommandInterpreterListener;

/**
 * Command terminal tracker.
 *
 */
public abstract class CommandTerminalTracker extends AnsiHandler {

	private static final String TILD = "~";

	private final String initialWorkingDir;
	private final String initialCommand;
	private LineCommand lineCommand;

	private String currentText;
	private int columns;

	public CommandTerminalTracker(String initialWorkingDir, String initialCommand) {
		this.initialWorkingDir = initialWorkingDir;
		this.initialCommand = initialCommand;
		this.columns = 80;
		onOpenTerminal(initialWorkingDir, initialCommand, getUserHome());
	}
	
	@Override
	public synchronized void parse(byte[] byteBuffer, int bytesRead, String encoding) {
		onContentReadFromStream(byteBuffer, bytesRead, encoding);
		super.parse(byteBuffer, bytesRead, encoding);
	}

	@Override
	protected void processText(String text) {
		processText(text, columns);
	}

	protected void processText(String text, int columns) {
		onProcessText(text, columns);
		processLine(text);
	}

	@Override
	protected void processCarriageReturnLineFeed() {
		// CRLF is thrown when:
		// - User types 'Enter' to submit a command.
		// - OR in Windows OS, when a new line is displayed in the DOS Command.
		onCarriageReturnLineFeed();
		if (lineCommand == null) {
			// Line command was not found in the terminal, ignore the CR event.
			return;
		}
		if (lineCommand.isSubmitted()) {
			// Line command is already submitted, ignore the CR event.
			return;
		}
		if (currentText != null) {
			return;
		}
		// Submit the command.
		lineCommand.submit();
	}

	private void processLine(String line) {
		if (line == null) {
			return;
		}
		if (line.length() >= columns - 1) {
			if (currentText == null) {
				currentText = "";
			}
			currentText += line; //rtrim(line);
			return;
		}
		if (currentText != null) {
			line = currentText + line; //rtrim(line);
			currentText = null;
		}
		if (lineCommand == null) {
			// search line command from the initial workingDir and command
			lineCommand = tryToCreateLineCommand(line, initialWorkingDir, initialCommand);
		} else {
			// Line command is initialized, update it.
			lineCommand.update(line);
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
			this.state = LineCommandState.INITIALIZED;
		}

		public boolean isSubmitted() {
			return this.state == LineCommandState.SUBMITTED;
		}

		public void submit() {
			try {
				// Fire submit command Event.
				CommandTerminalTracker.this.submitCommand(this);
			} catch (Throwable e) {
				e.printStackTrace();
			} finally {
				this.state = LineCommandState.SUBMITTED;
			}
		}

		public void update(String line) {
			if (!updateLineCommand(line)) {
				this.executing(line);
			}
		}

		private void executing(String line) {
			try {
				CommandTerminalTracker.this.executingCommand(line, this);
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}

		private void terminate() {
			try {
				CommandTerminalTracker.this.terminateCommand(this);
				this.workingDir = this.newWorkingDir;
				command = null;
				currentText = null;
			} finally {
				state = LineCommandState.INITIALIZED;
			}
		}

		private boolean updateLineCommand(String line) {
			int index = getWorkingDirIndex(line);
			if (index == -1) {
				if (state != LineCommandState.SUBMITTED) {
					this.command = command + line;
				}
				return false;
			}
			String workinDir = line.substring(beforeWorkingDir.length(), index);
			if (!(new File(workinDir).exists() || workinDir.startsWith(TILD))) {
				return false;
			}
			if (state == LineCommandState.SUBMITTED) {
				// Case when command is terminated.
				this.newWorkingDir = resolveTild(workinDir);
				this.terminate();
			} else {
				// Case when user is typing a command, update it.
				String command = line.substring(index + 1, line.length());
				this.command = command.trim();
			}
			return true;
		}

		private int getWorkingDirIndex(String line) {
			if (!line.startsWith(beforeWorkingDir)) {
				return -1;
			}
			return line.indexOf(afterWorkingDir);
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

	}

	private LineCommand tryToCreateLineCommand(String line, String initialWorkingDir, String initialCommand) {
		if (line == null) {
			return null;
		}
		line = resolveTild(line);
		int index = line.indexOf(initialWorkingDir);
		if (index == -1) {
			index = line.indexOf(initialWorkingDir.replaceAll("[\\\\]", "/"));
			if (index == -1) {
				return null;
			}
		}
		// line contains working dir, compute line command working dir
		String beforeWorkingDir = line.substring(0, index);
		int initialCommandIndex = -1;
		if (initialCommand != null) {
			// If initial command was set, it should be retrieve from the line.
			initialCommandIndex = line.indexOf(initialCommand);
			if (initialCommandIndex == -1) {
				return null;
			}
		}
		String afterWorkingDir = line.substring(index + initialWorkingDir.length(),
				(initialCommandIndex != -1 ? initialCommandIndex : line.length())).trim();
		return new LineCommand(initialWorkingDir, initialCommand, beforeWorkingDir, afterWorkingDir);
	}

	private String resolveTild(String line) {
		if (line.contains(TILD)) {
			String home = getUserHome();
			return line.replaceFirst("^~", home);
		}
		return line;
	}

	protected String getUserHome() {
		String home = System.getProperty("user.home");
		if (File.separatorChar == '\\') {
			// Windows OS with win-bash
			home = home.replaceAll("[\\\\]", "/");
		}
		return home;
	}

	// ---------------------- Trace to generate JUnit

	private void onOpenTerminal(String initialWorkingDir, String initialCommand, String userHome) {
		for (ICommandInterpreterListener listener : CommandTerminalService.getInstance().getInterpreterListeners()) {
			listener.onOpenTerminal(initialWorkingDir, initialCommand, userHome);
		}
	}
	
	private void onContentReadFromStream(byte[] byteBuffer, int bytesRead, String encoding) {
		for (ICommandInterpreterListener listener : CommandTerminalService.getInstance().getInterpreterListeners()) {
			listener.onContentReadFromStream(byteBuffer, bytesRead, encoding);
		}
	}

	private void onProcessText(String text, int columns) {
		for (ICommandInterpreterListener listener : CommandTerminalService.getInstance().getInterpreterListeners()) {
			listener.onProcessText(text, columns);
		}
	}

	private void onCarriageReturnLineFeed() {
		for (ICommandInterpreterListener listener : CommandTerminalService.getInstance().getInterpreterListeners()) {
			listener.onCarriageReturnLineFeed();
		}
	}

	public void setColumns(int columns) {
		this.columns = columns;
	}

	private static String rtrim(String s) {
		int i = s.length() - 1;
		while (i >= 0 && Character.isWhitespace(s.charAt(i))) {
			i--;
		}
		return s.substring(0, i + 1);
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
