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
import ts.eclipse.ide.core.resources.buildpath.ITypeScriptBuildPath;
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

	IIDETypeScriptFile getOpenedFile(IResource resource);

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

	/**
	 * Returns true if the given file is in the scope for validation,
	 * compilation and false otherwise.
	 * 
	 * A file is in the scope if :
	 * 
	 * <ul>
	 * <li>it is included by buildpath and.
	 * </li>
	 * <li>it doesn't exists tsconfig.json in the folder (and parent) of the
	 * file.</li>
	 * <li>it exists a tsconfig.json in the folder (or parent) of the file and:
	 * <ul>
	 * <li>the given file is defined in the "files" config section.</li>
	 * <li>or the given file is not excluded by the "exclude" config section.
	 * </li>
	 * </ul>
	 * </li>
	 * </ul>
	 * 
	 * @param resource
	 *            the TypeScript resource to validate.
	 * @return true if the given file can be validated and false otherwise.
	 */
	boolean isInScope(IResource resource);

	ITypeScriptBuildPath getTypeScriptBuildPath();
}