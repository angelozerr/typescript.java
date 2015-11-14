/**
 *  Copyright (c) 2013-2015 Angelo ZERR.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package ts.doc;

import ts.server.ITSClient;

public abstract class AbstractJSDocument implements IJSDocument {

	private final ITSClient client;
	private final String name;
	private boolean changed;

	public AbstractJSDocument(String name, ITSClient client) {
		this(name, client, false);
	}

	public AbstractJSDocument(String name, ITSClient client, boolean register) {
		this.name = name;
		this.client = client;
		this.changed = false;
		if (register) {
			//JSDocumentHelper.registerDoc(this, client);
		}
	}

	@Override
	public String getName() {
		return name;
	}

	public boolean isChanged() {
		return changed;
	}

	public void setChanged(boolean changed) {
		this.changed = changed;
	}

	@Override
	public ITSClient getClient() {
		return client;
	}
}
