package ts.internal.client.protocol;

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;

import ts.TypeScriptException;
import ts.client.diagnostics.ITypeScriptDiagnosticsCollector;

/**
 * Synchronous request for syntactic diagnostics of one file.
 */
public class SyntacticDiagnosticsSyncRequest extends FileRequest<ITypeScriptDiagnosticsCollector> {

	private final String fileName;

	public SyntacticDiagnosticsSyncRequest(String fileName, Boolean includeLinePosition,
			ITypeScriptDiagnosticsCollector collector) {
		super(CommandNames.SyntacticDiagnosticsSync.getName(),
				new SyntacticDiagnosticsSyncRequestArgs(fileName, includeLinePosition), null);
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
		for (JsonValue value : body) {
			diagnostic = value.asObject();
			text = diagnostic.getString("text", null);
			start = diagnostic.get("start").asObject();
			end = diagnostic.get("end").asObject();
			getCollector().addDiagnostic(null, fileName, text, start.getInt("line", -1), start.getInt("offset", -1),
					end.getInt("line", -1), end.getInt("offset", -1));
		}
	}
}
