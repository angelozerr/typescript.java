package ts.eclipse.ide.terminal.interpreter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

	public void executeCommand(Collection<LineCommand> commands, String terminalId,
			final Map<String, Object> properties, ITerminalService.Done done) {
		Assert.isTrue(commands.size() > 0, "Command list cannot be empty");
		Assert.isNotNull(terminalId, "Terminal ID cannot be null");
		properties.put(ICommandTerminalServiceConstants.TERMINAL_ID, terminalId);
		properties.put(ICommandTerminalServiceConstants.COMMAND_ID, commands);
		
		TerminalConnectorWrapper connector = getConnector(terminalId, properties);
		if (connector != null) {
			String workingDir = (String) properties.get(ITerminalsConnectorConstants.PROP_PROCESS_WORKING_DIR);
			if (connector.hasWorkingDirChanged(workingDir)) {
				connector.executeCommand(new LineCommand("cd " + workingDir), properties);
			}
			for (LineCommand command : commands) {
				connector.executeCommand(command, properties);
			}
		} else {
			this.openConsole(properties, done);
		}

	}

	public void executeCommand(String command, String terminalId, final Map<String, Object> properties,
			final ITerminalService.Done done) {
		List<LineCommand> commands = new ArrayList<>();
		commands.add(new LineCommand(command));
		executeCommand(commands, terminalId, properties, done);
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
