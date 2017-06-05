package org.eclipse.jface.text.provisional.codelens;

public class CodeLens implements ICodeLens {

	private Range range;
	private ICommand command;

	public CodeLens(int startLineNumber) {
		this(new Range(startLineNumber, 1));
	}
	
	public CodeLens(Range range) {
		this.range = range;
	}

	@Override
	public Range getRange() {
		return range;
	}

	@Override
	public boolean isResolved() {
		return getCommand() != null;
	}

	@Override
	public ICommand getCommand() {
		return command;
	}
	
	public void setCommand(ICommand command) {
		this.command = command;
	}

}
