package org.eclipse.jface.text.provisional.codelens;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.eclipse.jface.text.ITextListener;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.TextEvent;
import org.eclipse.jface.text.provisional.codelens.internal.CodeLens;
import org.eclipse.jface.text.provisional.codelens.internal.CodeLensData;
import org.eclipse.jface.text.provisional.codelens.internal.CodeLensHelper;
import org.eclipse.jface.text.provisional.viewzones.ViewZoneChangeAccessor;
import org.eclipse.swt.widgets.Display;

// /vscode/src/vs/editor/contrib/codelens/common/codelens.ts
public class CodeLensContribution {

	private final ITextViewer textViewer;
	private final List<String> targets;
	private ITextListener internalListener = new ITextListener() {

		@Override
		public void textChanged(TextEvent event) {
			if (event.getDocumentEvent() != null) {
				onModelChange();
			}
		}
	};

	private ViewZoneChangeAccessor accessor;
	private List<CodeLens> _lenses;
	private CompletableFuture<Collection<CodeLensData>> symbols;

	public CodeLensContribution(ITextViewer textViewer) {
		this.textViewer = textViewer;
		this.targets = new ArrayList<>();
		textViewer.addTextListener(internalListener);
		try {
			this.accessor = new ViewZoneChangeAccessor(textViewer);
		} catch (Exception e) {
			e.printStackTrace();
		}
		this._lenses = new ArrayList<>();
	}

	public void start() {
		onModelChange();
	}

	private void onModelChange() {
		if (symbols != null) {
			symbols.cancel(true);
		}
		symbols = getCodeLensData(textViewer, targets);
		//symbols.exceptionally(ex -> ex.printStackTrace());
		symbols.thenAccept(s -> {
			renderCodeLensSymbols(s);
		});

	}

	private static CompletableFuture<Collection<CodeLensData>> getCodeLensData(ITextViewer textViewer,
			List<String> targets) {
		return CompletableFuture.supplyAsync(() -> {
			Collection<CodeLensData> symbols = new ArrayList<>();
			for (String target : targets) {
				Collection<ICodeLensProvider> providers = CodeLensProviderRegistry.getInstance().all(target);
				if (providers != null) {
					for (ICodeLensProvider provider : providers) {
						ICodeLens[] lenses = provider.provideCodeLenses(textViewer);
						for (int i = 0; i < lenses.length; i++) {
							symbols.add(new CodeLensData(lenses[i], provider));
						}
					}
				}
			}
			return symbols;
		});		
	}

	private void renderCodeLensSymbols(Collection<CodeLensData> symbols) {
		int maxLineNumber = this.textViewer.getDocument().getNumberOfLines();
		List<List<CodeLensData>> groups = new ArrayList<>();
		List<CodeLensData> lastGroup = null;

		for (CodeLensData symbol : symbols) {
			int line = symbol.getSymbol().getRange().startLineNumber;
			if (line < 1 || line > maxLineNumber) {
				// invalid code lens
				continue;
			} else if (lastGroup != null
					&& lastGroup.get(lastGroup.size() - 1).getSymbol().getRange().startLineNumber == line) {
				// on same line as previous
				lastGroup.add(symbol);
			} else {
				// on later line as previous
				lastGroup = new ArrayList<>(Arrays.asList(symbol));
				groups.add(lastGroup);
			}
		}

		int codeLensIndex = 0, groupsIndex = 0;
		CodeLensHelper helper = new CodeLensHelper();

		while (groupsIndex < groups.size() && codeLensIndex < this._lenses.size()) {

			int symbolsLineNumber = groups.get(groupsIndex).get(0).getSymbol().getRange().startLineNumber;
			int codeLensLineNumber = this._lenses.get(codeLensIndex).getLineNumber();

			if (codeLensLineNumber < symbolsLineNumber) {
				this._lenses.get(codeLensIndex).dispose(helper, accessor);
				this._lenses.remove(codeLensIndex);// .splice(codeLensIndex,
													// 1);
			} else if (codeLensLineNumber == symbolsLineNumber) {
				this._lenses.get(codeLensIndex).updateCodeLensSymbols(groups.get(groupsIndex), helper);
				groupsIndex++;
				codeLensIndex++;
			} else {
				this._lenses.add(codeLensIndex, new CodeLens(groups.get(groupsIndex), helper, accessor));
				// this._lenses.splice(codeLensIndex, 0, new
				// CodeLens(groups.get(groupsIndex),
				// /* this._editor, */ helper, accessor
				/*
				 * , this._commandService, this._messageService, () =>
				 * this._detectVisibleLenses.schedule() //));
				 */
				codeLensIndex++;
				groupsIndex++;
			}
		}

		// Delete extra code lenses
		while (codeLensIndex < this._lenses.size()) {
			this._lenses.get(codeLensIndex).dispose(helper, accessor);
			this._lenses.remove(codeLensIndex);// splice(codeLensIndex, 1);
		}

		// Create extra symbols
		while (groupsIndex < groups.size()) {
			this._lenses.add(new CodeLens(
					groups.get(groupsIndex) /* this._editor */, helper,
					accessor/*
							 * , this._commandService, this._messageService, ()
							 * => this._detectVisibleLenses.schedule())
							 */));
			groupsIndex++;
		}

		// helper.commit(changeAccessor);

		_onViewportChanged();
	}

