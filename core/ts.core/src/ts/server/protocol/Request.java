package ts.server.protocol;

import java.util.concurrent.Callable;

import com.eclipsesource.json.JsonObject;

import ts.internal.SequenceHelper;

public abstract class Request extends Message implements Callable<JsonObject> {

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

}
