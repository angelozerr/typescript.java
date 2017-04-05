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

/**
 * Terminal line command.
 *
 */
public class LineCommand {

	private String workingDir;
	private String command;
	private String newWorkingDir;

	public LineCommand(String command) {
		this.command = command;
	}

	public String getWorkingDir() {
		return workingDir;
	}

	public void setWorkingDir(String workingDir) {
		this.workingDir = workingDir;
	}

	public String getNewWorkingDir() {
		return newWorkingDir;
	}

	public void setNewWorkingDir(String newWorkingDir) {
		this.newWorkingDir = newWorkingDir;
	}

	public String getCommand() {
		return command;
	}

	@Override
	public String toString() {
		StringBuilder s = new StringBuilder();
		s.append("workingDir: \"");
		s.append(workingDir);
		s.append("\"");
		s.append(", ");
		s.append("newWorkingDir: \"");
		s.append(newWorkingDir);
		s.append("\"");
		s.append(", ");
		s.append("command: \"");
		s.append(command);
		s.append("\"");
		s.append("}");
		return s.toString();
	}
}
