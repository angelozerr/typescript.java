package ts.eclipse.ide.terminal.interpreter;

import ts.eclipse.ide.terminal.interpreter.internal.LineCommand;

public interface ITerminalCommandListener {

	void onSubmitCommand(LineCommand lineCommand);

	void onExecutingCommand(String line, LineCommand lineCommand);

	void onTerminateCommand(LineCommand lineCommand);

}
