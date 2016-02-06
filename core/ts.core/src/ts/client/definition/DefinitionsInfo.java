package ts.client.definition;

import ts.TypeScriptException;

public class DefinitionsInfo extends AbstractDefinitionCollector {

	@Override
	public void doAddDefinition(String file, int startLine, int startOffset, int endLine, int endOffset)
			throws TypeScriptException {
		System.err.println("file:" + file + ", startLine:" + startLine + ", startOffset:" + startOffset + ", endLine:"
				+ endLine + ", endOffset:" + endOffset);
	}
}
