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

import org.eclipse.core.resources.IFile;

/**
 * Adapter class for {@link IFileWatcherListener}.
 *
 */
public class FileWatcherListenerAdapter implements IFileWatcherListener {

	@Override
	public void onDeleted(IFile file) {

	}

	@Override
	public void onAdded(IFile file) {

	}

	@Override
	public void onChanged(IFile file) {

	}

}
