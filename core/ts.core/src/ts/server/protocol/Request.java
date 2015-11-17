package ts.server.protocol;

import java.util.concurrent.Callable;

import com.eclipsesource.json.JsonObject;

import ts.TSException;
import ts.internal.SequenceHelper;

/**
 * Client-initiated request message
 */
public class Request extends Message implements Callable<JsonObject> {

	private JsonObject response;

	public Request(CommandNames command, FileRequestArgs args, Integer seq) {
		this(command.getName(), args, seq);
	}

	public Request(String command, JsonObject args, Integer seq) {
		super(seq != null ? seq : SequenceHelper.getRequestSeq(), "request");
		super.add("command", command);
		if (args != null) {
			super.add("arguments", args);
		}
	}

	public void setResponse(JsonObject response) {
		System.out.println(response);
		this.response = response;
		synchronized (this) {
			notifyAll();
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
