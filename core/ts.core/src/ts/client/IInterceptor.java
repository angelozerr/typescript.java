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

import com.eclipsesource.json.JsonObject;

import ts.client.protocol.Request;

public interface IInterceptor {

	void handleRequest(Request request, ITypeScriptServiceClient client, String methodName);

	void handleResponse(JsonObject response, ITypeScriptServiceClient client,
			String methodName, long ellapsedTime);

	void handleError(Throwable error, ITypeScriptServiceClient client, String methodName,
			long ellapsedTime);
}
