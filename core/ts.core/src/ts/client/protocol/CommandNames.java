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
package ts.client.protocol;

/**
 * Command names of tsserver.
 *
 */
public enum CommandNames {

	Open("open"), Close("close"), Change("change"), NavBar("navbar"), Completions(
			"completions"), CompletionEntryDetails("completionEntryDetails"), Reload("reload"), Definition(
					"definition"), SignatureHelp(
							"signatureHelp"), QuickInfo("quickinfo"), Geterr("geterr"), Format("format");

	private final String name;

	private CommandNames(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}
}
