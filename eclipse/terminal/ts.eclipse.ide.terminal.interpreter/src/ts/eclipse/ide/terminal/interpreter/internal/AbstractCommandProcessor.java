/**
 *  Copyright (c) 2015-2016 Angelo ZERR.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package ts.eclipse.ide.terminal.interpreter.internal;

import java.io.File;
import java.util.List;

import org.eclipse.tm.terminal.view.core.interfaces.ITerminalServiceOutputStreamMonitorListener;

/**
 * Abstract class which observes ANSI control characters to track command
 * events:
 * 
 * <ul>
 * <li>apply enter</li>
 * <li>enter apply</li>
 * </ul>
 * 
 * @see http://www.termsys.demon.co.uk/vtansi.htm
 *
 */
public abstract class AbstractCommandProcessor implements ITerminalServiceOutputStreamMonitorListener {

	private boolean submitCommand;
	private String command;

	private String beforeWorkingDir; // with Windows it's empty
	private String afterWorkingDir; // with Windows, it's '>'
	private String workingDir; // ex : "C:\User"
	private String lineCommandWorkingDir; // ex : "C:\User>"

	public AbstractCommandProcessor() {
		this.command = null;
		this.submitCommand = false;
	}

	@Override
	public final void onContentReadFromStream(byte[] byteBuffer, int bytesRead) {
		String encoding = getEncoding();
		LinesInfo info = new LinesInfo(byteBuffer, bytesRead, encoding);
		processLines(info.getLines(), info.isProcessAnsiCommand_n());
	}

	protected void processLines(List<String> lines, boolean processAnsiCommand_n) {
		trace(lines, processAnsiCommand_n);
		if (processAnsiCommand_n) {
			// User press enter to submit command.
			this.submitCommand = true;
			extractWorkingDirOfLineCommand(lines);
		} else {
			String newWorkingDir = extractWorkingDirOfLineCommand(lines);
			if (this.submitCommand) {
				// User has pressed enter to submit line command.
				if (command == null) {
					// command was not tracked.
					if (newWorkingDir != null) {
						// it's the end line command, terminate the command.
						endCommand(null);
					}
				} else {
					// String workingDir = getWorkingDir();
					String command = getCommand();
					processingCommand(newWorkingDir != null ? newWorkingDir : workingDir, command, lines);
					if (isEndCommand(newWorkingDir)) {
						endCommand(newWorkingDir);
					}
				}
			}
		}
	}

	private void trace(List<String> lines, boolean processAnsiCommand_n) {
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
		code.append(processAnsiCommand_n);
		code.append(");");
		System.err.println(code.toString());
	}

	protected abstract void processingCommand(String workingDir, String command, List<String> lines);

	private boolean isEndCommand(String newWorkingDir) {
		if (newWorkingDir == null) {
			return false;
		}
		if (newWorkingDir.startsWith(workingDir)) {
			return true;
		}
		// In the case when command changes the working dir (ex : cd ..)
		return new File(newWorkingDir).exists();
	}

	protected void endCommand(String newWorkingDir) {
		if (newWorkingDir != null) {
			// Check if working dir has changed.
			String oldWorkingDir = this.getWorkingDir();
			boolean workingDirChanged = !oldWorkingDir.equals(newWorkingDir);
			if (workingDirChanged) {
				// Working dir has changed, update it.
				setNewWorkingDir(newWorkingDir, command);
			}
		}
		submitCommand = false;
		command = null;
	}

	protected void setNewWorkingDir(String newWorkingDir, String command) {
		setWorkingDir(newWorkingDir);
	}

	/**
	 * Returns the working dir of the given end line command (which doesn't
	 * contains a command to execute) and null otherwise.
	 * 
	 * @param lines
	 * @return the working dir of the given end line command (which doesn't
	 *         contains a command to execute) and null otherwise.
	 */
	private String extractWorkingDirOfLineCommand(List<String> lines) {
		if (lines.isEmpty()) {
			// None lines, it's not a line command
			return null;
		}
		String line = null;
		String newWorkingDir = null;
		String command = null;
		// Loop for each lines starting with last line.
		// we need to loop for the whole lines, in the case of start an dend of
		// line command are in the same lines list.
		for (int i = lines.size() - 1; i >= 0; i--) {
			line = lines.get(i);
			String workingDir = extractWorkingDir(line);
			if (workingDir != null) {
				// it's a line command:
				// - start of line command: "C:\User>cd a"
				// - or end of line command: "C:\User>" (after command was
				// executed)
				// try to extract the command to interpret
				command = extractCommand(line);
				if (command == null) {
					// it's end of line command
					if (this.command != null) {
						// command was already getted, returns the working dir
						return workingDir;
					} else if (newWorkingDir == null) {
						// store the working dir and don't return it in order to
						// try to track command
						newWorkingDir = workingDir;
					}
				} else {
					this.command = command;
					// it's start of line command
					return null;
				}
			} else {
				if (line != null && beforeWorkingDir != null && line.startsWith(beforeWorkingDir)) {
					int index = line.indexOf(afterWorkingDir);
					if (index != -1) {
						line = line.substring(beforeWorkingDir.length(), index);
						if (new File(line).exists()) {
							return line;
						}
					}
				}
			}
		}
		return newWorkingDir;
	}

	private String extractCommand(String line) {
		if (line.length() > lineCommandWorkingDir.length()) {
			return line.substring(lineCommandWorkingDir.length(), line.length()).trim();
		}
		return null;
	}

	/**
	 * Return true if the given line is a line command and false otherwise.
	 * 
	 * A line command looks like "C:\User>" or "C:\User>cd a"
	 * 
	 * @param line
	 * @return true if the given line is a line command and false otherwise.
	 */
	private String extractWorkingDir(String line) {
		if (line == null) {
			return null;
		}
		// line command working dir is the working dir concat with some token
		// (ex: '>' for Windows => C:\User>)
		if (lineCommandWorkingDir == null) {
			String initialWorkingDir = getInitialWorkingDir();
			int index = line.indexOf(initialWorkingDir);
			if (index == -1) {
				return null;
			}
			// line contains working dir, compute line command working dir
			this.beforeWorkingDir = line.substring(0, index);

			int initialCommandIndex = -1;
			String initialCommand = getInitialCommand();
			if (initialCommand != null) {
				initialCommandIndex = line.indexOf(initialCommand);
			}
			this.afterWorkingDir = line.substring(index + initialWorkingDir.length(),
					(initialCommandIndex != -1 ? initialCommandIndex : line.length()));
			setWorkingDir(initialWorkingDir);
			return workingDir;
		}
		if (line.startsWith(lineCommandWorkingDir)) {
			return workingDir;
		}
		return null;
	}

	private void setWorkingDir(String workingDir) {
		this.workingDir = workingDir;
		this.lineCommandWorkingDir = beforeWorkingDir + workingDir + afterWorkingDir;
	}

	protected abstract String getInitialWorkingDir();

	protected abstract String getInitialCommand();

	/**
	 * Returns the encoding of the terminal.
	 * 
	 * @return the encoding of the terminal.
	 */
	protected abstract String getEncoding();

	public String getWorkingDir() {
		return workingDir;
	}

	public String getCommand() {
		return command;
	}
}
