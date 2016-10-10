package ts.internal.client.protocol;

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;

import ts.TypeScriptException;
import ts.client.CommandNames;
import ts.client.definition.ITypeScriptDefinitionCollector;

/**
 * Go to implementation request; value of command field is "implementation".
 * Return response giving the file locations that implement the symbol found in
 * file at location line, col.
 */
public class ImplementationRequest extends FileLocationRequest<ITypeScriptDefinitionCollector> {

	public ImplementationRequest(String fileName, int line, int offset, ITypeScriptDefinitionCollector collector) {
		super(CommandNames.Implementation, new FileLocationRequestArgs(fileName, line, offset));
		super.setCollector(collector);
	}

	@Override
	public void collect(JsonObject response) throws TypeScriptException {
		ITypeScriptDefinitionCollector collector = super.getCollector();
		JsonArray items = response.get("body").asArray();
		JsonObject def = null;
		JsonObject start = null;
		JsonObject end = null;
		for (JsonValue item : items) {
			def = (JsonObject) item;
			start = def.get("start").asObject();
			end = def.get("end").asObject();
			collector.addDefinition(def.getString("file", null), start.getInt("line", -1), start.getInt("offset", -1),
					end.getInt("line", -1), end.getInt("offset", -1));
		}
	}

}
