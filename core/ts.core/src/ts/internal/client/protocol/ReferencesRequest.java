package ts.internal.client.protocol;

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;

import ts.TypeScriptException;
import ts.client.references.ITypeScriptReferencesCollector;

public class ReferencesRequest extends FileLocationRequest<ITypeScriptReferencesCollector> {

	public ReferencesRequest(String fileName, int line, int offset, ITypeScriptReferencesCollector collector) {
		super(CommandNames.References, new FileLocationRequestArgs(fileName, line, offset));
		super.setCollector(collector);
	}

	@Override
	public void collect(JsonObject response) throws TypeScriptException {
		ITypeScriptReferencesCollector collector = super.getCollector();
		JsonObject body = response.get("body").asObject();
		JsonArray refs = body.get("refs").asArray();
		JsonObject ref = null;
		String file = null;
		JsonObject start = null;
		JsonObject end = null;
		String lineText = null;
		for (JsonValue r : refs) {
			ref = r.asObject();
			file = ref.getString("file", null);
			start = ref.get("start").asObject();
			end = ref.get("end").asObject();
			lineText = ref.getString("lineText", null);
			collector.ref(file, start.getInt("line", -1), start.getInt("offset", -1), end.getInt("line", -1),
					end.getInt("offset", -1), lineText);

		}
	}

}
