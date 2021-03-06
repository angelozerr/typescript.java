package ts.eclipse.ide.internal.ui.codelens;

import java.text.MessageFormat;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.provisional.codelens.Command;
import org.eclipse.jface.text.provisional.codelens.ICodeLens;
import org.eclipse.jface.text.provisional.codelens.ICodeLensContext;
import org.eclipse.jface.text.provisional.codelens.Range;

import ts.ScriptElementKind;
import ts.client.FileSpan;
import ts.client.navbar.NavigationBarItem;
import ts.eclipse.ide.core.resources.IIDETypeScriptFile;
import ts.eclipse.ide.ui.codelens.TypeScriptBaseCodeLensProvider;

public class TypeScriptImplementationsCodeLensProvider extends TypeScriptBaseCodeLensProvider {

	@Override
	public CompletableFuture<ICodeLens> resolveCodeLens(ICodeLensContext context, ICodeLens cl, IProgressMonitor monitor) {
		ImplementationsCodeLens codeLens = (ImplementationsCodeLens) cl;
		// const codeLens = inputCodeLens as ReferencesCodeLens;
		// const args: Proto.FileLocationRequestArgs = {
		// file: codeLens.file,
		// line: codeLens.range.start.line + 1,
		// offset: codeLens.range.start.character + 1
		// };
		IIDETypeScriptFile tsFile = codeLens.getTsFile();
		try {
			int position = tsFile.getPosition(codeLens.getRange().startLineNumber, codeLens.getRange().startColumn);
			return tsFile.implementation(position).thenApply(refs -> {
				int refCount = refs.size();
				if (refCount == 1) {
					codeLens.setCommand(new Command("1 implementation", "implementation"));
				} else {
					codeLens.setCommand(
							new Command(MessageFormat.format("{0} implementations", refCount), "implementation"));
				}
				return codeLens;
			});
		} catch (Exception e) {
			codeLens.setCommand(new Command("Could not determine implementations", null));
		}
		return null;
	}

	@Override
	protected Range extractSymbol(IIDETypeScriptFile document, NavigationBarItem item, NavigationBarItem parent) {
		ScriptElementKind tsKind = ScriptElementKind.getKind(item.getKind());
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

	@Override
	protected ICodeLens[] toCodeLenses(List<Range> referenceableSpans, IIDETypeScriptFile tsFile) {
		return referenceableSpans.stream().map(span -> new ImplementationsCodeLens(tsFile, span))
				.collect(Collectors.toList()).toArray(new ICodeLens[0]);
	}

}
