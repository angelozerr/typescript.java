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

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

import ts.compiler.CompilerOptions;
import ts.eclipse.ide.core.utils.WorkbenchResourceUtil;
import ts.eclipse.ide.internal.core.Trace;
import ts.resources.jsonconfig.TsconfigJson;
import ts.utils.StringUtils;

/**
 * tsconfig.json loade from {@link IFile} tsconfig.json.
 *
 */
public class IDETsconfigJson extends TsconfigJson {

	private IFile tsconfigFile;
	private IContainer outDir;

	/**
	 * Load tsconfig.json.
	 * 
	 * @param tsconfigFile
	 * @return
	 * @throws CoreException
	 */
	public static IDETsconfigJson load(IFile tsconfigFile) throws CoreException {
		IDETsconfigJson tsconfig = load(tsconfigFile.getContents(), IDETsconfigJson.class);
		tsconfig.tsconfigFile = tsconfigFile;
		tsconfig.outDir = computeOutDir(tsconfig);
		return tsconfig;
	}

	/**
	 * Returns the tsconfig.json Eclipse file.
	 * 
	 * @return the tsconfig.json Eclipse file.
	 */
	public IFile getTsconfigFile() {
		return tsconfigFile;
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
		String filename = getRelativePath(resource).toString();
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
		String filename = getRelativePath(resource).toString();
		for (String exclude : getExclude()) {
			if (filename.startsWith(exclude)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns the relative path of the given resource to tsconfig.json folder.
	 * 
	 * @param resource
	 * @return the relative path of the given resource to tsconfig.json folder.
	 */
	public IPath getRelativePath(IResource resource) {
		return WorkbenchResourceUtil.getRelativePath(resource, tsconfigFile.getParent());
	}

	/**
	 * Returns the compilerOptions/outDir as Eclipse folder and null otherwise.
	 * 
	 * @return the compilerOptions/outDir as Eclipse folder and null otherwise.
	 */
	public IContainer getOutDirContainer() {
		return outDir;
	}

	/**
	 * Compute compilerOptions/outDir as Eclipse folder and null otherwise.
	 * 
	 * @return compilerOptions/outDir as Eclipse folder and null otherwise
	 */
	private static IContainer computeOutDir(IDETsconfigJson tsconfig) {
		CompilerOptions options = tsconfig.getCompilerOptions();
		if (options != null) {
			String outDir = options.getOutDir();
			if (StringUtils.isEmpty(outDir)) {
				return null;
			}
			try {
				return tsconfig.getTsconfigFile().getParent().getFolder(new Path(outDir));
			} catch (Throwable e) {
				Trace.trace(Trace.SEVERE, "Error while getting compilerOption/outDir", e);
			}
		}
		return null;
	}

}
