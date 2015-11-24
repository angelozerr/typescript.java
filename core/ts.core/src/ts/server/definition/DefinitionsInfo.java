package ts.server.definition;

import ts.TSException;

public class DefinitionsInfo extends AbstractDefinitionCollector {

	@Override
	public void doAddDefinition(String file, int startLine, int startOffset, int endLine, int endOffset)
			throws TSException {
		System.err.println("file:" + file + ", startLine:" + startLine + ", startOffset:" + startOffset + ", endLine:"
				+ endLine + ", endOffset:" + endOffset);
	}
}
