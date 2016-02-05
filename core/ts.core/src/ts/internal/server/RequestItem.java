package ts.internal.server;

import ts.server.protocol.Request;

public class RequestItem {

	public final Request request;
	public final ICallbackItem callbacks;

	public RequestItem(Request request, ICallbackItem callbacks) {
		this.request = request;
		this.callbacks = callbacks;
	}
}