	private void _onViewportChanged() {
		List<List<CodeLensData>> toResolve = new ArrayList<>();
		List<CodeLens> lenses = new ArrayList<>();

		this._lenses.forEach((lens) -> {
			List<CodeLensData> request = lens.computeIfNecessary(null);
			if (request != null) {
				toResolve.add(request);
				lenses.add(lens);
			}
		});

		if (toResolve.isEmpty()) {
			return;
		}

		int i = 0;
		for (List<CodeLensData> request : toResolve) {
			List<ICodeLens> resolvedSymbols = new ArrayList<ICodeLens>(request.size());
			for (CodeLensData req : request) {
				ICodeLens symbol = req.getProvider().resolveCodeLens(textViewer, req.getSymbol());
				if (symbol != null) {
					resolvedSymbols.add(symbol);
				}
			}
			lenses.get(i).updateCommands(resolvedSymbols);
			i++;
		}
		

		Display.getDefault().syncExec(() -> {
			textViewer.getTextWidget().redraw();				
		});	
	}

	public CodeLensContribution addTarget(String target) {
		targets.add(target);
		return this;
	}

	public CodeLensContribution removeTarget(String target) {
		targets.remove(target);
		return this;
	}

	public void dispose() {
		
	}

	/*
	 * private Collection<CompletableFuture<CodeLensData>>
	 * getCodeLensData(ITextViewer textViewer) {
	 * 
	 * Collection<CodeLensData> symbols = new ArrayList<>(); String
	 * contentTypeId = "";
	 * 
	 * // Collection<CompletableFuture<CodeLensData>> promises =
	 * registry.all(contentTypeId).stream().map(provider -> { //
	 * CompletableFuture<CodeLensData> promise =
	 * CompletableFuture.supplyAsync(() -> { //
	 * provider.provideCodeLenses(textViewer); // });
	 * 
	 * // // // // new CompletableFuture<CodeLensData>(); // //promise. // //
	 * promise.thenAccept(result -> { // if (result != null) { // for (ICodeLens
	 * symbol : result) { // symbols.add(new CodeLensData(symbol, provider)); //
	 * } // } // }) return promise; }).collect(Collectors.toList());
	 * 
	 * for (
	 * 
	 * ICodeLensProvider provider : providers) { ICodeLens[] result =
	 * provider.provideCodeLenses(textViewer); if (result != null) { for
	 * (ICodeLens symbol : result) { symbols.add(new CodeLensData(symbol,
	 * provider)); } } }
	 * 
	 * }
	 */

}
