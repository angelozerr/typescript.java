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

import ts.client.IKindProvider;

public interface ICompletionEntry extends IKindProvider {

	ICompletionEntry[] EMPTY_ENTRIES = new ICompletionEntry[0];

	String getName();

	String getSortText();

	/**
	 * Returns the relevance of this completion proposal.
	 * <p>
	 * The relevance is used to determine if this proposal is more relevant than
	 * another proposal.
	 * </p>
	 *
	 * @return the relevance of this completion proposal in the range of [0,
	 *         100]
	 */
	int getRelevance();

	boolean updatePrefix(String prefix);
}
