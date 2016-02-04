package ts.server.protocol;

import com.eclipsesource.json.JsonObject;

import ts.TSException;

/**
 * Client-initiated request message
 */
public class SimpleRequest extends Request {

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
	public JsonObject call() throws Exception {
		while (response == null) {
			synchronized (this) {
				// wait for 200ms otherwise if we don't set ms, if completion is
				// executed several times
				// quickly (do Ctrl+Space every time), the Thread could be
				// blocked? Why?
				this.wait(5);
			}
		}
		if (!response.getBoolean("success", true)) {
			throw new TSException(response.getString("message", ""));
		}
		return response;
	}

}
