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
import ts.eclipse.ide.terminal.interpreter.ICommandInterpreterParametersExtractor;

public class CdCommandInterpreterFactory implements ICommandInterpreterFactory, ICommandInterpreterParametersExtractor {

	@Override
	public ICommandInterpreter create(List<String> parameters, String workingDir) {
		if (parameters.size() < 1) {
			return null;
		}
		String path = parameters.get(0);
		return new CdCommandInterpreter(path, workingDir);
	}

	@Override
	public String extractParameters(String cmdWithParameters) {
		if (!cmdWithParameters.startsWith("cd")) {
			return null;
		}
		return cmdWithParameters.length() > 2 ? cmdWithParameters.substring(2, cmdWithParameters.length()) : "";
	}
}
