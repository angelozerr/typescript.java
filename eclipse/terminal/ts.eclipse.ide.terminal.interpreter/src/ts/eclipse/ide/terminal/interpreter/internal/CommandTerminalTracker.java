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
import java.util.ArrayList;
import java.util.List;

import ts.eclipse.ide.terminal.interpreter.CommandTerminalService;
import ts.eclipse.ide.terminal.interpreter.ICommandInterpreterListener;
import ts.eclipse.ide.terminal.interpreter.ITerminalCommandListener;

/**
 * Command terminal tracker.
 *
 */
public abstract class CommandTerminalTracker extends AnsiHandler {

	private static final String TILD = "~";

	private final List<ITerminalCommandListener> listeners;

	private String workingDir;
	private final String initialCommand;

	private int columns;

	private LineCommand lineCommand;

	public CommandTerminalTracker(String initialWorkingDir, String initialCommand) {
		this.workingDir = initialWorkingDir;
		this.initialCommand = initialCommand;
		this.columns = 80;
		this.listeners = new ArrayList<>();
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
	}

	private void processLine(String line) {
		if (line == null) {
			return;
		}
		if (!tryTerminateCommand(line)) {
			tryExecutingCommand(line);
		}

		// else if (lineCommand != null) {
		// executingCommand(line, lineCommand);
		// }
	}

	private void tryExecutingCommand(String line) {
		if (lineCommand == null) {
			return;
		}
		executingCommand(line, lineCommand);
	}

	public boolean tryTerminateCommand(String line) {
		String workingDir = null;
		int length = line.length();
		char last = line.charAt(length - 1);
		if (last == '>' || last == '$') {
			workingDir = line.substring(0, length - 1);
		} else if (length >= 2 && last == ' ' && line.charAt(length - 2) == '$') {
			workingDir = line.substring(0, length - 2);
		}
		if (workingDir == null) {
			return false;
		}
		workingDir = resolveTild(workingDir);
		if (isDirectory(workingDir)) {
			terminateCommand(workingDir);
			return true;
		}
		return false;
	}

	protected boolean isDirectory(String dir) {
		return new File(dir).exists();
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

	public void addTerminalCommandListener(ITerminalCommandListener listener) {
		synchronized (listeners) {
			if (!listeners.contains(listener)) {
				listeners.add(listener);
			}
		}
	}

	public void removeTerminalCommandListener(ITerminalCommandListener listener) {
		synchronized (listeners) {
			listeners.remove(listener);
		}
	}

	private void terminateCommand(String workingDir) {
		this.workingDir = workingDir;
		if (lineCommand == null) {
			submitCommand(new LineCommand(null));
		} else {
			lineCommand.setNewWorkingDir(workingDir);
		}
		LineCommand oldlineCommand = lineCommand;
		lineCommand = null;		
		terminateCommand(oldlineCommand);
	}

	protected void terminateCommand(LineCommand lineCommand) {
		synchronized (listeners) {			
			for (ITerminalCommandListener listener : listeners) {
				listener.onTerminateCommand(lineCommand);
			}
		}
		for (ICommandInterpreterListener listener : CommandTerminalService.getInstance().getInterpreterListeners()) {
			listener.onTerminateCommand(lineCommand);
		}
	}

	protected void executingCommand(String line, LineCommand lineCommand) {
		synchronized (listeners) {
			for (ITerminalCommandListener listener : listeners) {
				listener.onExecutingCommand(line, lineCommand);
			}
		}
		for (ICommandInterpreterListener listener : CommandTerminalService.getInstance().getInterpreterListeners()) {
			listener.onExecutingCommand(line, lineCommand);
		}
	}

	protected void submitCommand(LineCommand lineCommand) {
		this.lineCommand = lineCommand;
		lineCommand.setWorkingDir(workingDir);
		synchronized (listeners) {
			for (ITerminalCommandListener listener : listeners) {
				listener.onSubmitCommand(lineCommand);
			}
		}
		for (ICommandInterpreterListener listener : CommandTerminalService.getInstance().getInterpreterListeners()) {
			listener.onSubmitCommand(lineCommand);
		}
	}
}
