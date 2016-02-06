package ts.client.protocol;

import com.eclipsesource.json.JsonObject;

/**
 * Arguments for FileRequest messages.
 */
public class FileRequestArgs extends JsonObject {

	/**
	 * 
	 * @param file
	 *            The file for the request (absolute pathname required).
	 */
	public FileRequestArgs(String file) {
		super.add("file", file);
	}

}
