package org.eclipse.jface.text.provisional.codelens;

/**
 * Represents a reference to a command. Provides a title which will be used to
 * represent a command in the UI and, optionally, an array of arguments which
 * will be passed to the command handler function when invoked.
 */
public interface ICommand {

	/**
	 * Title of the command, like `save`.
	 */
	String getTitle();

	/**
	 * The identifier of the actual command handler.
	 * 
	 * @see [commands.registerCommand](#commands.registerCommand).
	 */
	String getCommand();

	/**
	 * A tooltip for for command, when represented in the UI.
	 */
	default String getTooltip() {
		return null;
	}

	/**
	 * Arguments that the command handler should be invoked with.
	 */
	default Object[] getArguments() {
		return null;
	}
}
