package ts.eclipse.ide.terminal.interpreter;

import java.util.List;

public interface ICommandTerminalListener {

	void submitCommand(String workingDir, String command);

	void processingCommand(String workingDir, String command, List<String> lines);

	void terminateCommand(String workingDir, String command);

}
