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
package ts.eclipse.ide.internal.ui.console;

import ts.eclipse.ide.core.resources.IIDETypeScriptProject;
import ts.eclipse.ide.ui.TypeScriptUIImageResource;

public class TypeScriptConsole extends AbstractTypeScriptConsole {

	private static final String CONSOLE_KEY = TypeScriptConsole.class.getName();

	private final IIDETypeScriptProject project;

	public TypeScriptConsole(IIDETypeScriptProject project) {
		super(getName(project), TypeScriptUIImageResource.getImageDescriptor(TypeScriptUIImageResource.IMG_LOGO));
		project.setData(CONSOLE_KEY, this);
		this.project = project;
	}

	private static String getName(IIDETypeScriptProject project) {
		return new StringBuilder("TypeScript [").append(project.getProject().getName()).append("]").toString();
	}

	public IIDETypeScriptProject getProject() {
		return project;
	}

	public static TypeScriptConsole getConsole(IIDETypeScriptProject project) {
		return project.getData(CONSOLE_KEY);
	}

	public static TypeScriptConsole getOrCreateConsole(IIDETypeScriptProject project) {
		TypeScriptConsole console = getConsole(project);
		if (console == null) {
			console = new TypeScriptConsole(project);
		}
		return console;
	}
}
