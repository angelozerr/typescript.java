/**
 *  Copyright (c) 2013-2015 Angelo ZERR.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package ts.eclipse.ide.server.nodejs.internal.ui.console;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

import org.eclipse.swt.widgets.Display;

import ts.eclipse.ide.core.resources.IIDETypeScriptProject;
import ts.eclipse.ide.ui.TypeScriptUIPlugin;
import ts.eclipse.ide.ui.console.ITypeScriptConsole;
import ts.eclipse.ide.ui.console.LineType;
import ts.server.LoggingInterceptor;
import ts.server.nodejs.process.INodejsProcess;
import ts.server.nodejs.process.INodejsProcessListener;

public class TypeScriptNodejsInterceptor extends LoggingInterceptor implements INodejsProcessListener {

	private final IIDETypeScriptProject project;

	public TypeScriptNodejsInterceptor(IIDETypeScriptProject project) {
		this.project = project;
	}

	@Override
	protected void outPrintln(String line) {
		ITypeScriptConsole console = getConsole();
		if (console != null) {
			console.doAppendLine(LineType.DATA, line);
		}
	}

	protected void outProcessPrintln(String line) {
		ITypeScriptConsole console = getConsole();
		if (console != null) {
			console.doAppendLine(LineType.PROCESS_INFO, line);
		}
	}

	protected void errPrintln(String line) {
		ITypeScriptConsole console = getConsole();
		if (console != null) {
			console.doAppendLine(LineType.PROCESS_ERROR, line);
		}
	}

	@Override
	protected void printStackTrace(Throwable error) {
		ITypeScriptConsole console = getConsole();
		if (console != null) {
			StringWriter s = new StringWriter();
			PrintWriter writer = new PrintWriter(s);
			error.printStackTrace(writer);
			console.doAppendLine(LineType.PROCESS_ERROR, s.toString());
		}
	}

	@Override
	public void onCreate(final INodejsProcess process, final List<String> commands, final File projectDir) {
		if (Display.getDefault().getThread() != Thread.currentThread()) {
			Display.getDefault().asyncExec(new Runnable() {
				@Override
				public void run() {
					onCreate(process, commands, projectDir);
				}
			});
			return;
		}
		ITypeScriptConsole console = getConsole();
		if (console != null) {
			StringBuilder commandsAsString = new StringBuilder();
			int i = 0;
			for (String cmd : commands) {
				if (i > 0) {
					commandsAsString.append(" ");
				}
				if (i <= 1) {
					commandsAsString.append("\"");
				}
				commandsAsString.append(cmd);
				if (i <= 1) {
					commandsAsString.append("\"");
				}
				i++;
			}
			outProcessPrintln("Nodejs Commnand: " + commandsAsString.toString());
			String path = projectDir.getPath();
			try {
				path = projectDir.getCanonicalPath();
			} catch (IOException e) {
			}
			outProcessPrintln("Project dir: " + path);
			/*String json = "";
			try {
				File ternProject = new File(projectDir, ITypeScriptProject.TERN_PROJECT_FILE);
				json = IOUtils.toString(new FileInputStream(ternProject));
			} catch (Throwable e) {
				errPrintln(e.getMessage());
			}

			outProcessPrintln(ITypeScriptProject.TERN_PROJECT_FILE + ": " + json);
			*/
		}
	}

	@Override
	public void onStart(INodejsProcess process) {
		outProcessPrintln("tsserver started");
	}

	@Override
	public void onData(INodejsProcess process, String line) {
		outProcessPrintln(line);
	}

	@Override
	public void onStop(INodejsProcess process) {
		outProcessPrintln("tsserver stopped");
	}

	@Override
	public void onError(INodejsProcess process, String line) {
		errPrintln(line);
	}

	private ITypeScriptConsole getConsole() {
		if (TypeScriptUIPlugin.getDefault() != null) {
			return TypeScriptUIPlugin.getDefault().getConsole(project);
		}
		return null;
	}
}
