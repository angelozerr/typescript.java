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
package ts.npm;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import ts.OS;
import ts.utils.IOUtils;

/**
 * NPM Process helper.
 *
 */
public class NPMProcessHelper {

	// public static void main(String[] args) throws IOException {
	// List<String> versions = NPMProcessHelper.getVersions("typescript",
	// OS.Windows);// "@angular/cli");
	// for (String version : versions) {
	// System.err.println(version);
	// }
	// }

	public static List<String> getVersions(String moduleName, OS os) throws IOException {
		List<String> versions = new ArrayList<>();
		BufferedReader reader = null;
		try {
			String[] command = { os == OS.Windows ? "npm.cmd" : "npm", "view", "--json", moduleName, "versions" };
			Process p = Runtime.getRuntime().exec(command);
			reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String line;
			while ((line = reader.readLine()) != null) {
				line = line.trim();
				if (line.startsWith("\"")) {
					String version = line.substring(1, line.lastIndexOf('"'));
					versions.add(0, version);
				}
			}
		} finally {
			IOUtils.closeQuietly(reader);
		}
		return versions;
	}
}
