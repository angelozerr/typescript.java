package org.eclipse.jface.text.provisional.codelens;

public class Command implements ICommand {

	private final String title;
	private final String command;

	public Command(String title, String command) {
		this.title = title;
		this.command = command;
	}

	@Override
	public String getTitle() {
		return title;
	}

	@Override
	public String getCommand() {
		return command;
	}

}
