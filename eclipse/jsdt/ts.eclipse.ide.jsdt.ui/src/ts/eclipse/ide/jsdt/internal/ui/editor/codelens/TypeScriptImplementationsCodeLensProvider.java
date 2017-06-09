package ts.eclipse.ide.jsdt.internal.ui.editor.codelens;

import java.text.MessageFormat;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.provisional.codelens.Command;
import org.eclipse.jface.text.provisional.codelens.ICodeLens;
import org.eclipse.jface.text.provisional.codelens.Range;

import ts.TypeScriptKind;
import ts.client.FileSpan;
import ts.client.navbar.NavigationBarItem;
import ts.eclipse.ide.core.resources.IIDETypeScriptFile;

public class TypeScriptImplementationsCodeLensProvider extends TypeScriptBaseCodeLensProvider {

	@Override
	public ICodeLens resolveCodeLens(ITextViewer textViewer, ICodeLens cl) {
		ReferencesCodeLens codeLens = (ReferencesCodeLens) cl;
		// const codeLens = inputCodeLens as ReferencesCodeLens;
		// const args: Proto.FileLocationRequestArgs = {
		// file: codeLens.file,
		// line: codeLens.range.start.line + 1,
		// offset: codeLens.range.start.character + 1
		// };
		IIDETypeScriptFile tsFile = codeLens.getTsFile();
		try {
			int position = tsFile.getPosition(codeLens.getRange().startLineNumber, codeLens.getRange().startColumn);
			List<FileSpan> refs = tsFile.implementation(position).get(1000, TimeUnit.MILLISECONDS);
			int refCount = refs.size();
			if (refCount == 1) {
				codeLens.setCommand(new Command("1 implementation", "implementation"));
			} else {
				codeLens.setCommand(new Command(MessageFormat.format("{0} implementations", refCount), "implementation"));
			}
			
			// (response -> {
			// response.getRefs().stream().map(reference -> {
			// return null;
			// });
			// int refCount = response.getRefs().size() - 1;
			// if (refCount == 1) {
			// codeLens.setCommand(new Command("1 reference", null));
			// } else {
			// codeLens.setCommand(new Command(MessageFormat.format("{0}
			// references", refCount), null));
			// }
			// });
		} catch (Exception e) {
			codeLens.setCommand(new Command("Could not determine references", null));
		}
		return codeLens;
	}

	@Override
	protected Range extractSymbol(IIDETypeScriptFile document, NavigationBarItem item, NavigationBarItem parent) {
		TypeScriptKind tsKind = TypeScriptKind.getKind(item.getKind());
		if (tsKind != null) {
			switch (tsKind) {
			case INTERFACE:
				return super.getSymbolRange(document, item);

			case CLASS:
			case METHOD: /* memberFunction: */
			case PROPERTY /* memberVariable */:
			case GETTER /* memberGetAccessor */:
			case SETTER /* memberSetAccessor */:
				if (item.getKindModifiers() == null || !item.getKindModifiers().contains("abstract")) {
					// if (item.kindModifiers.match(/\babstract\b/g)) {
					return super.getSymbolRange(document, item);
				}
			}
		}
		return null;

	}

}
