/**
 *  Copyright (c) 2013-2014 Angelo ZERR.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package ts.nodejs;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import ts.OS;
import ts.utils.IOUtils;
import ts.utils.StringUtils;

/**
 * Node path helper.
 *
 */
public class NodejsProcessHelper {

	private static final String[] WINDOWS_NODE_PATHS = new String[] {
			"C:/Program Files/nodejs/node.exe".replace('/', File.separatorChar),
			"C:/Program Files (x86)/nodejs/node.exe".replace('/', File.separatorChar), "node" };

	private static final String[] MACOS_NODE_PATHS = new String[] { "/usr/local/bin/node", "/opt/local/bin/node",
			"node" };

	private static final String[] LINUX_NODE_PATHS = new String[] { "/usr/local/bin/node", "node" };

	private NodejsProcessHelper() {
	}

	public static String getNodejsPath(OS os) {
		String path = getDefaultNodejsPath(os);
		if (path != null) {
			return path;
		}
		File nodeFile = findNode(os);
		if (nodeFile != null) {
			return nodeFile.getAbsolutePath();
		}
		return "node";
	}

	public static String getDefaultNodejsPath(OS os) {
		String[] paths = getDefaultNodejsPaths(os);
		String path = null;
		for (int i = 0; i < paths.length; i++) {
			path = paths[i];
			if (new File(path).exists()) {
				return path;
			}
		}
		return null;
	}

	public static String[] getDefaultNodejsPaths(OS os) {
		switch (os) {
		case Windows:
			return WINDOWS_NODE_PATHS;
		case MacOS:
			return MACOS_NODE_PATHS;
		default:
			return LINUX_NODE_PATHS;
		}
	}

	public static File findNode(OS os) {
		String nodeFileName = getNodeFileName(os);
		String path = System.getenv("PATH");
		String[] paths = path.split("" + File.pathSeparatorChar, 0);
		List<String> directories = new ArrayList<String>();
		for (String p : paths) {
			directories.add(p);
		}

		// ensure /usr/local/bin is included for OS X
		if (os == OS.MacOS) {
			directories.add("/usr/local/bin");
		}

		// search for Node.js in the PATH directories
		for (String directory : directories) {
			File nodeFile = new File(directory, nodeFileName);

			if (nodeFile.exists()) {
				return nodeFile;
			}
		}

		return getNodeLocation(os);
	}

	private static String getNodeFileName(OS os) {
		if (os == OS.Windows) {
			return "node.exe";
		}
		return "node";
	}

	/**
	 * Returns the node.js location by using command "which node".
	 * 
	 * @param os
	 * @return the node.js location by using command "which node".
	 */
	private static File getNodeLocation(OS os) {
		String[] command = new String[] { "/bin/bash", "-c", "which node" };
		if (os == OS.Windows) {
			command = new String[] { "cmd", "/c", "where node" };
		} else {
			command = new String[] { "/bin/bash", "-c", "which node" };
		}
		BufferedReader reader = null;
		try {
			Process p = Runtime.getRuntime().exec(command);
			reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String nodeFile = reader.readLine();
			if (StringUtils.isEmpty(nodeFile)) {
				return null;
			}
			File f = new File(nodeFile);
			return f.exists() ? f : null;
		} catch (IOException e) {
			//e.printStackTrace();
		} finally {
			IOUtils.closeQuietly(reader);
		}
		return null;
	}
}
