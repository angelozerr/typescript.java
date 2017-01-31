/**
 *  Copyright (c) 2015-2016 Angelo ZERR.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Lorenzo Dalla Vecchia <lorenzo.dallavecchia@webratio.com> - initial API and implementation
 */
package ts.eclipse.ide.core.resources.problems;

import java.util.Set;

import org.eclipse.core.resources.IResource;

/**
 * Listener that is notified after changes occur in the TypeScript problems of
 * workspace resources.
 */
public interface IProblemChangeListener {

	/**
	 * Called when problems change.
	 * 
	 * @param changedResources
	 *            set of resources on which problems have changed. This also
	 *            includes containers where the <i>combined</i> problem status
	 *            may have changed because of changes in the contained files or
	 *            sub-containers.
	 */
	public void problemsChanged(Set<IResource> changedResources);

}
