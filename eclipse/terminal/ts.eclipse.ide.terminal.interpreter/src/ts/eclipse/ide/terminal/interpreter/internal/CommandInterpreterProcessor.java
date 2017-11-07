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

import java.util.Map;

import org.eclipse.tm.terminal.view.core.interfaces.ITerminalServiceOutputStreamMonitorListener;
import org.eclipse.tm.terminal.view.core.interfaces.constants.ITerminalsConnectorConstants;

import ts.eclipse.ide.terminal.interpreter.ICommandInterpreter;
import ts.eclipse.ide.terminal.interpreter.LineCommand;

public class CommandInterpreterProcessor extends CommandTerminalTracker
		implements ITerminalServiceOutputStreamMonitorListener {

	private ICommandInterpreter interpreter;
	private final String encoding;

	public CommandInterpreterProcessor(Map<String, Object> properties) {
		this.encoding = getInitialEncoding(properties);
	}

	@Override
	public final void onContentReadFromStream(byte[] byteBuffer, int bytesRead) {
		super.parse(byteBuffer, bytesRead, encoding);
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
	public void submitCommand(LineCommand lineCommand) {
		super.submitCommand(lineCommand);
		initializeInterpreter(lineCommand.getWorkingDir(), lineCommand.getCommand());
	}

	@Override
	protected void executingCommand(String line, LineCommand lineCommand) {
		super.executingCommand(line, lineCommand);
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
		// Execute here ancestor terminateCommand command which can executes an
		// another command.
		super.terminateCommand(lineCommand);
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
				interpreter = CommandInterpreterManager.getInstance().createInterpreter(command.trim(), workingDir);				
			}
		}
	}

}
