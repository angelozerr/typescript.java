/**
 *  Copyright (c) 2015-2017 Angelo ZERR.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package ts.eclipse.ide.core.nodejs;

import ts.eclipse.ide.core.utils.OSHelper;
import ts.nodejs.NodejsProcessHelper;

/**
 * IDE node.js process helper.
 */
public class IDENodejsProcessHelper {

	private IDENodejsProcessHelper() {
	}

	public static String getNodejsPath() {
		return NodejsProcessHelper.getNodejsPath(OSHelper.getOs());
	}

	public static String[] getDefaultNodejsPaths() {
		return NodejsProcessHelper.getDefaultNodejsPaths(OSHelper.getOs());
	}

	public static String[] getAvailableNodejsPaths() {
		return NodejsProcessHelper.getNodejsPaths(OSHelper.getOs());
	}

}
