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
package ts.resources;

import java.io.File;

/**
 * TypeScript project settings API.
 *
 */
public interface ITypeScriptProjectSettings {

	SynchStrategy getSynchStrategy();

	/**
	 * Returns the node.js install path.
	 * 
	 * @return the node.js install path.
	 */
	File getNodejsInstallPath();

	File getTsserverFile();

}
