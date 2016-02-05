package ts.server.protocol;

import com.eclipsesource.json.JsonObject;

import ts.TSException;

/**
 * Client-initiated request message
 */
public class SimpleRequest extends Request<JsonObject> {

	private JsonObject response;

	public SimpleRequest(CommandNames command, JsonObject args, Integer seq) {
		super(command, args, seq);
	}

	public SimpleRequest(String command, JsonObject args, Integer seq) {
		super(command, args, seq);
	}

	public void handleResponse(JsonObject response) {
		this.response = response;
		synchronized (this) {
			this.notifyAll();
		}
	}

	@Override
	public void complete(JsonObject response) {
		this.response = response;
		synchronized (this) {
			this.notifyAll();
		}
	}

	@Override
	protected boolean isCompleted() {
		return response != null;
	}

	@Override
	protected JsonObject getResult() throws Exception {
		if (!response.getBoolean("success", true)) {
			throw new TSException(response.getString("message", ""));
		}
		return response;
	}

}
