package ts.server.protocol;

import java.util.concurrent.Callable;

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;

import ts.TSException;
import ts.internal.SequenceHelper;

/**
 * Client-initiated request message
 */
public class Request extends Message implements Callable<JsonObject> {

	private JsonObject response;

	public Request(CommandNames command, JsonObject args, Integer seq) {
		this(command.getName(), args, seq);
	}

	public Request(String command, JsonObject args, Integer seq) {
		super(seq != null ? seq : SequenceHelper.getRequestSeq(), "request");
		super.add("command", command);
		if (args != null) {
			super.add("arguments", args);
		}
	}

	public String getCommand() {
		return super.getString("command", null);
	}

	public JsonObject getArguments() {
		return super.get("arguments").asObject();
	}

	public void setResponse(JsonObject response) {
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
				this.wait(200);
			}
		}
		if (!response.getBoolean("success", true)) {
			throw new TSException(response.getString("message", ""));
		}
		return response;
	}

	public Object getResponseKey() {
		return getSeq();
	}
}
