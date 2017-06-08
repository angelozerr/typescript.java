/**
 *  Copyright (c) 2015-2017 Angelo ZERR.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package ts.eclipse.ide.core.resources;

import org.eclipse.core.resources.IProject;

/**
 * Extension point to know if a given resource can consume tsserver or not.
 *
 */
public interface ITypeScriptResourceParticipant {

	/**
	 * Returns true if the given resource can consume tsserver and false
	 * otherwise.
	 * 
	 * @param project
	 * @param fileObject
	 * @return true if the given resource can consume tsserver and false
	 *         otherwise.
	 */
	boolean canConsumeTsserver(IProject project, Object fileObject);

}
