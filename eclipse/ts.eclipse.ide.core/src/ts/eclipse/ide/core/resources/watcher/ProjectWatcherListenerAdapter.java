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
 * Adapter class for {@link IProjectWatcherListener}.
 *
 */
public class ProjectWatcherListenerAdapter implements IProjectWatcherListener {

	public ProjectWatcherListenerAdapter() {
	}

	@Override
	public void onOpened(IProject project) {

	}

	@Override
	public void onClosed(IProject project) {

	}

	@Override
	public void onDeleted(IProject project) {

	}

}
