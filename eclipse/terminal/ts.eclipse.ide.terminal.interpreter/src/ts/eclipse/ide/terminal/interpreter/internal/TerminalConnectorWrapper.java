package ts.eclipse.ide.terminal.interpreter.internal;

import java.io.OutputStream;
import java.util.Collection;
import java.util.Map;

import org.eclipse.core.runtime.Platform;
import org.eclipse.tm.internal.terminal.control.ITerminalViewControl;
import org.eclipse.tm.internal.terminal.provisional.api.ISettingsStore;
import org.eclipse.tm.internal.terminal.provisional.api.ITerminalConnector;
import org.eclipse.tm.internal.terminal.provisional.api.ITerminalControl;
import org.eclipse.tm.internal.terminal.provisional.api.TerminalState;
import org.eclipse.tm.terminal.view.core.interfaces.constants.ITerminalsConnectorConstants;

import ts.eclipse.ide.terminal.interpreter.CommandTerminalService;
import ts.eclipse.ide.terminal.interpreter.ICommandTerminalServiceConstants;

public class TerminalConnectorWrapper implements ITerminalConnectorWrapper {

	private final ITerminalConnector delegate;
	private final CommandInterpreterProcessor processor;
	private final String terminalId;
	private ITerminalViewControl terminal;

	private Map<String, Object> properties;

	public TerminalConnectorWrapper(ITerminalConnector delegate, CommandInterpreterProcessor processor,
			Map<String, Object> properties) {
		this.delegate = delegate;
		this.processor = processor;
		this.terminalId = getTerminalId(properties);
		this.properties = properties;
		if (terminalId != null) {
			CommandTerminalService.getInstance().registerConnector(this);
		}
	}

	private String getTerminalId(Map<String, Object> properties) {
		return (String) properties.get(ICommandTerminalServiceConstants.TERMINAL_ID);
	}

	public boolean isDirty() {
		return terminal == null || terminal.getState() == TerminalState.CLOSED;
	}

	public void connect(ITerminalControl control) {
		delegate.connect(control);
		if (control instanceof ITerminalViewControl) {
			terminal = (ITerminalViewControl) control;
			if (isConfigureShellPrompt()) {
				// Initialize Shell Prompt to display working dir in the
				// prompt
				// This command is required to track working directory by
				// CommandTerminalTracker
				executeCommand("PS1='\\w\\$ '", null); // //$NON-NLS-1$
			}
			Object command = properties.get(ICommandTerminalServiceConstants.COMMAND_ID);
			if (command instanceof Collection) {
				Collection<String> commands = (Collection<String>) command;
				for (String cmd : commands) {
					executeCommand(cmd, null);
				}
			} else {
				executeCommand((String) command, null);
			}

		}
	}

	private boolean isConfigureShellPrompt() {
		if (!Platform.OS_WIN32.equals(Platform.getOS())) {
			return true;
		}
		// Windows OS with cmd.exe should not initialize Shell Prompt.
		String settings = delegate.getSettingsSummary();
		return !(settings != null && settings.contains("cmd.exe"));
	}

	public void disconnect() {
		delegate.disconnect();
		if (terminalId != null) {
			CommandTerminalService.getInstance().unregisterConnector(this);
		}
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
		processor.setColumns(newWidth);
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
		terminal.pasteString(cmd + "\r\n");

	}

	public boolean hasWorkingDirChanged(String workingDir) {
		String wd = getWorkingDir();
		if (wd == null) {
			return workingDir != null;
		}
		return !wd.equals(workingDir);
	}

	@Override
	public String getWorkingDir() {
		return (String) properties.get(ITerminalsConnectorConstants.PROP_PROCESS_WORKING_DIR);
	}

}
