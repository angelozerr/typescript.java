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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;

import ts.eclipse.ide.terminal.interpreter.ICommandInterpreter;
import ts.eclipse.ide.terminal.interpreter.ICommandInterpreterFactory;
import ts.eclipse.ide.terminal.interpreter.ICommandInterpreterParametersExtractor;

public class CommandInterpreterManager {

	private final static CommandInterpreterManager INSTANCE = new CommandInterpreterManager();

	private static final String COMMAND_INTERPRETER_FACTORIES_EXTENSION_POINT = "commandInterpreterFactories";

	private Map<String, ICommandInterpreterFactory> factories;
	private List<ICommandInterpreterParametersExtractor> extractors;

	public CommandInterpreterManager() {
	}

	public static CommandInterpreterManager getInstance() {
		return INSTANCE;
	}

	public ICommandInterpreter createInterpreter(String cmdWithParameters, String workingDir) {
		populateCache();
		ICommandInterpreter interpreter = createInterpreterFromParametersExtractor(cmdWithParameters, workingDir);
		if (interpreter != null) {
			return interpreter;
		}

		String cmd = cmdWithParameters;
		String parameters = null;
		int index = cmdWithParameters.indexOf(" ");
		if (index != -1) {
			cmd = cmdWithParameters.substring(0, index);
			parameters = cmdWithParameters.substring(index + 1, cmdWithParameters.length());
		}

		ICommandInterpreterFactory factory = CommandInterpreterManager.getInstance().getFactory(cmd.toUpperCase());
		if (factory != null) {
			return factory.create(getParameters(parameters), workingDir);
		}
		return null;
	}

	public ICommandInterpreter createInterpreterFromParametersExtractor(String cmdWithParameters, String workingDir) {
		String parameters = null;
		for (ICommandInterpreterParametersExtractor extractor : extractors) {
			parameters = extractor.extractParameters(cmdWithParameters);
			if (parameters != null) {
				return ((ICommandInterpreterFactory) extractor).create(getParameters(parameters), workingDir);
			}
		}
		return null;
	}

	private List<String> getParameters(String s) {
		if (s == null) {
			return Collections.emptyList();
		}
		List<String> parameters = new ArrayList<String>();
		char[] chars = s.toCharArray();
		StringBuilder param = null;
		for (int i = 0; i < chars.length; i++) {
			char c = chars[i];
			switch (c) {
			case ' ':
				if (param != null) {
					parameters.add(param.toString());
				}
				param = null;
				break;
			default:
				if (param == null) {
					param = new StringBuilder();
				}
				param.append(c);
			}
		}
		if (param != null) {
			parameters.add(param.toString());
		}
		return parameters;
	}

	private ICommandInterpreterFactory getFactory(String cmdWithParameters) {
		populateCache();
		String cmd = null;
		for (ICommandInterpreterParametersExtractor extractor : extractors) {
			cmd = extractor.extractParameters(cmdWithParameters);
			if (cmd != null && cmd.length() > 0) {
				return (ICommandInterpreterFactory) extractor;
			}
		}
		cmd = getCmd(cmdWithParameters);
		return factories.get(cmd.toUpperCase());
	}

	private String getCmd(String cmdWithParameters) {
		int index = cmdWithParameters.indexOf(" ");
		if (index != -1) {
			return cmdWithParameters.substring(0, index);
		}
		return cmdWithParameters;
	}

	/**
	 * Populates the preferences updater contribution cache if necessary.
	 *
	 */
	private void populateCache() {
		if (factories == null) {
			this.factories = new HashMap<String, ICommandInterpreterFactory>();
			this.extractors = new ArrayList<>();
			final IConfigurationElement[] elements = Platform.getExtensionRegistry().getConfigurationElementsFor(
					TerminalInterpreterPlugin.PLUGIN_ID, COMMAND_INTERPRETER_FACTORIES_EXTENSION_POINT);
			for (int index = 0; index < elements.length; index++) {
				// loop for factory elements
				final IConfigurationElement element = elements[index];
				try {
					String[] commands = element.getAttribute("commands").split(",");
					ICommandInterpreterFactory factory = (ICommandInterpreterFactory) element
							.createExecutableExtension("class");
					if (factory instanceof ICommandInterpreterParametersExtractor) {
						extractors.add((ICommandInterpreterParametersExtractor) factory);
					}
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
