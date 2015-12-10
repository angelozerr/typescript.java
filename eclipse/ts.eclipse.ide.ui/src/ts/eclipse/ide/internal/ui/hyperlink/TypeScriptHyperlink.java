package ts.eclipse.ide.internal.ui.hyperlink;

import org.eclipse.jface.text.IRegion;

import ts.eclipse.ide.core.resources.IIDETypeScriptFile;
import ts.eclipse.ide.core.resources.IIDETypeScriptProject;
import ts.eclipse.ide.internal.ui.TypeScriptUIMessages;
import ts.eclipse.ide.ui.hyperlink.AbstractTypeScriptHyperlink;

public class TypeScriptHyperlink extends AbstractTypeScriptHyperlink {

	private final IIDETypeScriptFile tsFile;

	public TypeScriptHyperlink(IIDETypeScriptFile tsFile, IRegion region) {
		super(region, (IIDETypeScriptProject) tsFile.getProject());
		this.tsFile = tsFile;
	}

	@Override
	public String getTypeLabel() {
		return TypeScriptUIMessages.TypeScriptHyperlink_typeLabel;
	}

	@Override
	public String getHyperlinkText() {
		return TypeScriptUIMessages.TypeScriptHyperlink_text;
	}

	@Override
	protected void findDef() throws Exception {
		int position = region.getOffset();
		tsFile.definition(position, this);
	}
}
