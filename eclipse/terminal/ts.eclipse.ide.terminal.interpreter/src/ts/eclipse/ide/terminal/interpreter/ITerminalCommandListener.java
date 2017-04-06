package ts.eclipse.ide.terminal.interpreter;

public interface ITerminalCommandListener {

	void onSubmitCommand(LineCommand lineCommand);

	void onExecutingCommand(String line, LineCommand lineCommand);

	void onTerminateCommand(LineCommand lineCommand);

}
