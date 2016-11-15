package ts.internal.client.protocol;

import com.eclipsesource.json.JsonObject;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import ts.TypeScriptException;
import ts.client.CommandNames;
import ts.client.IPositionProvider;
import ts.client.codefixes.ITypeScriptGetCodeFixesCollector;

public class CodeFixRequest extends SimpleRequest<ITypeScriptGetCodeFixesCollector> {

	public CodeFixRequest(String fileName, IPositionProvider positionProvider, int startLine, int startOffset,
			int endLine, int endOffset, String[] errorCodes, ITypeScriptGetCodeFixesCollector collector) {
		super(CommandNames.GetCodeFixes,
				new CodeFixRequestArgs(fileName, startLine, startOffset, endLine, endOffset, errorCodes), null);
		super.setCollector(collector);
	}

	@Override
	public void collect(JsonObject response) throws TypeScriptException {
		Gson gson = new GsonBuilder().create();
		CodeFixResponse a = gson.fromJson(response.toString(), CodeFixResponse.class);
		getCollector().fix(a.getBody());
	}
}
