package ts.eclipse.ide.terminal.interpreter;

import java.util.List;

public interface ICommandInterpreter {

	void process(List<String> parameters, String workingDir);

	void addLine(String line);

}
