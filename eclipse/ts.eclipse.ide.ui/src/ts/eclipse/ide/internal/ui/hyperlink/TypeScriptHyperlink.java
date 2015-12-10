package ts.eclipse.ide.internal.ui.hyperlink;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;

import ts.eclipse.ide.core.resources.IIDETypeScriptProject;
import ts.eclipse.ide.internal.ui.TypeScriptUIMessages;
import ts.eclipse.ide.ui.hyperlink.AbstractTypeScriptHyperlink;
import ts.resources.ITypeScriptFile;

public class TypeScriptHyperlink extends AbstractTypeScriptHyperlink {

	private final IDocument document;
	private final IResource resource;

	public TypeScriptHyperlink(IDocument document, IRegion region, IResource resource,
			IIDETypeScriptProject tsProject) {
		super(region, tsProject);
		this.document = document;
		this.resource = resource;
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
		ITypeScriptFile tsFile = tsProject.openFile(resource, document);
		int position = region.getOffset();
		tsProject.definition(tsFile, position, this);
	}
}
