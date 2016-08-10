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

import ts.eclipse.ide.core.nodejs.IEmbeddedNodejs;
import ts.resources.ITypeScriptProjectSettings;

/**
 * TypeScript project settings API.
 *
 */
public interface IIDETypeScriptProjectSettings extends ITypeScriptProjectSettings {

	/**
	 * Returns the node install from the project/workspace preferences.
	 * 
	 * @return the node install from the project/workspace preferences.
	 */
	IEmbeddedNodejs getEmbeddedNodejs();

	/**
	 * Returns true if JSON request/response can be traced inside Eclipse
	 * console and false otherwise.
	 * 
	 * @param project
	 * @return true if JSON request/response can be traced inside Eclipse
	 *         console and false otherwise.
	 */
	boolean isTraceOnConsole();


}
