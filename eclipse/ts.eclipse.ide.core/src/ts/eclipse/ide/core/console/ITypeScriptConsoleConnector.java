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
package ts.eclipse.ide.core.console;

import ts.client.ITypeScriptServiceClient;
import ts.eclipse.ide.core.resources.IIDETypeScriptProject;

/**
 * Connector used to connect tern server to the eclipse console.
 * 
 */
public interface ITypeScriptConsoleConnector {

	/**
	 * Returns true if this connector can be applyied to the given tern server
	 * instance and false otherwise.
	 * 
	 * @param ternServer
	 * @return
	 */
	boolean isAdaptFor(ITypeScriptServiceClient client);

	/**
	 * Connect the give tern server to the eclipse tern console.
	 * 
	 * @param ternServer
	 */
	void connectToConsole(ITypeScriptServiceClient client, IIDETypeScriptProject project);

	/**
	 * Disconnect the give tern server to the eclipse tern console.
	 * 
	 * @param ternServer
	 */
	void disconnectToConsole(ITypeScriptServiceClient client, IIDETypeScriptProject project);

}
