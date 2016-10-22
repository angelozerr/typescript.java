package ts.internal.client.protocol;

import com.eclipsesource.json.JsonObject;

import ts.TypeScriptException;
import ts.client.CommandNames;
import ts.client.IPositionProvider;
import ts.client.codefixes.ITypeScriptGetCodeFixesCollector;

public class CodeFixRequest extends SimpleRequest<ITypeScriptGetCodeFixesCollector> {

	public CodeFixRequest(String fileName, IPositionProvider positionProvider, int startLine, int startOffset,
			int endLine, int endOffset, ITypeScriptGetCodeFixesCollector collector) {
		super(CommandNames.GetCodeFixes, new CodeFixRequestArgs(fileName, startLine, startOffset, endLine, endOffset),
				null);
		super.setCollector(collector);
	}

	@Override
	public void collect(JsonObject response) throws TypeScriptException {
		System.err.println(response);
	}
}
