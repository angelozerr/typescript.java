package ts.eclipse.ide.terminal.interpreter;

import java.util.List;

public interface ICommandInterpreter {

	/**
	 * Execute the command interpreter with the given parameters and working
	 * directory.
	 * 
	 * @param parameters
	 * @param workingDir
	 */
	void execute(List<String> parameters, String workingDir);

	void addLine(String line);

}
