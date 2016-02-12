package ts.client.protocol;

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;

import ts.utils.JSONUtils;

/**
 * Arguments for geterr messages.
 */
public class GeterrRequestArgs extends JsonObject {

	/**
	 * 
	 * @param files
	 *            List of file names for which to compute compiler errors. The
	 *            files will be checked in list order.
	 * @param delay
	 *            Delay in milliseconds to wait before starting to compute
	 *            errors for the files in the file list
	 */
	public GeterrRequestArgs(String[] files, int delay) {
		super.add("files", JSONUtils.toJson(files));
		super.add("delay", delay);
	}

	public JsonArray getFiles() {
		return super.get("files").asArray();
	}

	public int getDelay() {
		return super.getInt("delay", -1);
	}

}
