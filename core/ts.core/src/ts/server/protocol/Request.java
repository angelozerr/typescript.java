package ts.server.protocol;

import com.eclipsesource.json.JsonObject;

/**
 * Client-initiated request message
 */
public class Request extends Message {

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

}
