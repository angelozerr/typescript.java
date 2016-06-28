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
package ts.eclipse.ide.terminal.interpreter;

/**
 * Abstract class for {@link ICommandInterpreter}.
 *
 */
public abstract class AbstractCommandInterpreter implements ICommandInterpreter {

	private final String workingDir;

	public AbstractCommandInterpreter(String workingDir) {
		this.workingDir = workingDir;
	}

	public String getWorkingDir() {
		return workingDir;
	}

	@Override
	public void onTrace(String line) {
		// Do nothing
	}

	/**
	 * Execute the command interpreter.
	 * 
	 */
	public abstract void execute();

}
