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
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.eclipse.tm.terminal.view.core.interfaces.constants.ITerminalsConnectorConstants;

import ts.eclipse.ide.terminal.interpreter.ICommandInterpreter;
import ts.eclipse.ide.terminal.interpreter.ICommandInterpreterFactory;
import ts.eclipse.ide.terminal.interpreter.ICommandTerminalServiceConstants;
import ts.eclipse.ide.terminal.interpreter.internal.commands.CdCommandInterpreterFactory;

public class CommandInterpreterProcessor extends AbstractCommandProcessor {

	private final ICommandInterpreter NULL_INTERPRETER = new ICommandInterpreter() {

		@Override
		public void execute() {

		}

		@Override
		public void onTrace(String line) {

		}
	};

	private final Map<String, Object> properties;
	private ITerminalConnectorWrapper connector;

	private ICommandInterpreter interpreter;

	public CommandInterpreterProcessor(Map<String, Object> properties) {
		this.properties = properties;
		this.connector = null;
	}

	@Override
	protected String getInitialCommand() {
		if (connector != null) {
			return connector.getCommand();
		}
		return (String) properties.get(ICommandTerminalServiceConstants.COMMAND_ID);
	}

	@Override
	protected String getEncoding() {
		if (connector != null) {
			return connector.getEncoding();
		}
		return (String) properties.get(ITerminalsConnectorConstants.PROP_ENCODING);
	}

	/**
	 * Returns the initial working directory of the terminal.
	 * 
	 * @return the initial working directory of the terminal.
	 */
	@Override
	protected String getInitialWorkingDir() {
		if (connector != null) {
			return connector.getWorkingDir();
		}
		return (String) properties.get(ITerminalsConnectorConstants.PROP_PROCESS_WORKING_DIR);
	}

	public void setConnector(ITerminalConnectorWrapper connector) {
		this.connector = connector;
	}

	@Override
	protected void endCommand(String newWorkingDir) {
		if (interpreter != null) {
			interpreter.execute();
		}
		interpreter = null;
		super.endCommand(newWorkingDir);
	}

	@Override
	protected void processingCommand(String workingDir, String command, List<String> lines) {
		initializeInterpreter(workingDir, command);
		for (int i = 0; i < lines.size(); i++) {
			interpreter.onTrace(lines.get(i));
		}
	}

	/**
	 * Initialize interpreter if needed.
	 * 
	 * @param workingDir
	 */
	private void initializeInterpreter(String workingDir, String command) {
		// Initialize interpreter if needed
		if (interpreter == null) {
			String cmd = getCmd(command);
			ICommandInterpreterFactory factory = CommandInterpreterManager.getInstance().getFactory(cmd);
			if (factory != null) {
				List<String> parameters = getParameters(command);
				interpreter = factory.create(parameters, workingDir);
			}
			if (interpreter == null) {
				interpreter = NULL_INTERPRETER;
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
