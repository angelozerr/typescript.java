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

import java.util.HashMap;
import java.util.Map;

import ts.OS;

/**
 * NPM modules manager.
 *
 */
public class NPMModulesManager {

	private final OS os;
	private final Map<String, NPMModule> modules;

	public NPMModulesManager(OS os) {
		this.os = os;
		this.modules = new HashMap<>();
	}

	public NPMModule getNPMModule(String moduleName) {
		NPMModule module = modules.get(moduleName);
		if (module == null) {
			module = new NPMModule(moduleName, os);
			modules.put(moduleName, module);
		}
		return module;
	}

	public void resetCache(String moduleName) {
		modules.remove(moduleName);
	}

}
