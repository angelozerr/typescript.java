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

import ts.internal.matcher.LCSS;

/**
 * Matcher for completion entry.
 * 
 */
public interface ICompletionEntryMatcher {

	public static ICompletionEntryMatcher LCS = new ICompletionEntryMatcher() {

		@Override
		public boolean match(String completion, String token) {
			return LCSS.containsSubsequence(completion, token);
		}

		@Override
		public int[] bestSubsequence(String completion, String token) {
			return LCSS.bestSubsequence(completion, token);
		}
	};
	public static ICompletionEntryMatcher START_WITH_MATCHER = new ICompletionEntryMatcher() {

		@Override
		public boolean match(String completion, String token) {
			return completion.startsWith(token);
		}

		@Override
		public int[] bestSubsequence(String completion, String token) {
			return new int[] { 0, token.length() - 1 };
		}
	};

	/**
	 * Returns true if the given completion entry name match the given token and
	 * false otherwise.
	 * 
	 * @param completion
	 * @param token
	 * @return true if the given completion entry name match the given token and
	 *         false otherwise.
	 */
	boolean match(String completion, String token);

	int[] bestSubsequence(String completion, String token);

}
