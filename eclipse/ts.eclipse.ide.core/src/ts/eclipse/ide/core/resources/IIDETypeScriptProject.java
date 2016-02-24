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
package ts.eclipse.ide.core.resources;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.text.IDocument;

import ts.TypeScriptException;
import ts.resources.ITypeScriptProject;

/**
 * IDE TypeScript project API.
 *
 */
public interface IIDETypeScriptProject extends ITypeScriptProject {

	/**
	 * Returns the Eclispe project.
	 * 
	 * @return
	 */
	IProject getProject();

	/**
	 * Mark the given resource as opened to the tsserver and returns the cached
	 * instance {@link IIDETypeScriptFile}.
	 * 
	 * @param file
	 *            the TypeScript Eclipse {@link IResource} which is opened.
	 * @param document
	 *            the TypeScript Eclipse {@link IDocument} which is opened.
	 * @return
	 * @throws TypeScriptException
	 */
	IIDETypeScriptFile openFile(IResource file, IDocument document) throws TypeScriptException;

	/**
	 * Mark the given resource as closed to the tsserver.
	 * 
	 * @param file
	 * @throws TypeScriptException
	 */
	void closeFile(IResource file) throws TypeScriptException;

	/**
	 * Configure Eclipse Console if needed.
	 */
	void configureConsole();

	/**
	 * Returns the TypeScript project settings.
	 * 
	 * @return the TypeScript project settings.
	 */
	IIDETypeScriptProjectSettings getProjectSettings();

}