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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Pojo for tsconfig.json
 * 
 * @see http://www.typescriptlang.org/docs/handbook/tsconfig.json.html
 *
 */
public class TsconfigJson {

	private boolean compileOnSave;

	public boolean isCompileOnSave() {
		return compileOnSave;
	}

	public void setCompileOnSave(boolean compileOnSave) {
		this.compileOnSave = compileOnSave;
	}

	/**
	 * Load tsconfig.json instance from the given reader.
	 * 
	 * @param reader
	 * @return tsconfig.json instance from the given reader.
	 */
	public static TsconfigJson load(Reader reader) {
		Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
		return gson.fromJson(reader, TsconfigJson.class);
	}

	/**
	 * Load tsconfig.json instance from the given input stream.
	 * 
	 * @param in
	 * @return tsconfig.json instance from the given input stream
	 */
	public static TsconfigJson load(InputStream in) {
		Reader isr = null;
		try {
			isr = new InputStreamReader(in);
			return load(isr);
		} finally {
			if (isr != null) {
				try {
					isr.close();
				} catch (IOException e) {
				}
			}
		}
	}

}
