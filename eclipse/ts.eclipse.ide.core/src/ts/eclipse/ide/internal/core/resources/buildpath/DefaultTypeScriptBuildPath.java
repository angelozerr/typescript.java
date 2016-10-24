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
package ts.eclipse.ide.internal.core.resources.buildpath;

/**
 * Default TypeScript build path which search "tsconfig.json" from root of
 * project and "/src" folder..
 *
 */
public class DefaultTypeScriptBuildPath extends TypeScriptBuildPath {

	public DefaultTypeScriptBuildPath() {
		super(null);
		addEntry(new TypeScriptBuildPathEntry("/tsconfig.json"));
		addEntry(new TypeScriptBuildPathEntry("/src/tsconfig.json"));
	}

}
