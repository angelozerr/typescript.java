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
 * Command interpreter API.
 *
 */
public interface ICommandInterpreter {

	/**
	 * Execute the command.
	 */
	void execute();

	/**
	 * Call when apply of command trace some logs in the shell.
	 * 
	 * @param trace
	 */
	void onTrace(String trace);

}
