package ts.server.protocol;

import com.eclipsesource.json.JsonObject;

import ts.TypeScriptException;

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

	@Override
	public boolean complete(JsonObject response) {
		this.response = response;
		synchronized (this) {
			this.notifyAll();
		}
		return isCompleted();
	}

	@Override
	protected boolean isCompleted() {
		return response != null;
	}

	@Override
	protected JsonObject getResult() throws Exception {
		if (!response.getBoolean("success", true)) {
			throw new TypeScriptException(response.getString("message", ""));
		}
		return response;
	}

}
