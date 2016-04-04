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
package ts.eclipse.ide.core.resources.jsconfig;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

import ts.resources.jsonconfig.TsconfigJson;

/**
 * tsconfig.json loadef from {@link IFile} tsconfig.json.
 * 
 * @author azerr
 *
 */
public class IDETsconfigJson extends TsconfigJson {

	private IFile tsconfigFile;

	public IFile getTsconfigFile() {
		return tsconfigFile;
	}

	public static IDETsconfigJson load(IFile tsconfigFile) throws CoreException {
		IDETsconfigJson tsconfig = load(tsconfigFile.getContents(), IDETsconfigJson.class);
		tsconfig.tsconfigFile = tsconfigFile;
		return tsconfig;
	}

	/**
	 * Returns true if the given file is declared in the "files" section and
	 * false otherwise.
	 * 
	 * @param resource
	 * @return true if the given file is declared in the "files" section and
	 *         false otherwise.
	 */
	public boolean isInFiles(IResource resource) {
		if (!hasFiles()) {
			return false;
		}
		String filename = getRelativePath(resource);
		return getFiles().contains(filename);
	}

	/**
	 * Returns true if the given resource is excluded (declared as "exclude"
	 * section) and false otherwise.
	 * 
	 * @param resource
	 * @return true if the given resource is excluded (declared as "exclude"
	 *         section) and false otherwise.
	 */
	public boolean isExcluded(IResource resource) {
		if (!hasExclude()) {
			return false;
		}
		String filename = getRelativePath(resource);
		for (String exclude : getExclude()) {
			if (filename.startsWith(exclude)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns the relative path of teh given resource to tsconfig.json folder.
	 * 
	 * @param resource
	 * @return the relative path of teh given resource to tsconfig.json folder.
	 */
	private String getRelativePath(IResource resource) {
		IPath path = resource.getLocation().makeRelativeTo(tsconfigFile.getParent().getLocation());
		return path.toString();
	}

}
