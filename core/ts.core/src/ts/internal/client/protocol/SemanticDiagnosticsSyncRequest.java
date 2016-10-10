package ts.internal.client.protocol;

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;

import ts.TypeScriptException;
import ts.client.CommandNames;
import ts.client.diagnostics.ITypeScriptDiagnosticsCollector;

/**
 * Synchronous request for semantic diagnostics of one file.
 */
public class SemanticDiagnosticsSyncRequest extends FileRequest<ITypeScriptDiagnosticsCollector> {

	private final String fileName;

	public SemanticDiagnosticsSyncRequest(String fileName, Boolean includeLinePosition,
			ITypeScriptDiagnosticsCollector collector) {
		super(CommandNames.SemanticDiagnosticsSync.getName(),
				new SemanticDiagnosticsSyncRequestArgs(fileName, includeLinePosition), null);
		super.setCollector(collector);
		this.fileName = fileName;
	}

	@Override
	public void collect(JsonObject response) throws TypeScriptException {
		JsonArray body = response.get("body").asArray();

		JsonObject diagnostic = null;
		String text = null;
		JsonObject start = null;
		JsonObject end = null;
		JsonValue value = null;
		for (JsonValue item : body) {
			diagnostic = item.asObject();
			text = diagnostic.getString("text", null);
			value = diagnostic.get("startLocation");
			if (value == null) {
				value = diagnostic.get("start");
			}
			start = value.asObject();
			value = diagnostic.get("endLocation");
			if (value == null) {
				value = diagnostic.get("end");
			}
			end = value.asObject();
			getCollector().addDiagnostic(null, fileName, text, start.getInt("line", -1), start.getInt("offset", -1),
					end.getInt("line", -1), end.getInt("offset", -1));
		}
	}
}
