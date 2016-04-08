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
package ts.client.protocol;

/**
 * NavBar itesm request; value of command field is "navbar". Return response
 * giving the list of navigation bar entries extracted from the requested file.
 */
public class NavBarRequest extends FileRequest {

	public NavBarRequest(String fileName) {
		super(CommandNames.NavBar.getName(), new FileRequestArgs(fileName), null);
	}

}
