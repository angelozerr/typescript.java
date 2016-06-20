/**
 *  Copyright (c) 2015-2016 Angelo ZERR.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
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

	public ICommandInterpreterFactory getFactory(String cmd) {
		populateCache();
		return factories.get(cmd.toUpperCase());
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
