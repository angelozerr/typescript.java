package ts.eclipse.ide.terminal.interpreter;

import java.util.List;

public interface ICommandInterpreterFactory {

	ICommandInterpreter create(List<String> parameters);

}
