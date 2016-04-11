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
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

import ts.eclipse.ide.core.resources.jsconfig.IDETsconfigJson;
import ts.eclipse.ide.core.utils.WorkbenchResourceUtil;
import ts.utils.FileUtils;

/**
 * JSON configuration (tsconfig.json, package.json) file manager.
 *
 */
public class JsonConfigResourcesManager {

	private static final JsonConfigResourcesManager INSTANCE = new JsonConfigResourcesManager();

	private static final IPath TSCONFIG_JSON_PATH = new Path(FileUtils.TSCONFIG_JSON);
	private static final IPath JSCONFIG_JSON_PATH = new Path(FileUtils.JSCONFIG_JSON);

	public static JsonConfigResourcesManager getInstance() {
		return INSTANCE;
	}

	private final Map<IFile, IDETsconfigJson> jsconConfig;

	public JsonConfigResourcesManager() {
		this.jsconConfig = new HashMap<IFile, IDETsconfigJson>();
	}

	/**
	 * Remove the given tsconfig.json from the cache.
	 * 
	 * @param file
	 */
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
	public IDETsconfigJson findTsconfig(IResource resource) throws CoreException {
		IFile tsconfigFile = findTsconfigFile(resource);
		if (tsconfigFile != null) {
			return getTsconfig(tsconfigFile);
		}
		return null;
	}

	public IFile findTsconfigFile(IResource resource) throws CoreException {
		IFile tsconfigFile = WorkbenchResourceUtil.findFileInContainerOrParent(resource, TSCONFIG_JSON_PATH);
		return tsconfigFile;
	}

	/**
	 * Returns the Pojo of the given tsconfig.json file.
	 * 
	 * @param tsconfigFile
	 * @return the Pojo of the given tsconfig.json file.
	 * @throws CoreException
	 */
	private IDETsconfigJson getTsconfig(IFile tsconfigFile) throws CoreException {
		IDETsconfigJson tsconfig = jsconConfig.get(tsconfigFile);
		if (tsconfig == null) {
			return createTsConfig(tsconfigFile);
		}
		return tsconfig;
	}

	/**
	 * Create Pojo instance of the given tsconfig.json file.
	 * 
	 * @param tsconfigFile
	 * @return Pojo instance of the given tsconfig.json file.
	 * @throws CoreException
	 */
	private synchronized IDETsconfigJson createTsConfig(IFile tsconfigFile) throws CoreException {
		IDETsconfigJson tsconfig = jsconConfig.get(tsconfigFile);
		if (tsconfig != null) {
			return tsconfig;
		}

		tsconfig = IDETsconfigJson.load(tsconfigFile);
		synchronized (jsconConfig) {
			jsconConfig.put(tsconfigFile, tsconfig);
		}
		return tsconfig;
	}

	/**
	 * Find jsconfig.json
	 * 
	 * @param resource
	 * @return
	 * @throws CoreException
	 */
	public IFile findJsconfigFile(IResource resource) throws CoreException {
		return WorkbenchResourceUtil.findFileInContainerOrParent(resource, JSCONFIG_JSON_PATH);
	}

}
