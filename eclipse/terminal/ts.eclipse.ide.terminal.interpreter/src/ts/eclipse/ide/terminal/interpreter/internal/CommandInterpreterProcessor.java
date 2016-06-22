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
import ts.eclipse.ide.terminal.interpreter.internal.commands.CdCommandInterpreter;

public class CommandInterpreterProcessor implements ITerminalServiceOutputStreamMonitorListener {

	private final ICommandInterpreter NULL_INTERPRETER = new ICommandInterpreter() {

		@Override
		public void execute() {

		}

		@Override
		public void onTrace(String line) {

		}
	};
	private final Map<String, Object> properties;
	private ICommandInterpreter interpreter;

	private String lineInput;
	private String encoding;
	private String originalWorkingDir;

	// Query Cursor Position <<ESC>>[6n
	// Requests a Report Cursor Position response from the device.
	private boolean processingCommand;

	private String cmd;
	private String cmdWithParameters;
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
					endCommand(null);
				} else {
					// Initialize interpreter if needed
					if (interpreter == null) {
						ICommandInterpreterFactory factory = CommandInterpreterManager.getInstance().getFactory(cmd);
						if (factory != null) {
							String workingDir = getWorkingDir(lineInput);
							List<String> parameters = getParameters(cmdWithParameters);
							interpreter = factory.create(parameters, workingDir);
						}
						if (interpreter == null) {
							interpreter = NULL_INTERPRETER;
						}
					}

					String lastLine = lines.getLastLine();
					if (isEndCommand(lastLine)) {
						trace(lines.getLines(), 1);
						endCommand(lastLine);
					} else {
						trace(lines.getLines(), 0);
					}
				}
			} else {
				// Terminal was opened, get the last lines which is the line
				// input : "workingDir" concat with ('>' for Windows, '$' for
				// Linux)
				if (lineInput == null) {
					this.lineInput = lines.getLastLine();
					if (lineInput != null) {
						String originalWorkingDir = getOriginalWorkingDir();
						if (lineInput.startsWith(originalWorkingDir)) {
							// retrieve the character used for line input ('>'
							// for Windows, '$' for Linux)
							this.workingDirEnd = lineInput.substring(originalWorkingDir.length(), lineInput.length());
						} else {
							lineInput = null;
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
						this.cmdWithParameters = lineCmd.substring(lineInput.length(), lineCmd.length()).trim();
						// here cmdWithParameters is equals to "cd a"
						// get the first token to retrieve the command (ex:
						// "cd")
						this.cmd = getCmd(cmdWithParameters);
					}
				}
			}
		}
	}

	private void trace(List<String> lines, int index) {
		for (int i = 0; i < lines.size() - index; i++) {
			interpreter.onTrace(lines.get(i));
		}
	}

	private boolean isEndCommand(String line) {
		if (line == null) {
			return false;
		}
		if (lineInput.equals(line)) {
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

	private void endCommand(String lastLine) {
		if (interpreter != null) {
			interpreter.execute();
		}
		if (lastLine != null) {
			boolean workingDirChanged = !this.lineInput.equals(lastLine);
			if (workingDirChanged) {
				String workingDir = getWorkingDir(lineInput);
				List<String> parameters = getParameters(cmdWithParameters);
				new CdCommandInterpreter(parameters, workingDir).execute();
			}
			this.lineInput = lastLine;
		}
		processingCommand = false;
		cmd = null;
		cmdWithParameters = null;
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
