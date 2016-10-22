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
package ts.client;

/**
 * Command names of tsserver.
 *
 */
public enum CommandNames {

	Open("open"), Close("close"), Change("change"), NavBar("navbar"), Completions(
			"completions"), CompletionEntryDetails("completionEntryDetails"), Reload("reload"), Definition(
					"definition"), SignatureHelp("signatureHelp"), QuickInfo("quickinfo"), Geterr("geterr"), Format(
							"format"), References("references"), Occurrences("occurrences"), Configure(
									"configure"), SemanticDiagnosticsSync("semanticDiagnosticsSync",
											"2.0.3"), SyntacticDiagnosticsSync("syntacticDiagnosticsSync",
													"2.0.3"), Implementation("implementation", "2.0.6"), NavTree(
															"navtree", "2.0.6"), GetCodeFixes("getCodeFixes", "2.1.0");

	private final String name;
	private final String sinceVersion;

	private CommandNames(String name) {
		this(name, null);
	}

	private CommandNames(String name, String sinceVersion) {
		this.name = name;
		this.sinceVersion = sinceVersion;
	}

	public String getName() {
		return name;
	}

	/**
	 * Return true if the tsserver command support the given version and false
	 * otherwise.
	 * 
	 * @param version
	 * @return true if the tsserver command support the given version and false
	 *         otherwise.
	 */
	public boolean canSupport(String version) {
		if (sinceVersion == null) {
			return true;
		}
		return versionCompare(version, sinceVersion) >= 0;
	}

	/**
	 * Compares two version strings.
	 * 
	 * Use this instead of String.compareTo() for a non-lexicographical
	 * comparison that works for version strings. e.g. "1.10".compareTo("1.6").
	 * 
	 * @note It does not work if "1.10" is supposed to be equal to "1.10.0".
	 * 
	 * @param str1
	 *            a string of ordinal numbers separated by decimal points.
	 * @param str2
	 *            a string of ordinal numbers separated by decimal points.
	 * @return The result is a negative integer if str1 is _numerically_ less
	 *         than str2. The result is a positive integer if str1 is
	 *         _numerically_ greater than str2. The result is zero if the
	 *         strings are _numerically_ equal.
	 */
	private static int versionCompare(String str1, String str2) {
		String[] vals1 = str1.split("\\.");
		String[] vals2 = str2.split("\\.");
		int i = 0;
		// set index to first non-equal ordinal or length of shortest version
		// string
		while (i < vals1.length && i < vals2.length && vals1[i].equals(vals2[i])) {
			i++;
		}
		// compare first non-equal ordinal number
		if (i < vals1.length && i < vals2.length) {
			int diff = Integer.valueOf(vals1[i]).compareTo(Integer.valueOf(vals2[i]));
			return Integer.signum(diff);
		}
		// the strings are equal or one string is a substring of the other
		// e.g. "1.2.3" = "1.2.3" or "1.2.3" < "1.2.3.4"
		return Integer.signum(vals1.length - vals2.length);
	}

}
