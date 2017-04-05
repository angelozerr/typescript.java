package ts.eclipse.ide.terminal.interpreter.internal;

import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

import org.eclipse.core.runtime.Platform;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.tm.internal.terminal.control.ITerminalViewControl;
import org.eclipse.tm.internal.terminal.provisional.api.ISettingsStore;
import org.eclipse.tm.internal.terminal.provisional.api.ITerminalConnector;
import org.eclipse.tm.internal.terminal.provisional.api.ITerminalControl;
import org.eclipse.tm.internal.terminal.provisional.api.TerminalState;
import org.eclipse.tm.terminal.model.ITerminalTextData;
import org.eclipse.tm.terminal.view.core.interfaces.constants.ITerminalsConnectorConstants;

import ts.eclipse.ide.terminal.interpreter.CommandTerminalService;
import ts.eclipse.ide.terminal.interpreter.ICommandTerminalServiceConstants;
import ts.eclipse.ide.terminal.interpreter.ITerminalCommandListener;

public class TerminalConnectorWrapper implements ITerminalConnectorWrapper, ITerminalCommandListener {

	private final ITerminalConnector delegate;
	private final CommandInterpreterProcessor processor;
	private final String terminalId;
	private ITerminalViewControl terminal;

	private Map<String, Object> properties;

	private final Queue<LineCommand> commandsQueue;
	private boolean terminalReady;
	
	public TerminalConnectorWrapper(ITerminalConnector delegate, CommandInterpreterProcessor processor,
			Map<String, Object> properties) {
		this.delegate = delegate;
		this.processor = processor;
		this.commandsQueue = new ArrayBlockingQueue<>(5, true);
		processor.addTerminalCommandListener(this);
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
			bindWithTerminalTextData(terminal);
			if (isConfigureShellPrompt()) {
				// Initialize Shell Prompt to display working dir in the
				// prompt
				// This command is required to track working directory by
				// CommandTerminalTracker
				executeCommand("PS1='\\w\\$ '", properties, true); //$NON-NLS-1$
			}
			Object command = properties.get(ICommandTerminalServiceConstants.COMMAND_ID);
			if (command instanceof Collection) {
				Collection<String> commands = (Collection<String>) command;
				for (String cmd : commands) {
					executeCommand(cmd, properties);
				}
			} else {
				executeCommand((String) command, properties);
			}
		}
	}

	private void bindWithTerminalTextData(ITerminalViewControl terminal) {
		ITerminalTextData data = getTerminaltextData();
		if (data == null) {
			return;
		}
		terminal.getControl().addKeyListener(new KeyAdapter() {

			private int startLine = -1;

			@Override
			public void keyPressed(KeyEvent e) {
				if (startLine == -1) {
					startLine = data.getCursorLine();
				}
				char character = e.character;
				if (character == '\r') {
					// User is typing enter
					StringBuilder line = new StringBuilder();
					int endLine = data.getHeight();
					for (int i = startLine; i <= endLine; i++) {
						char[] chars = data.getChars(i);
						if (chars != null) {
							line.append(chars);
						}
					}
					String c = line.toString();
					int index = c.indexOf(">");
					if (index == -1) {
						index = c.indexOf("$");
					}
					if (index != -1) {
						String command = line.substring(index + 1, line.length());
						submitCommand(new LineCommand(command), false);
					}
					startLine = -1;
				}
			}
		});
	}

	private ITerminalTextData getTerminaltextData() {
		try {
			Field f = terminal.getClass().getDeclaredField("fTerminalModel");
			f.setAccessible(true);
			return (ITerminalTextData) f.get(terminal);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
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
		executeCommand(cmd, properties, false);
	}

	private void executeCommand(String cmd, Map<String, Object> properties, boolean nowaitForTerminalready) {
		if (cmd == null) {
			return;
		}
		if (properties != null) {
			this.properties = properties;
		}
		LineCommand lineCommand = new LineCommand(cmd);
		if ((nowaitForTerminalready || (terminalReady && commandsQueue.isEmpty()))) {
			submitCommand(lineCommand, true);
		} else {
			commandsQueue.add(lineCommand);
		}
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

	@Override
	public void onSubmitCommand(LineCommand lineCommand) {

	}

	@Override
	public void onExecutingCommand(String line, LineCommand lineCommand) {

	}

	@Override
	public void onTerminateCommand(LineCommand lineCommand) {
		this.terminalReady = true;
		if (!commandsQueue.isEmpty()) {
			LineCommand next = commandsQueue.poll();
			submitCommand(next, true);
		}
	}

	private void submitCommand(LineCommand lineCommand, boolean submitToTerminal) {
		processor.submitCommand(lineCommand);
		if (submitToTerminal) {
			terminal.pasteString(lineCommand.getCommand() + "\r\n");
		}
	}

}
