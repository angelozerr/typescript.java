/**
 *  Copyright (c) 2013-2016 Angelo ZERR.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package ts.server;

import com.eclipsesource.json.JsonObject;

import ts.server.protocol.Request;

public class LoggingInterceptor implements IInterceptor {

	private static final IInterceptor INSTANCE = new LoggingInterceptor();

	public static IInterceptor getInstance() {
		return INSTANCE;
	}

	@Override
	public void handleRequest(Request request, ITypeScriptServiceClient server,
			String methodName) {
		outPrintln("-----------------------------------");
		outPrintln("TypeScript request#" + methodName + ": ");
		outPrintln(request.toString());
	}

	@Override
	public void handleResponse(JsonObject response, ITypeScriptServiceClient server,
			String methodName, long ellapsedTime) {
		outPrintln("");
		outPrintln("TypeScript response#" + methodName + " with " + ellapsedTime
				+ "ms: ");
		outPrintln(response.toString());
		outPrintln("-----------------------------------");
	}

	@Override
	public void handleError(Throwable error, ITypeScriptServiceClient server,
			String methodName, long ellapsedTime) {
		errPrintln("");
		errPrintln("TypeScript error#" + methodName + " with " + ellapsedTime
				+ "ms: ");
		printStackTrace(error);
		errPrintln("-----------------------------------");
	}

	protected void outPrintln(String line) {
		System.out.println(line);
	}

	protected void errPrintln(String line) {
		System.err.println(line);
	}

	protected void printStackTrace(Throwable error) {
		error.printStackTrace(System.err);
	}

	
	
}
