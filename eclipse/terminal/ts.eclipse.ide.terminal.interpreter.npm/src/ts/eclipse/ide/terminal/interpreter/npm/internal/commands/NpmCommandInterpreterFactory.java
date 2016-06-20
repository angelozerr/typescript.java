package ts.eclipse.ide.terminal.interpreter.npm.internal.commands;

import java.util.List;

import ts.eclipse.ide.terminal.interpreter.ICommandInterpreter;
import ts.eclipse.ide.terminal.interpreter.ICommandInterpreterFactory;

public class NpmCommandInterpreterFactory implements ICommandInterpreterFactory {

	private static final ICommandInterpreter NPM_INSTALL_INTERPRETER = new NpmInstallCommandInterpreter();

	@Override
	public ICommandInterpreter create(List<String> parameters) {
		if (parameters.contains("install")) {
			return NPM_INSTALL_INTERPRETER;
		}
		return null;
	}

}
