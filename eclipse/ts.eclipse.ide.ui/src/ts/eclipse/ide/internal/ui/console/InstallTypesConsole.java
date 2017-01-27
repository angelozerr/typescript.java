package ts.eclipse.ide.internal.ui.console;

import ts.eclipse.ide.ui.TypeScriptUIImageResource;

public class InstallTypesConsole extends AbstractTypeScriptConsole {

	private static InstallTypesConsole console;

	public InstallTypesConsole() {
		super("Install @types", TypeScriptUIImageResource.getImageDescriptor(TypeScriptUIImageResource.IMG_LOGO));
	}

	public static InstallTypesConsole getConsole() {
		if (console == null) {
			console = new InstallTypesConsole();
		}
		return console;
	}
}
