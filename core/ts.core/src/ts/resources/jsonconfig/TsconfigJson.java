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
package ts.resources.jsonconfig;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import ts.compiler.CompilerOptions;
import ts.utils.FileUtils;
import ts.utils.StringUtils;

/**
 * Pojo for tsconfig.json
 * 
 * @see http://www.typescriptlang.org/docs/handbook/tsconfig.json.html
 *
 */
public class TsconfigJson {

	private CompilerOptions compilerOptions;

	private boolean compileOnSave;

	private List<String> files;

	private List<String> exclude;

	private List<String> defaultExclude;

	public TsconfigJson() {
		this.compileOnSave = true;
	}

	public void setCompilerOptions(CompilerOptions compilerOptions) {
		this.compilerOptions = compilerOptions;
	}

	public CompilerOptions getCompilerOptions() {
		return compilerOptions;
	}

	public boolean isCompileOnSave() {
		return compileOnSave;
	}

	public void setCompileOnSave(boolean compileOnSave) {
		this.compileOnSave = compileOnSave;
	}

	public List<String> getFiles() {
		return files;
	}

	public void setFiles(List<String> files) {
		this.files = files;
	}

	public boolean hasFiles() {
		return files != null;
	}

	public List<String> getExclude() {
		return exclude;
	}

	public void setExclude(List<String> exclude) {
		this.exclude = exclude;
	}

	public boolean hasExclude() {
		return exclude != null;
	}

	/**
	 * Returns the defined "exclude" list from the tsconfig.json other exclude
	 * by default "node_modules" and "bower_components".
	 * 
	 * @return the defined "exclude" list from the tsconfig.json other exclude
	 *         by default "node_modules" and "bower_components".
	 */
	protected List<String> getDefaultOrDefinedExclude() {
		if (exclude != null) {
			return exclude;
		}
		if (defaultExclude != null) {
			return defaultExclude;
		}
		// by default exclude node_modules, bower_components and any specificied
		// output directory (see this rule used in the tsc.js)
		this.defaultExclude = new ArrayList<String>(Arrays.asList(FileUtils.NODE_MODULES, FileUtils.BOWER_COMPONENTS));
		CompilerOptions options = getCompilerOptions();
		if (options != null && !StringUtils.isEmpty(options.getOutDir())) {
			defaultExclude.add(options.getOutDir());
		}
		return defaultExclude;
	}

	/**
	 * Load tsconfig.json instance from the given reader.
	 * 
	 * @param reader
	 * @return tsconfig.json instance from the given reader.
	 */
	public static TsconfigJson load(Reader reader) {
		return load(reader, TsconfigJson.class);
	}

	public static <T extends TsconfigJson> T load(Reader json, Class<T> classOfT) {
		Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
		return gson.fromJson(json, classOfT);
	}

	public static <T extends TsconfigJson> T load(InputStream in, Class<T> classOfT) {
		Reader isr = null;
		try {
			isr = new InputStreamReader(in);
			return load(isr, classOfT);
		} finally {
			if (isr != null) {
				try {
					isr.close();
				} catch (IOException e) {
				}
			}
		}
	}

	/**
	 * Load tsconfig.json instance from the given input stream.
	 * 
	 * @param in
	 * @return tsconfig.json instance from the given input stream
	 */
	public static TsconfigJson load(InputStream in) {
		return load(in, TsconfigJson.class);
	}

}
