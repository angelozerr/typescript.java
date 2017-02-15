package ts.eclipse.ide.terminal.interpreter.internal;

import java.util.Map;

import org.eclipse.tm.internal.terminal.provisional.api.ITerminalConnector;

public interface ITerminalConnectorWrapper extends ITerminalConnector{

	void executeCommand(String cmd, Map<String, Object> properties);
	
	boolean hasWorkingDirChanged(String workingDir);
	
	String getCommand();

	String getWorkingDir();

	String getEncoding();
}
