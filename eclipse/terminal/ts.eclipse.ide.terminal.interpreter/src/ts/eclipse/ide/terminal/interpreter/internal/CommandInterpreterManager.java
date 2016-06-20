package ts.eclipse.ide.terminal.interpreter.internal;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;

import ts.eclipse.ide.terminal.interpreter.ICommandInterpreter;
import ts.eclipse.ide.terminal.interpreter.ICommandInterpreterFactory;

public class CommandInterpreterManager {

	private final static CommandInterpreterManager INSTANCE = new CommandInterpreterManager();

	private static final String COMMAND_INTERPRETER_FACTORIES_EXTENSION_POINT = "commandInterpreterFactories";

	private Map<String, ICommandInterpreterFactory> factories;

	public CommandInterpreterManager() {
	}

	public static CommandInterpreterManager getInstance() {
		return INSTANCE;
	}

	public ICommandInterpreter getCommand(String cmd) {
		populateCache();
		ICommandInterpreterFactory factory = factories.get(cmd.toUpperCase());
		if (factory != null) {
			return factory.create();
		}
		return null;
	}

	/**
	 * Populates the preferences updater contribution cache if necessary.
	 *
	 */
	private void populateCache() {
		if (factories == null) {
			this.factories = new HashMap<String, ICommandInterpreterFactory>();
			final IConfigurationElement[] elements = Platform.getExtensionRegistry().getConfigurationElementsFor(
					TerminalInterpreterPlugin.PLUGIN_ID, COMMAND_INTERPRETER_FACTORIES_EXTENSION_POINT);
			for (int index = 0; index < elements.length; index++) {
				// loop for factory elements
				final IConfigurationElement element = elements[index];
				try {
					String[] commands = element.getAttribute("commands").split(",");
					ICommandInterpreterFactory factory = (ICommandInterpreterFactory) element
							.createExecutableExtension("class");
					for (int i = 0; i < commands.length; i++) {
						registerFactory(commands[i], factory);
					}
				} catch (Throwable e) {
					e.printStackTrace();
				}
			}
		}
	}

	private void registerFactory(String cmd, ICommandInterpreterFactory factory) {
		factories.put(cmd.toUpperCase(), factory);
	}
}
