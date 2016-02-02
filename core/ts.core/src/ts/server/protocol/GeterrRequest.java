package ts.server.protocol;

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;

import ts.server.geterr.ITypeScriptGeterrCollector;

/**
 * Geterr request; value of command field is "geterr". Wait for delay
 * milliseconds and then, if during the wait no change or reload messages have
 * arrived for the first file in the files list, get the syntactic errors for
 * the file, field requests, and then get the semantic errors for the file.
 * Repeat with a smaller delay for each subsequent file on the files list. Best
 * practice for an editor is to send a file list containing each file that is
 * currently visible, in most-recently-used order.
 */
public class GeterrRequest extends Request {

	private final ITypeScriptGeterrCollector collector;
	private final String[] files;

	public GeterrRequest(String[] files, int delay, ITypeScriptGeterrCollector collector) {
		super(CommandNames.Geterr, new GeterrRequestArgs(files, delay), null);
		this.files = files;
		this.collector = collector;
	}

	public JsonArray getFiles() {
		return ((JsonArray) ((GeterrRequestArgs) getArguments()).getFiles());
	}

	@Override
	public Object getResponseKey() {
		return files;
	}

	@Override
	public void setResponse(JsonObject response) {
		String event = response.getString("event", null);
		JsonObject body = response.get("body").asObject();
		String file = body.getString("file", null);
		JsonArray diagnostics = body.get("diagnostics").asArray();
		JsonObject diagnostic = null;
		String text = null;
		JsonObject start = null;
		JsonObject end = null;
		for (JsonValue value : diagnostics) {
			diagnostic = value.asObject();
			text = diagnostic.getString("text", null);
			start = diagnostic.get("start").asObject();
			end = diagnostic.get("end").asObject();
			collector.addDiagnostic(event, file, text, start.getInt("line", -1), start.getInt("offset", -1),
					end.getInt("line", -1), end.getInt("offset", -1));
		}
		super.setResponse(response);
	}
}
