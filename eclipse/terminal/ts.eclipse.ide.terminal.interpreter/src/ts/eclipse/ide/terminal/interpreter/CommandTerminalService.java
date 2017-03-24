package ts.eclipse.ide.terminal.interpreter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.Assert;
import org.eclipse.tm.terminal.view.core.interfaces.ITerminalService;
import org.eclipse.tm.terminal.view.core.interfaces.constants.ITerminalsConnectorConstants;
import org.eclipse.tm.terminal.view.ui.services.TerminalService;

import ts.eclipse.ide.terminal.interpreter.internal.TerminalConnectorWrapper;

public class CommandTerminalService extends TerminalService {

	private static final CommandTerminalService INSTANCE = new CommandTerminalService();

	private final Map<String, TerminalConnectorWrapper> connectors;

	private final List<ICommandInterpreterListener> listeners;

	public CommandTerminalService() {
		this.connectors = new HashMap<>();
		this.listeners = new ArrayList<>();
	}

	public static CommandTerminalService getInstance() {
		return INSTANCE;
	}

	public void executeCommand(List<String> commands, String terminalId, final Map<String, Object> properties,
			final ITerminalService.Done done) {
		if (commands.isEmpty()) {
			return;
		}
		Assert.isNotNull(terminalId);

		properties.put(ICommandTerminalServiceConstants.TERMINAL_ID, terminalId);
		properties.put(ICommandTerminalServiceConstants.COMMAND_ID, commands);

		TerminalConnectorWrapper connector = getConnector(terminalId, properties);
		if (connector != null) {
			String workingDir = (String) properties.get(ITerminalsConnectorConstants.PROP_PROCESS_WORKING_DIR);
			if (connector.hasWorkingDirChanged(workingDir)) {
				connector.executeCommand("cd " + workingDir, properties);
			}
			for (String cmd : commands) {
				connector.executeCommand(cmd, properties);
			}
		} else {
			this.openConsole(properties, done);
		}
	}

	public void executeCommand(String command, String terminalId, final Map<String, Object> properties,
			final ITerminalService.Done done) {
		Assert.isNotNull(terminalId);
		command = command.trim();
		properties.put(ICommandTerminalServiceConstants.TERMINAL_ID, terminalId);
		properties.put(ICommandTerminalServiceConstants.COMMAND_ID, command);

		TerminalConnectorWrapper connector = getConnector(terminalId, properties);
		if (connector != null) {
			String workingDir = (String) properties.get(ITerminalsConnectorConstants.PROP_PROCESS_WORKING_DIR);
			if (connector.hasWorkingDirChanged(workingDir)) {
				connector.executeCommand("cd " + workingDir, properties);
			}
			connector.executeCommand(command, properties);
		} else {
			this.openConsole(properties, done);
		}
	}

	private TerminalConnectorWrapper getConnector(String terminalId, Map<String, Object> properties) {
		TerminalConnectorWrapper connector = connectors.get(terminalId);
		if (connector != null) {
			if (connector.isDirty()) {
				unregisterConnector(connector);
				return null;
			}
		}
		return connector;
	}

	public void registerConnector(TerminalConnectorWrapper connector) {
		synchronized (connectors) {
			connectors.put(connector.getTerminalId(), connector);
		}
	}

	public void unregisterConnector(TerminalConnectorWrapper connector) {
		synchronized (connectors) {
			connectors.remove(connector.getTerminalId());
		}
	}

	public void addCommandTerminalListener(ICommandInterpreterListener listener) {
		synchronized (listeners) {
			if (!listeners.contains(listener)) {
				listeners.add(listener);
			}
		}
	}

	public void removeCommandTerminalListener(ICommandInterpreterListener listener) {
		synchronized (listeners) {
			listeners.remove(listener);
		}
	}

	public List<ICommandInterpreterListener> getInterpreterListeners() {
		return listeners;
	}

}
