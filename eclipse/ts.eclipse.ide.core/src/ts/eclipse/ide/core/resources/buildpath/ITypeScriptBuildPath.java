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
package ts.eclipse.ide.core.resources.buildpath;

import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;

/**
 * TypeScript build path API.
 *
 */
public interface ITypeScriptBuildPath {

	/**
	 * Returns list of folders root of the project which hosts "tsconfig.json".
	 * 
	 * @return list of folders root of the project which hosts "tsconfig.json".
	 */
	List<IContainer> getContainers();

	/**
	 * Returns true if the given resource is in the scope of the build path and
	 * false otherwise.
	 * 
	 * @param resource
	 * @return true if the given resource is in the scope of the build path and
	 *         false otherwise.
	 */
	boolean isInScope(IResource resource);

	IContainer getContainer(IResource resource);

	void addEntry(ITypeScriptBuildPathEntry entry);

	void addEntry(IResource resource);

	void removeEntry(ITypeScriptBuildPathEntry entry);

	void removeEntry(IResource resource);

	boolean isContainer(IResource resource);

	ITypeScriptBuildPath copy();

	void save();

}
