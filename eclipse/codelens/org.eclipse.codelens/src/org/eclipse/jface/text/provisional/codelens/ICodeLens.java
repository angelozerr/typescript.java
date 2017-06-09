package org.eclipse.jface.text.provisional.codelens;

/**
 * A code lens represents a command that should be shown along with source text,
 * like the number of references, a way to run tests, etc.
 *
 * A code lens is _unresolved_ when no command is associated to it. For
 * performance reasons the creation of a code lens and resolving should be done
 * in two stages.
 */
public interface ICodeLens {

	/**
	 * The range in which this code lens is valid. Should only span a single
	 * line.
	 */
	Range getRange();
	
	/**
	 * Returns `true` when there is a command associated.
	 * @return `true` when there is a command associated.
	 */
	boolean isResolved();
	
	/**
	 * The command this code lens represents.
	 */
	ICommand getCommand();

	void open();
	
}
