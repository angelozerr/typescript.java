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

import ts.client.ITypeScriptCollector;

/**
 * Collector to collect CompletionEntryDetails.
 *
 */
public interface ITypeScriptCompletionEntryDetailsCollector extends ITypeScriptCollector {

	void setEntryDetails(String name, String kind, String kindModifiers);

	void addDisplayPart(String text, String kind);

	void addDocumentation(String text, String kind);

}
