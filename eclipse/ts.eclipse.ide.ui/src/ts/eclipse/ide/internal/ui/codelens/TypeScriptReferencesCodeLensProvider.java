package ts.eclipse.ide.internal.ui.codelens;

import java.text.MessageFormat;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.provisional.codelens.Command;
import org.eclipse.jface.text.provisional.codelens.ICodeLens;
import org.eclipse.jface.text.provisional.codelens.ICodeLensContext;
import org.eclipse.jface.text.provisional.codelens.Range;

import ts.ScriptElementKind;
import ts.client.navbar.NavigationBarItem;
import ts.client.references.ReferencesResponseItem;
import ts.eclipse.ide.core.resources.IIDETypeScriptFile;
import ts.eclipse.ide.ui.codelens.TypeScriptBaseCodeLensProvider;

public class TypeScriptReferencesCodeLensProvider extends TypeScriptBaseCodeLensProvider {

	@Override
	public ICodeLens resolveCodeLens(ICodeLensContext context, ICodeLens cl, IProgressMonitor monitor) {
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
			List<ReferencesResponseItem> refs = tsFile.references(position).get(1000, TimeUnit.MILLISECONDS).getRefs();
			int refCount = refs.size() - 1;
			if (refCount == 1) {
				codeLens.setCommand(new Command("1 reference", "references"));
			} else {
				codeLens.setCommand(new Command(MessageFormat.format("{0} references", refCount), "references"));
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
		// return this.client.execute('references', args, token).then(response
		// => {
		// if (!response || !response.body) {
		// throw codeLens;
		// }
		//
		// const locations = response.body.refs
		// .map(reference =>
		// new Location(this.client.asUrl(reference.file),
		// new Range(
		// reference.start.line - 1, reference.start.offset - 1,
		// reference.end.line - 1, reference.end.offset - 1)))
		// .filter(location =>
		// // Exclude original definition from references
		// !(location.uri.fsPath === codeLens.document.fsPath &&
		// location.range.start.isEqual(codeLens.range.start)));
		//
		// codeLens.command = {
		// title: locations.length === 1
		// ? localize('oneReferenceLabel', '1 reference')
		// : localize('manyReferenceLabel', '{0} references', locations.length),
		// command: locations.length ? 'editor.action.showReferences' : '',
		// arguments: [codeLens.document, codeLens.range.start, locations]
		// };
		// return codeLens;
		// }).catch(() => {
		// codeLens.command = {
		// title: localize('referenceErrorLabel', 'Could not determine
		// references'),
		// command: ''
		// };
		// return codeLens;
		// });
	}

	@Override
	protected Range extractSymbol(IIDETypeScriptFile document, NavigationBarItem item, NavigationBarItem parent) {
		if (parent != null && ScriptElementKind.ENUM.equals(ScriptElementKind.getKind(parent.getKind()))) {
			return super.getSymbolRange(document, item);
		}

		ScriptElementKind tsKind = ScriptElementKind.getKind(item.getKind());
		if (tsKind != null) {

			switch (tsKind) {
			case CONST:
			case LET:
			case VAR:
			case FUNCTION:
				// Only show references for exported variables
				if (item.getKindModifiers() == null || !item.getKindModifiers().contains("export")) {
					break;
				}
				// fallthrough

			case CLASS:
				if ("<class>".equals(item.getText())) {
					break;
				}
				// fallthrough

			case METHOD /* memberFunction */:
			case PROPERTY /* PConst.Kind.memberVariable */:
			case GETTER /* PConst.Kind.memberGetAccessor */:
			case SETTER /* PConst.Kind.memberSetAccessor */:
			case CONSTRUCTOR /* PConst.Kind.constructorImplementation */:

			case INTERFACE:
			case TYPE:
			case ENUM:
				return super.getSymbolRange(document, item);
			}
		}
		return null;

	}

	@Override
	protected ICodeLens[] toCodeLenses(List<Range> referenceableSpans, IIDETypeScriptFile tsFile) {
		return referenceableSpans.stream().map(span -> new ReferencesCodeLens(tsFile, span))
				.collect(Collectors.toList()).toArray(new ICodeLens[0]);
	}

}
