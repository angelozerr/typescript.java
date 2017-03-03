/**
 *  Copyright (c) 2016-2017 Angelo ZERR.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package ts.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import ts.OS;

/**
 * Helper for Process.
 *
 */
public class ProcessHelper {

	private static final String PATH_ENV = "PATH";

	/**
	 * Returns the given filename location.
	 * 
	 * @param fileName
	 * @param os
	 * @param extension
	 * @return the given filename location
	 */
	public static File findLocation(String fileName, OS os, String extension) {
		String fileNameWithExt = extension == null ? fileName : fileName + extension;
		String path = System.getenv(PATH_ENV);
		String[] paths = path.split("" + File.pathSeparatorChar, 0);
		List<String> directories = new ArrayList<String>();
		for (String p : paths) {
			directories.add(p);
		}

		// ensure /usr/local/bin is included for OS X
		if (os == OS.MacOS) {
			directories.add("/usr/local/bin");
		}

		// search for filename in the PATH directories
		for (String directory : directories) {
			File file = new File(directory, fileNameWithExt);

			if (file.exists()) {
				return file;
			}
		}
		return which(fileName, os);
	}

	/**
	 * Returns the given filename location by using command "which $filename".
	 * 
	 * @param fileName
	 *            file name to search.
	 * @param os
	 *            the OS.
	 * @return the given filename location by using command "which $filename".
	 */
	public static File which(String fileName, OS os) {
		String[] command = getCommand(fileName, os);
		BufferedReader reader = null;
		try {
			Process p = Runtime.getRuntime().exec(command);
			reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String foundFile = reader.readLine();
			if (StringUtils.isEmpty(foundFile)) {
				return null;
			}
			File f = new File(foundFile);
			return f.exists() ? f : null;
		} catch (IOException e) {
			return null;
		} finally {
			IOUtils.closeQuietly(reader);
		}
	}

	private static String[] getCommand(String fileName, OS os) {
		if (os == OS.Windows) {
			return new String[] { "cmd", "/c", "where " + fileName };
		}
		return new String[] { "/bin/bash", "-c", "which " + fileName };
	}
}
