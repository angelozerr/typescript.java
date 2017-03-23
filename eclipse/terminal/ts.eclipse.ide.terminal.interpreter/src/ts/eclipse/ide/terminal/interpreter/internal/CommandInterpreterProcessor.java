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
package ts.eclipse.ide.terminal.interpreter.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.eclipse.tm.terminal.view.core.interfaces.ITerminalServiceOutputStreamMonitorListener;
import org.eclipse.tm.terminal.view.core.interfaces.constants.ITerminalsConnectorConstants;

import ts.eclipse.ide.terminal.interpreter.ICommandInterpreter;
import ts.eclipse.ide.terminal.interpreter.ICommandInterpreterFactory;
import ts.eclipse.ide.terminal.interpreter.ICommandTerminalServiceConstants;

public class CommandInterpreterProcessor extends CommandTerminalTracker
		implements ITerminalServiceOutputStreamMonitorListener {

	private ICommandInterpreter interpreter;
	private final String encoding;

	public CommandInterpreterProcessor(Map<String, Object> properties) {
		super(getInitialWorkingDir(properties), getInitialCommand(properties));
		this.encoding = getInitialEncoding(properties);
	}

	@Override
	public final void onContentReadFromStream(byte[] byteBuffer, int bytesRead) {
		super.parse(byteBuffer, bytesRead, encoding);
	}

	/**
	 * Returns the initial working directory when terminal is opened.
	 * 
	 * @param properties
	 * @return the initial working directory when terminal is opened.
	 */
	private static String getInitialWorkingDir(Map<String, Object> properties) {
		return (String) properties.get(ITerminalsConnectorConstants.PROP_PROCESS_WORKING_DIR);
	}

	/**
	 * Returns the initial command when terminal is opened.
	 * 
	 * @param properties
	 * @return the initial command when terminal is opened.
	 */
	private static String getInitialCommand(Map<String, Object> properties) {
		Object command = properties.get(ICommandTerminalServiceConstants.COMMAND_ID);
		if (command instanceof Collection) {
			Collection<String> commands = (Collection<String>) command;
			for (String cmd : commands) {
				return cmd;
			}
		}
		return (String) command;
	}

	/**
	 * Returns the initial encoding when terminal is opened.
	 * 
	 * @param properties
	 * @return the initial encoding when terminal is opened.
	 */
	private static String getInitialEncoding(Map<String, Object> properties) {
		return (String) properties.get(ITerminalsConnectorConstants.PROP_ENCODING);
	}

	@Override
	protected void submitCommand(LineCommand lineCommand) {
		initializeInterpreter(lineCommand.getWorkingDir(), lineCommand.getCommand());
	}

	@Override
	protected void executingCommand(String line, LineCommand lineCommand) {
		if (interpreter != null) {
			interpreter.onTrace(line);
		}
	}

	@Override
	protected void terminateCommand(LineCommand lineCommand) {
		if (interpreter != null) {
			interpreter.execute(lineCommand.getNewWorkingDir());
		}
		interpreter = null;
	}

	/**
	 * Initialize interpreter if needed.
	 * 
	 * @param workingDir
	 */
	private void initializeInterpreter(String workingDir, String command) {
		// Initialize interpreter if needed
		if (interpreter == null) {
			if (command != null) {
				String cmd = getCmd(command);
				ICommandInterpreterFactory factory = CommandInterpreterManager.getInstance().getFactory(cmd);
				if (factory != null) {
					List<String> parameters = getParameters(command);
					interpreter = factory.create(parameters, workingDir);
				}
			}
		}
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
}
