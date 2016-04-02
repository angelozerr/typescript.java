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
package ts.eclipse.ide.internal.core.resources.jsonconfig;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

import ts.eclipse.ide.core.utils.WorkbenchResourceUtil;
import ts.resources.jsonconfig.TsconfigJson;
import ts.utils.FileUtils;

/**
 * JSON configuration (tsconfig.json, package.json) file manager.
 *
 */
public class JsonConfigResourcesManager {

	private static final JsonConfigResourcesManager INSTANCE = new JsonConfigResourcesManager();

	public static JsonConfigResourcesManager getInstance() {
		return INSTANCE;
	}

	private final Map<IFile, TsconfigJson> jsconConfig;

	public JsonConfigResourcesManager() {
		this.jsconConfig = new HashMap<IFile, TsconfigJson>();
	}

	public void remove(IFile file) {
		synchronized (jsconConfig) {
			jsconConfig.remove(file);
		}
	}

	/**
	 * Find tsconfig.json from the folder (or parent folder) of the given
	 * resource.
	 * 
	 * @param resource
	 * @return
	 * @throws CoreException
	 */
	public TsconfigJson findTsconfig(IResource resource) throws CoreException {
		IFile tsconfigFile = WorkbenchResourceUtil.findFileRecursively(resource, FileUtils.TSCONFIG_JSON);
		if (tsconfigFile != null) {
			return getTsconfig(tsconfigFile);
		}
		return null;
	}

	private TsconfigJson getTsconfig(IFile tsconfigFile) throws CoreException {
		TsconfigJson tsconfig = jsconConfig.get(tsconfigFile);
		if (tsconfig == null) {
			return createTsConfig(tsconfigFile);
		}
		return tsconfig;
	}

	private synchronized TsconfigJson createTsConfig(IFile tsconfigFile) throws CoreException {
		TsconfigJson tsconfig = jsconConfig.get(tsconfigFile);
		if (tsconfig != null) {
			return tsconfig;
		}

		tsconfig = TsconfigJson.load(tsconfigFile.getContents());
		synchronized (jsconConfig) {
			jsconConfig.put(tsconfigFile, tsconfig);
		}
		return tsconfig;
	}

}
