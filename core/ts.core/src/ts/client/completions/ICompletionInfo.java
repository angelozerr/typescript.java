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
package ts.client.completions;

/**
 * @see https://github.com/Microsoft/TypeScript/blob/master/src/services/services.ts
 *
 */
public interface ICompletionInfo {

	boolean isMemberCompletion();
	/**
	 * Returns true when the current location also allows for a new identifier
	 * @return true when the current location also allows for a new identifier
	 */
	boolean isNewIdentifierLocation();  
	
	ICompletionEntry[] getEntries();
}
