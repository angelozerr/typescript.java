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

import java.util.List;

/**
 * Abstract class for {@link ICommandInterpreter}.
 *
 */
public abstract class AbstractCommandInterpreter implements ICommandInterpreter {

	private final List<String> parameters;
	private final String workingDir;

	public AbstractCommandInterpreter(List<String> parameters, String workingDir) {
		this.parameters = parameters;
		this.workingDir = workingDir;
	}

	@Override
	public void execute() {
		execute(parameters, workingDir);
	}

	@Override
	public void onTrace(String line) {
		// Do nothing
	}

	/**
	 * Execute the command interpreter with the given parameters and working
	 * directory.
	 * 
	 * @param parameters
	 * @param workingDir
	 */
	protected abstract void execute(List<String> parameters, String workingDir);

}
