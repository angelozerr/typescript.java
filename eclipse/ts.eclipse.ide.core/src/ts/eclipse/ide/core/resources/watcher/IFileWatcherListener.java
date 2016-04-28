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
 * Listener API to observe when file is deleted, created and changed.
 *
 */
public interface IFileWatcherListener {

	void onDeleted(IFile file);

	void onAdded(IFile file);

	void onChanged(IFile file);

}
