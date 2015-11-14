package ts.server.protocol;

import java.util.concurrent.Callable;

import com.eclipsesource.json.JsonObject;

import ts.TSException;

/**
 * Client-initiated request message
 */
public class Request extends Message implements Callable<JsonObject> {

	private JsonObject response;

	public Request(CommandNames command, FileRequestArgs args, ISequenceProvider provider) {
		this(command.getName(), args, provider);
	}

	public Request(String command, JsonObject args, ISequenceProvider provider) {
		super(provider.getSequence(), "request");
		super.add("command", command);
		if (args != null) {
			super.add("arguments", args);
		}
	}

	public void setResponse(JsonObject response) {
		this.response = response;
		synchronized (this) {
			notify();
		}
	}

	@Override
	public JsonObject call() throws Exception {
		synchronized (this) {
			wait();
		}
		if (!response.getBoolean("success", true)) {
			throw new TSException(response.getString("message", ""));
		}
		return response;
	}

}
