package ts.client.definition;

import ts.TypeScriptException;
import ts.client.AbstractTypeScriptCollector;

public abstract class AbstractDefinitionCollector extends AbstractTypeScriptCollector implements ITypeScriptDefinitionCollector {

	@Override
	public final void addDefinition(String file, int startLine, int startOffset, int endLine, int endOffset)
			throws TypeScriptException {
		if (file != null && startLine != -1 && startOffset != -1 && endLine != -1 && endOffset != -1) {
			doAddDefinition(file, startLine, startOffset, endLine, endOffset);
		}

	}

	protected abstract void doAddDefinition(String file, int startLine, int startOffset, int endLine, int endOffset)
			throws TypeScriptException;

}
