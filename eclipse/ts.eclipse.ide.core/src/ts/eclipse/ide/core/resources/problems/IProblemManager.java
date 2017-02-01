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

/**
 * Manager for getting updates about the global state of TypeScript problems on
 * workspace resources.
 */
public interface IProblemManager {

	/**
	 * Adds a listener to be notified when problems change on workspace
	 * resources.
	 * 
	 * @param listener
	 *            listener to notify.
	 */
	public void addProblemChangedListener(IProblemChangeListener listener);

	/**
	 * Removes a problem change listener previously added via
	 * {@link #addProblemChangedListener}.
	 * 
	 * @param listener
	 *            listener to remove.
	 */
	public void removeProblemChangedListener(IProblemChangeListener listener);

}
