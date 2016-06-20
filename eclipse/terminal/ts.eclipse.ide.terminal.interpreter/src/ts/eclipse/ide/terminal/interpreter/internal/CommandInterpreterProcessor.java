package ts.eclipse.ide.terminal.interpreter.internal;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.eclipse.tm.terminal.view.core.interfaces.ITerminalServiceOutputStreamMonitorListener;
import org.eclipse.tm.terminal.view.core.interfaces.constants.ITerminalsConnectorConstants;

import ts.eclipse.ide.terminal.interpreter.ICommandInterpreter;

public class CommandInterpreterProcessor implements ITerminalServiceOutputStreamMonitorListener {

	private final ICommandInterpreter NULL_INTERPRETER = new ICommandInterpreter() {

		@Override
		public void process(java.util.List<String> parameters, String workingDir) {

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
						interpreter = CommandInterpreterManager.getInstance().getCommand(cmd);
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
					//
					// if (lineCmd.contains("cd") || lineCmd.contains("chdir"))
					// {
					// queryCursorPosition = false;
					// System.err.println("Finish!" + lineCmd);
					// lineCmd = null;
					// workingDir = lines.getLastLine();
					// } else if (workingDir.equals(lines.getLastLine())) {
					// //
					// queryCursorPosition = false;
					// System.err.println("Finish!" + lineCmd);
					// lineCmd = null;
					// } else {
					// for (String line : lines.getLines()) {
					// System.err.println("Add to " + lineCmd + ": " + line);
					// }
					// }
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
			interpreter.process(getParameters(cmdWithParameters), dir);
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

	private String getEncoding() {
		if (encoding != null) {
			return encoding;
		}
		encoding = (String) properties.get(ITerminalsConnectorConstants.PROP_ENCODING);
		return encoding;
	}

	private String getOriginalWorkingDir() {
		if (originalWorkingDir != null) {
			return originalWorkingDir;
		}
		originalWorkingDir = (String) properties.get(ITerminalsConnectorConstants.PROP_PROCESS_WORKING_DIR);
		return originalWorkingDir;
	}

}
