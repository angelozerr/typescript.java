package ts.eclipse.ide.terminal.interpreter;

import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.Assert;
import org.eclipse.tm.internal.terminal.control.ITerminalViewControl;
import org.eclipse.tm.internal.terminal.provisional.api.ISettingsStore;
import org.eclipse.tm.internal.terminal.provisional.api.ITerminalConnector;
import org.eclipse.tm.internal.terminal.provisional.api.ITerminalControl;
import org.eclipse.tm.internal.terminal.provisional.api.TerminalState;
import org.eclipse.tm.terminal.view.core.interfaces.ITerminalService;
import org.eclipse.tm.terminal.view.core.interfaces.ITerminalServiceOutputStreamMonitorListener;
import org.eclipse.tm.terminal.view.core.interfaces.constants.ITerminalsConnectorConstants;
import org.eclipse.tm.terminal.view.ui.services.TerminalService;

import ts.eclipse.ide.terminal.interpreter.internal.CommandInterpreterProcessor;
import ts.eclipse.ide.terminal.interpreter.internal.ITerminalConnectorWrapper;

public class CommandTerminalService extends TerminalService {

	private static final CommandTerminalService INSTANCE = new CommandTerminalService();

	private final Map<String, TerminalConnectorWrapper> connectors;

	public CommandTerminalService() {
		this.connectors = new HashMap<>();
	}

	public static CommandTerminalService getInstance() {
		return INSTANCE;
	}

	@Override
	protected ITerminalConnector createTerminalConnector(Map<String, Object> properties) {
		ITerminalConnector connector = super.createTerminalConnector(properties);
		String terminalId = getTerminalId(properties);
		if (terminalId == null) {
			return connector;
		}
		return new TerminalConnectorWrapper(connector, terminalId, properties);
	}

	public String getTerminalId(Map<String, Object> properties) {
		return (String) properties.get(ICommandTerminalServiceConstants.TERMINAL_ID);
	}

	public void executeCommand(String command, String terminalId, final Map<String, Object> properties,
			final ITerminalService.Done done) {
		Assert.isNotNull(terminalId);
		properties.put(ICommandTerminalServiceConstants.TERMINAL_ID, terminalId);
		properties.put(ICommandTerminalServiceConstants.COMMAND_ID, command);

		TerminalConnectorWrapper connector = getConnector(terminalId, properties);
		if (connector != null) {
			connector.executeCommand(command, properties);
		} else {
			this.openConsole(properties, done);
		}
	}

	private TerminalConnectorWrapper getConnector(String terminalId, Map<String, Object> properties) {
		TerminalConnectorWrapper connector = connectors.get(terminalId);
		if (connector != null) {
			String workingDir = (String) properties.get(ITerminalsConnectorConstants.PROP_PROCESS_WORKING_DIR);
			if (connector.isDirty()) {
				unregisterConnector(connector);
				return null;
			} else if (connector.hasWorkingDirChanged(workingDir)) {
				terminateConsole(properties, null);
				closeConsole(properties, null);
				unregisterConnector(connector);
				return null;
			}
		}
		return connector;
	}

	void registerConnector(TerminalConnectorWrapper connector) {
		synchronized (connectors) {
			connectors.put(connector.getTerminalId(), connector);
		}
	}

	void unregisterConnector(TerminalConnectorWrapper connector) {
		synchronized (connectors) {
			connectors.remove(connector.getTerminalId());
		}
	}

	class TerminalConnectorWrapper implements ITerminalConnectorWrapper {

		private final ITerminalConnector delegate;
		private final String terminalId;
		private ITerminalViewControl terminal;
		private String command;

		private Map<String, Object> properties;

		public TerminalConnectorWrapper(ITerminalConnector delegate, String terminalId,
				Map<String, Object> properties) {
			this.delegate = delegate;
			this.terminalId = terminalId;
			this.properties = properties;
			ITerminalServiceOutputStreamMonitorListener[] listeners = (ITerminalServiceOutputStreamMonitorListener[]) properties
					.get(ITerminalsConnectorConstants.PROP_STDOUT_LISTENERS);
			for (ITerminalServiceOutputStreamMonitorListener listener : listeners) {
				if (listener instanceof CommandInterpreterProcessor) {
					((CommandInterpreterProcessor) listener).setConnector(this);
				}
			}
			CommandTerminalService.this.registerConnector(this);
		}

		public boolean isDirty() {
			return terminal == null || terminal.getState() == TerminalState.CLOSED;
		}

		public void connect(ITerminalControl control) {
			delegate.connect(control);
			if (control instanceof ITerminalViewControl) {
				terminal = (ITerminalViewControl) control;
				executeCommand(getCommand(), null);
			}
		}

		public void disconnect() {
			delegate.disconnect();
			CommandTerminalService.this.unregisterConnector(this);
		}

		public <T> T getAdapter(Class<T> arg0) {
			return delegate.getAdapter(arg0);
		}

		public String getId() {
			return delegate.getId();
		}

		public String getTerminalId() {
			return terminalId;
		}

		public String getName() {
			return delegate.getName();
		}

		public boolean isHidden() {
			return delegate.isHidden();
		}

		public boolean isInitialized() {
			return delegate.isInitialized();
		}

		public String getInitializationErrorMessage() {
			return delegate.getInitializationErrorMessage();
		}

		public boolean isLocalEcho() {
			return delegate.isLocalEcho();
		}

		public void setTerminalSize(int newWidth, int newHeight) {
			delegate.setTerminalSize(newWidth, newHeight);
		}

		public OutputStream getTerminalToRemoteStream() {
			return delegate.getTerminalToRemoteStream();
		}

		public void load(ISettingsStore store) {
			delegate.load(store);
		}

		public void save(ISettingsStore store) {
			delegate.save(store);
		}

		public void setDefaultSettings() {
			delegate.setDefaultSettings();
		}

		public String getSettingsSummary() {
			return delegate.getSettingsSummary();
		}

		public void executeCommand(String cmd, Map<String, Object> properties) {
			if (properties != null) {
				this.properties = properties;
			}
			if (cmd == null || terminal == null) {
				return;
			}
			terminal.pasteString(cmd + '\r');
		}

		public boolean hasWorkingDirChanged(String workingDir) {
			String wd = getWorkingDir();
			if (wd == null) {
				return workingDir != null;
			}
			return !wd.equals(workingDir);
		}

		public String getCommand() {
			return (String) properties.get(ICommandTerminalServiceConstants.COMMAND_ID);
		}

		@Override
		public String getWorkingDir() {
			return (String) properties.get(ITerminalsConnectorConstants.PROP_PROCESS_WORKING_DIR);
		}

		@Override
		public String getEncoding() {
			return (String) properties.get(ITerminalsConnectorConstants.PROP_ENCODING);
		}
	}
}
