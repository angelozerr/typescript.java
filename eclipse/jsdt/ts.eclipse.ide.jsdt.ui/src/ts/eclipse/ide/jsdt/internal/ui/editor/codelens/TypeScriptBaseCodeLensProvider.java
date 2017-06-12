package ts.eclipse.ide.jsdt.internal.ui.editor.codelens;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.provisional.codelens.ICodeLens;
import org.eclipse.jface.text.provisional.codelens.ICodeLensProvider;
import org.eclipse.jface.text.provisional.codelens.Range;

import ts.client.Location;
import ts.client.navbar.NavigationBarItem;
import ts.client.navbar.NavigationTextSpan;
import ts.eclipse.ide.core.resources.IIDETypeScriptFile;
import ts.eclipse.ide.core.resources.IIDETypeScriptProject;
import ts.eclipse.ide.core.utils.TypeScriptResourceUtil;
import ts.eclipse.ide.ui.TypeScriptUIPlugin;

public abstract class TypeScriptBaseCodeLensProvider implements ICodeLensProvider {

	@Override
	public ICodeLens[] provideCodeLenses(ITextViewer textViewer) {
		IResource resource = TypeScriptResourceUtil.getFile(textViewer.getDocument());
		if (resource == null) {
			return null;
		}
		if (TypeScriptResourceUtil.canConsumeTsserver(resource)) {
			// the project of the resource has typescript nature, execute
			// typescript
			// hyperlink.
			try {
				IProject project = resource.getProject();
				IIDETypeScriptProject tsProject = TypeScriptResourceUtil.getTypeScriptProject(project);
				IDocument document = textViewer.getDocument();
				IIDETypeScriptFile tsFile = tsProject.openFile(resource, document);
				NavigationBarItem tree = tsProject.getClient().navtree(tsFile.getName(), tsFile).get(1000,
						TimeUnit.MILLISECONDS);
				List<Range> referenceableSpans = new ArrayList<>();
				if (tree != null && tree.hasChildItems()) {
					tree.getChildItems().forEach(item -> this.walkNavTree(tsFile, item, null, referenceableSpans));
				}
				return referenceableSpans.stream().map(span -> new ReferencesCodeLens(tsFile, span))
						.collect(Collectors.toList()).toArray(new ICodeLens[0]);
			} catch (Exception e) {
				TypeScriptUIPlugin.log("Error while TypeScript codelens", e);
			}
		}
		return new ICodeLens[0];
	}

	private void walkNavTree(IIDETypeScriptFile document, NavigationBarItem item, NavigationBarItem parent,
			List<Range> results) {
		if (item == null) {
			return;
		}

		Range range = this.extractSymbol(document, item, parent);
		if (range != null) {
			results.add(range);
		}

		if (item.hasChildItems()) {
			item.getChildItems().forEach(child -> this.walkNavTree(document, child, item, results));
		}
	}

	protected Range getSymbolRange(IIDETypeScriptFile tsFile, NavigationBarItem item) {
		if (item == null) {
			return null;
		}

		NavigationTextSpan span = item.hasSpans() ? item.getSpans().get(0) : null;
		if (span == null) {
			return null;
		}

		Range range = new Range(span.getStart().getLine(), span.getStart().getOffset());

		// Range range = new Range(
		// span.getStart().getLine() - 1, span.getStart().getOffset() - 1,
		// span.getEnd().getLine() - 1, span.getEnd().getOffset()- 1);

		try {
			IDocument document = tsFile.getDocument();
			int offset = tsFile.getPosition(span.getStart());
			int endOffset = tsFile.getPosition(span.getEnd());
			String text = document.get(offset, span.getLength());

			String regex = "^(.*?(\\b|\\W))" + (item.getText() != null ? item.getText() : "")
					.replaceAll("[-[\\\\]{}()*+?.,\\\\^$|#\\s]", "\\\\$&") + "(\\b|\\W)";
			Matcher identifierMatch = Pattern.compile(regex, Pattern.MULTILINE).matcher(text);
			int prefixLength = identifierMatch.find() ? identifierMatch.start() + identifierMatch.end() : 0;

			int position = offset + prefixLength;

			Location location = tsFile.getLocation(position);
			int newLine = location.getLine();
			int newOffset = location.getOffset();

			return new Range(newLine, newOffset);
			// return new Range(
			// document.positionAt(startOffset),
			// document.positionAt(startOffset + item.text.length));

			// new RegExp(`^(.*?(\\b|\\W))${(item.text ||
			// '').replace(/[-[\]{}()*+?.,\\^$|#\s]/g, '\\$&')}(\\b|\\W)`,
			// 'gm');
			//
			// const identifierMatch =
			// Pattern.compile(".*?(\\b|\\W))${(item.text ||
			// '').replace(/[-[\]{}()*+?.,\\^$|#\s]/g, '\\$&')}(\\b|\\W)`,
			// 'gm');
			// const match = identifierMatch.exec(text);
			// const prefixLength = match ? match.index + match[1].length : 0;

			// const identifierMatch = new RegExp(`^(.*?(\\b|\\W))${(item.text
			// || * '').replace(/[-[\]{}()*+?.,\\^$|#\s]/g, '\\$&')}(\\b|\\W)`,
			// 'gm');
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		/*
		 * const text = document.getText(range);
		 * 
		 * const identifierMatch = new RegExp(`^(.*?(\\b|\\W))${(item.text ||
		 * '').replace(/[-[\]{}()*+?.,\\^$|#\s]/g, '\\$&')}(\\b|\\W)`, 'gm');
		 * const match = identifierMatch.exec(text); const prefixLength = match
		 * ? match.index + match[1].length : 0; const startOffset =
		 * document.offsetAt(new Position(range.start.line,
		 * range.start.character)) + prefixLength; return new Range(
		 * document.positionAt(startOffset), document.positionAt(startOffset +
		 * item.text.length));
		 */
		return range;
	}

	protected abstract Range extractSymbol(IIDETypeScriptFile document, NavigationBarItem item,
			NavigationBarItem parent);
}
