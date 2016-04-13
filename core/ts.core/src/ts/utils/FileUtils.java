/**
 *  Copyright (c) 2015-2016 Angelo ZERR
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package ts.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class FileUtils {

	/**
	 * Extension file
	 */
	public static final String JS_EXTENSION = "js";
	public static final String TS_EXTENSION = "ts";
	public static final String JSX_EXTENSION = "jsx";
	public static final String TSX_EXTENSION = "tsx";
	public static final String SOURCE_MAP_EXTENSION = "sourcemap";

	/**
	 * Configuration file
	 */
	public static final String TSCONFIG_JSON = "tsconfig.json";
	public static final String JSCONFIG_JSON = "jsconfig.json";

	public static String getFileExtension(String fileName) {
		int index = fileName.lastIndexOf('.');
		if (index == -1)
			return null;
		if (index == fileName.length() - 1)
			return ""; //$NON-NLS-1$
		return fileName.substring(index + 1);
	}

	public static String getFileNameWithoutExtension(String fileName) {
		int index = fileName.lastIndexOf('.');
		if (index == -1)
			return null;
		return fileName.substring(0, index);
	}

	/**
	 * Returns the normalized path of the given file.
	 * 
	 * @param file
	 * @return the normalized path of the given file.
	 */
	public static String getPath(File file) {
		return getPath(file, true);
	}

	/**
	 * Returns the path of the given file.
	 * 
	 * @param file
	 * @param normalize
	 * @return
	 */
	public static String getPath(File file, boolean normalize) {
		String path = null;
		try {
			path = file.getCanonicalPath();
		} catch (IOException e) {
			path = file.getPath();
		}
		return normalize ? normalizeSlashes(path) : null;
	}

	/**
	 * Replace '\' with '/' from the given path because tsserver normalize it
	 * like this.
	 * 
	 * @param path
	 * @return
	 */
	public static String normalizeSlashes(String path) {
		return path.replaceAll("\\\\", "/");
	}

	public static String getContents(final File file) throws IOException {
		InputStream in = null;
		try {
			in = openInputStream(file);
			return IOUtils.toString(in, null);
		} finally {
			IOUtils.closeQuietly(in);
		}
	}

	public static FileInputStream openInputStream(final File file) throws IOException {
		if (file.exists()) {
			if (file.isDirectory()) {
				throw new IOException("File '" + file + "' exists but is a directory");
			}
			if (file.canRead() == false) {
				throw new IOException("File '" + file + "' cannot be read");
			}
		} else {
			throw new FileNotFoundException("File '" + file + "' does not exist");
		}
		return new FileInputStream(file);
	}

}
