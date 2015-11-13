package ts.server.protocol;

import com.eclipsesource.json.JsonObject;

/**
 * A TypeScript Server message
 */
public class Message extends JsonObject {

	public Message(int seq, String type) {
		super.add("seq", seq);
		super.add("type", type);
	}

	public int getSeq() {
		return getInt("seq", 0);
	}
}
