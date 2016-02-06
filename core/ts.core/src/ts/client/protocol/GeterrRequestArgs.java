package ts.client.protocol;

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;

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
		JsonArray f = new JsonArray();
		for (int i = 0; i < files.length; i++) {
			f.add(files[i]);
		}
		super.add("files", f);
		super.add("delay", delay);
	}

	public JsonArray getFiles() {
		return super.get("files").asArray();
	}

	public int getDelay() {
		return super.getInt("delay", -1);
	}

}
