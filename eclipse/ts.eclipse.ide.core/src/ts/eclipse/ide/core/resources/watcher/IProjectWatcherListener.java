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
package ts.eclipse.ide.core.resources.watcher;

import org.eclipse.core.resources.IProject;

/**
 * Listener API to observe when project is opened, closed and deleted.
 *
 */
public interface IProjectWatcherListener {

	/**
	 * Call when the given project is opened.
	 * 
	 * @param project
	 */
	void onOpened(IProject project);

	/**
	 * Call when the given project is closed.
	 * 
	 * @param project
	 */
	void onClosed(IProject project);

	/**
	 * Call when the given project is deleted.
	 * 
	 * @param project
	 */
	void onDeleted(IProject project);

}
