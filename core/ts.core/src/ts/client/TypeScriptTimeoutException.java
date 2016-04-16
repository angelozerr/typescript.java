package ts.client;

import ts.TypeScriptException;
import ts.internal.client.protocol.Request;

public class TypeScriptTimeoutException extends TypeScriptException {

	private final Request request;

	public TypeScriptTimeoutException(Request request, long timeout) {
		super("Timeout error " + timeout + "ms");
		this.request = request;
	}

	public Request getRequest() {
		return request;
	}
}
