package ts.server.protocol;

import com.eclipsesource.json.JsonObject;

/**
 * Arguments for FileRequest messages.
 */
public class FileRequestArgs extends JsonObject {

	public FileRequestArgs(String fileName) {
		super.add("file", fileName);
	}

}
