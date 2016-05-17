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
import ts.client.ITypeScriptServiceClient;

public interface ITypeScriptCompletionCollector extends ITypeScriptCollector {

	void addCompletionEntry(String name, String kind, String kindModifiers, String sortText,
			String fileName, int line, int offset, ITypeScriptServiceClient client);

}
