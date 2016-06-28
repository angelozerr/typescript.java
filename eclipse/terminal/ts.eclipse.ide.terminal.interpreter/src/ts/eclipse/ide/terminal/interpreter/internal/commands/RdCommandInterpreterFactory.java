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
package ts.eclipse.ide.terminal.interpreter.internal.commands;

import java.util.List;

import ts.eclipse.ide.terminal.interpreter.ICommandInterpreter;
import ts.eclipse.ide.terminal.interpreter.ICommandInterpreterFactory;

/**
 * After a "rd" command, this interpreter refresh the Project Explorer.
 *
 */
public class RdCommandInterpreterFactory implements ICommandInterpreterFactory {

	@Override
	public ICommandInterpreter create(List<String> parameters, String workingDir) {
		if (parameters.size() < 1) {
			return null;
		}
		return new RdCommandInterpreter(getPath(parameters), workingDir);
	}

	private String getPath(List<String> parameters) {
		// RD [/S] [/Q] [drive:]path
		// path is the last token
		return parameters.get(parameters.size() - 1);
	}

}
