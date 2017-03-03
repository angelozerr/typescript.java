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
package ts.eclipse.ide.core.utils;

import org.eclipse.core.runtime.Platform;

import ts.OS;

/**
 * OS Helper.
 *
 */
public class OSHelper {

	private static final OS os;

	static {
		if (Platform.getOS().startsWith("win")) {
			os = OS.Windows;
		} else if (Platform.getOS().equals(Platform.OS_MACOSX)) {
			os = OS.MacOS;
		} else {
			os = OS.Linux;
		}
	}

	public static OS getOs() {
		return os;
	}
}
