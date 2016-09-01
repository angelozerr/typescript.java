package ts.eclipse.ide.jsdt.internal.ui.editor;

import ts.eclipse.ide.ui.folding.IndentFoldingStrategy;

/**
 * Folding strategy with indent and line which starts with "import".
 *
 */
public class TypeScriptFoldingStrategy extends IndentFoldingStrategy {

	private static final String IMPORT = "import";

	public TypeScriptFoldingStrategy() {
		super(IMPORT);
	}
}
