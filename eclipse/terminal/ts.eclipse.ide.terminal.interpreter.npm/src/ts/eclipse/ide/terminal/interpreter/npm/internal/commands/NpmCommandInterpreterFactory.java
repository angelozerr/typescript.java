package ts.eclipse.ide.terminal.interpreter.npm.internal.commands;

import java.util.List;

import ts.eclipse.ide.terminal.interpreter.ICommandInterpreter;
import ts.eclipse.ide.terminal.interpreter.ICommandInterpreterFactory;

public class NpmCommandInterpreterFactory implements ICommandInterpreterFactory {

	@Override
	public ICommandInterpreter create(List<String> parameters, String workingDir) {
		if (parameters.contains("install")) {
			return new NpmInstallCommandInterpreter(workingDir);
		}
		return null;
	}
}
