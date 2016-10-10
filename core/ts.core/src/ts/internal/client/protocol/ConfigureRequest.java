package ts.internal.client.protocol;

import com.eclipsesource.json.JsonObject;

import ts.TypeScriptException;
import ts.client.CommandNames;

public class ConfigureRequest extends SimpleRequest {

	public ConfigureRequest(ConfigureRequestArguments arguments) {
		super(CommandNames.Configure, arguments, null);
	}

	@Override
	public void collect(JsonObject response) throws TypeScriptException {

	}

}
