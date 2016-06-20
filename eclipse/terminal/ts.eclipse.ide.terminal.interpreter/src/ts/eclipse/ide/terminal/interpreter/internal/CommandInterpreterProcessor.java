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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.eclipse.tm.terminal.view.core.interfaces.ITerminalServiceOutputStreamMonitorListener;
import org.eclipse.tm.terminal.view.core.interfaces.constants.ITerminalsConnectorConstants;

import ts.eclipse.ide.terminal.interpreter.ICommandInterpreter;
import ts.eclipse.ide.terminal.interpreter.ICommandInterpreterFactory;

public class CommandInterpreterProcessor implements ITerminalServiceOutputStreamMonitorListener {

	private final ICommandInterpreter NULL_INTERPRETER = new ICommandInterpreter() {

		@Override
		public void execute(List<String> parameters, String workingDir) {

		}

		@Override
		public void addLine(String line) {

		}
	};
	private final Map<String, Object> properties;
	private ICommandInterpreter interpreter;

	private String workingDir;
	private String encoding;
	private String originalWorkingDir;

	// Query Cursor Position <<ESC>>[6n
	// Requests a Report Cursor Position response from the device.
	private boolean processingCommand;

	private String cmd;
	private String cmdWithParameters;
	private List<String> cmdParameters;
	private String workingDirEnd;

	public CommandInterpreterProcessor(Map<String, Object> properties) {
		this.properties = properties;
		this.processingCommand = false;
	}

	@Override
	public void onContentReadFromStream(byte[] byteBuffer, int bytesRead) {
		String encoding = getEncoding();
		LinesInfo lines = new LinesInfo(byteBuffer, bytesRead, encoding);
		if (lines.isProcessAnsiCommand_n()) {
			processingCommand = true;
		} else {
			if (processingCommand) {
				// Enter was done
				if (cmd == null) {
					endCommand();
				} else {
					// Initialize interpreter if needed
					if (interpreter == null) {
						ICommandInterpreterFactory factory = CommandInterpreterManager.getInstance().getFactory(cmd);
						if (factory != null) {
							cmdParameters = getParameters(cmdWithParameters);
							interpreter = factory.create(cmdParameters);
						}
						if (interpreter == null) {
							interpreter = NULL_INTERPRETER;
						}
					}

					List<String> l = lines.getLines();
					String lastLine = lines.getLastLine();
					if (isEndCommand(lastLine)) {
						for (int i = 0; i < l.size() - 1; i++) {
							interpreter.addLine(l.get(i));
						}
						endCommand();
						this.workingDir = lastLine;
					} else {
						for (String line : lines.getLines()) {
							interpreter.addLine(line);
						}
					}
				}
			} else {
				// Terminal was opened, get the last lines which is the working
				// dir.
				if (workingDir == null) {
					this.workingDir = lines.getLastLine();
					if (workingDir != null) {
						String originalWorkingDir = getOriginalWorkingDir();
						if (workingDir.startsWith(originalWorkingDir)) {
							this.workingDirEnd = workingDir.substring(originalWorkingDir.length(), workingDir.length());
						}
					}
				} else {
					// User is typing command.
					String lineCmd = lines.getLastLine();
					if (lineCmd != null) {
						// line cmd contains the working dir and the command
						// with parameters
						// ex: "C:\User>cd a"
						// get the command and their parameters
						this.cmdWithParameters = lineCmd.substring(workingDir.length(), lineCmd.length()).trim();
						// here cmdWithParameters is equals to "cd a"
						// get the first token to retrieve the command (ex:
						// "cd")
						this.cmd = getCmd(cmdWithParameters);
					}
				}
			}
		}
	}

	private boolean isEndCommand(String line) {
		if (line == null) {
			return false;
		}
		if (workingDir.equals(line)) {
			return true;
		}
		if (!line.endsWith(workingDirEnd)) {
			return false;
		}

		try {
			return new File(getWorkingDir(line)).exists();
		} catch (Throwable e) {
			return false;
		}
	}

	private void endCommand() {
		if (interpreter != null) {
			String dir = getWorkingDir(workingDir);
			interpreter.execute(cmdParameters, dir);
		}
		processingCommand = false;
		cmd = null;
		cmdWithParameters = null;
		cmdParameters = null;
		interpreter = null;
	}

	private String getWorkingDir(String dir) {
		return dir.substring(0, dir.length() - workingDirEnd.length());
	}

	private List<String> getParameters(String cmdWithParameters) {
		int index = cmdWithParameters.indexOf(" ");
		if (index == -1) {
			// search '.' for "cd..", "cd."
			index = cmdWithParameters.indexOf(".");
		}
		if (index == -1) {
			return Collections.emptyList();
		}
		List<String> parameters = new ArrayList<String>();
		char[] chars = cmdWithParameters.substring(index, cmdWithParameters.length()).trim().toCharArray();
		StringBuilder param = null;
		for (int i = 0; i < chars.length; i++) {
			char c = chars[i];
			switch (c) {
			case ' ':
				if (param != null) {
					parameters.add(param.toString());
				}
				param = null;
				break;
			default:
				if (param == null) {
					param = new StringBuilder();
				}
				param.append(c);
			}
		}
		if (param != null) {
			parameters.add(param.toString());
		}
		return parameters;
	}

	private String getCmd(String cmdWithParameters) {
		int index = cmdWithParameters.indexOf(" ");
		if (index == -1) {
			// search '.' for "cd..", "cd."
			index = cmdWithParameters.indexOf(".");
		}
		if (index != -1) {
			return cmdWithParameters.substring(0, index);
		}
		return cmdWithParameters;
	}

	/**
	 * Returns the encoding of the terminal.
	 * 
	 * @return the encoding of the terminal.
	 */
	private String getEncoding() {
		if (encoding != null) {
			return encoding;
		}
		encoding = (String) properties.get(ITerminalsConnectorConstants.PROP_ENCODING);
		return encoding;
	}

	/**
	 * Returns the original working directory of the terminal.
	 * 
	 * @return the original working directory of the terminal.
	 */
	private String getOriginalWorkingDir() {
		if (originalWorkingDir != null) {
			return originalWorkingDir;
		}
		originalWorkingDir = (String) properties.get(ITerminalsConnectorConstants.PROP_PROCESS_WORKING_DIR);
		return originalWorkingDir;
	}

}
