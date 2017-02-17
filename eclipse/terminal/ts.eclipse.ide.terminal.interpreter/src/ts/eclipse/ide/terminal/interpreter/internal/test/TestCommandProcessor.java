package ts.eclipse.ide.terminal.interpreter.internal.test;

import java.util.List;

import ts.eclipse.ide.terminal.interpreter.internal.AbstractCommandProcessor;

public class TestCommandProcessor extends AbstractCommandProcessor {

	private final String initialWorkingDir;
	private final String initialCommand;

	public TestCommandProcessor(String initialWorkingDir, String initialCommand) {
		this.initialWorkingDir = initialWorkingDir;
		this.initialCommand = initialCommand;
	}

	@Override
	protected void processingCommand(String workingDir, String command, List<String> lines) {
		System.err.println(lines);
	}

	@Override
	protected String getInitialWorkingDir() {
		return initialWorkingDir;
	}

	@Override
	protected String getInitialCommand() {
		return initialCommand;
	}

	@Override
	protected String getEncoding() {
		return null;
	}

	@Override
	public void processLines(List<String> lines, boolean processAnsiCommand_n) {
		super.processLines(lines, processAnsiCommand_n);
	}

}
