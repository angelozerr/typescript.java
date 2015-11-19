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
package ts.resources;

import ts.ICompletionInfo;
import ts.TSException;
import ts.server.ITypeScriptServiceClient;

public interface ITypeScriptProject {

	/**
	 * Returns associated tsclient if any. This call may result in creating one
	 * if it hasn't been created already.
	 * 
	 * @return
	 * @throws TSException 
	 */
	ITypeScriptServiceClient getClient() throws TSException;

	void openFile(ITypeScriptFile file) throws TSException;
	
	void closeFile(String fileName) throws TSException;
	
	ICompletionInfo getCompletionsAtPosition(ITypeScriptFile file, int position) throws TSException;
	
	ITypeScriptFile getFile(String fileName);
}
