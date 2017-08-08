package org.eclipse.jface.text.provisional.codelens;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.provisional.codelens.internal.CodeLens;
import org.eclipse.jface.text.provisional.codelens.internal.CodeLensData;
import org.eclipse.jface.text.provisional.codelens.internal.CodeLensHelper;
import org.eclipse.jface.text.provisional.viewzones.ViewZoneChangeAccessor;
import org.eclipse.jface.text.reconciler.DirtyRegion;
import org.eclipse.jface.text.reconciler.IReconcilingStrategy;
import org.eclipse.jface.text.reconciler.IReconcilingStrategyExtension;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.custom.patch.StyledTextPatcher;

// /vscode/src/vs/editor/contrib/codelens/common/codelens.ts
public class CodeLensStrategy implements IReconcilingStrategy, IReconcilingStrategyExtension {

	private final ICodeLensContext context;
	private final List<String> targets;

	private AtomicInteger count = new AtomicInteger(0);

	private ViewZoneChangeAccessor accessor;
	private List<CodeLens> _lenses;
	private CompletableFuture<Void> symbolsPromise;
	private boolean invalidateTextPresentation;
	private IProgressMonitor monitor;

	public CodeLensStrategy(ICodeLensContext context) {
		this(context, true);
	}

	public CodeLensStrategy(ICodeLensContext context, boolean invalidateTextPresentation) {
		this.context = context;
		this.invalidateTextPresentation = invalidateTextPresentation;
		this.targets = new ArrayList<>();
		// Initialize the view change accessor in the UI Thread because the
		// constructor update the StyledTextRenderer which is accessible only in
		// an UI Thread.
		ITextViewer textViewer = context.getViewer();
		textViewer.getTextWidget().getDisplay().syncExec(() -> {
			CodeLensStrategy.this.accessor = new ViewZoneChangeAccessor(textViewer);
		});
		this._lenses = new ArrayList<>();
	}

	private void onModelChange() {
		if (symbolsPromise != null) {
			symbolsPromise.cancel(true);
		}
		int modelCount = count.incrementAndGet();
		symbolsPromise = getCodeLensData(context, targets, modelCount).thenAccept(symbols -> {
			renderCodeLensSymbols(symbols);
		}).exceptionally(e -> {
			e.printStackTrace();
			return null;
		});
	}

	private CompletableFuture<Collection<CodeLensData>> getCodeLensData(ICodeLensContext context, List<String> targets,
			int modelCount) {
		return CompletableFuture.supplyAsync(() -> {
			List<CodeLensData> symbols = new ArrayList<>();
			for (String target : targets) {
				List<ICodeLensProvider> providers = CodeLensProviderRegistry.getInstance().all(target);
				if (providers != null) {
					for (ICodeLensProvider provider : providers) {
						ICodeLens[] lenses = provider.provideCodeLenses(context, getProgressMonitor());
						if (lenses != null) {
							for (int i = 0; i < lenses.length; i++) {
								symbols.add(new CodeLensData(lenses[i], provider));
							}
						}
					}
					Collections.sort(symbols, (a, b) -> {
						// sort by lineNumber, provider-rank, and column
						if (a.getSymbol().getRange().startLineNumber < b.getSymbol().getRange().startLineNumber) {
							return -1;
						} else if (a.getSymbol().getRange().startLineNumber > b.getSymbol()
								.getRange().startLineNumber) {
							return 1;
						} else if (providers.indexOf(a.getProvider()) < providers.indexOf(b.getProvider())) {
							return -1;
						} else if (providers.indexOf(a.getProvider()) > providers.indexOf(b.getProvider())) {
							return 1;
						} else if (a.getSymbol().getRange().startColumn < b.getSymbol().getRange().startColumn) {
							return -1;
						} else if (a.getSymbol().getRange().startColumn > b.getSymbol().getRange().startColumn) {
							return 1;
						} else {
							return 0;
						}
					});
				}
			}
			return symbols;
		});
	}

	private void renderCodeLensSymbols(Collection<CodeLensData> symbols) {
		IDocument document = this.context.getViewer().getDocument();
		int maxLineNumber = document.getNumberOfLines();
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
			int offset = this._lenses.get(codeLensIndex).getOffsetAtLine();
			int codeLensLineNumber = -1;
			try {
				codeLensLineNumber = offset != -1 ? document.getLineOfOffset(offset) + 1 : -1;
			} catch (BadLocationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} // this._lenses.get(codeLensIndex).getLineNumber();

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
			this._lenses.add(new CodeLens(groups.get(groupsIndex) /* this._editor */, helper,
					accessor/*
							 * , this._commandService, this._messageService, () =>
							 * this._detectVisibleLenses.schedule())
							 */));
			groupsIndex++;
		}

		// helper.commit(changeAccessor);
		// Display.getDefault().asyncExec(() -> {
		//// this._lenses.forEach((lens) -> {
		//// lens.redraw(accessor);
		//// });
		// textViewer.getTextWidget().redraw();
		// });
		_onViewportChanged();
	}

	private void _onViewportChanged() {
		List<List<CodeLensData>> toResolve = new ArrayList<>();
		List<CodeLens> lenses = new ArrayList<>();

		Integer topMargin = null;
		for (CodeLens lens : _lenses) {
			List<CodeLensData> request = lens.computeIfNecessary(null);
			if (request != null) {
				toResolve.add(request);
				lenses.add(lens);

				Integer top = lens.getTopMargin();
				if (top != null) {
					topMargin = top;
				}
			}
		}

		if (toResolve.isEmpty()) {
			// return;
		}

		int i = 0;
		for (List<CodeLensData> request : toResolve) {
			List<ICodeLens> resolvedSymbols = new ArrayList<ICodeLens>(request.size());
			for (CodeLensData req : request) {
				ICodeLens symbol = req.getProvider().resolveCodeLens(context, req.getSymbol(), getProgressMonitor());
				if (symbol != null) {
					resolvedSymbols.add(symbol);
				}
			}
			lenses.get(i).updateCommands(resolvedSymbols);
			i++;
		}

		final Integer top = topMargin;
		ITextViewer textViewer = context.getViewer();
		final StyledText styledText = textViewer.getTextWidget();
		styledText.getDisplay().syncExec(() -> {
			if (invalidateTextPresentation) {
				// if (top != null && styledText.getTopMargin() != top) {
				// try {
				// Field f =
				// styledText.getClass().getDeclaredField("topMargin");
				// f.setAccessible(true);
				// f.set(styledText, top);
				// } catch (Exception e) {
				// // TODO Auto-generated catch block
				// e.printStackTrace();
				// }
				// }
				textViewer.invalidateTextPresentation();
			} else {
				if (top != null && styledText.getTopMargin() != top) {
					styledText.setTopMargin(top);
				} else {
					int offset = styledText.getCaretOffset();
					StyledTextPatcher.setVariableLineHeight(styledText);
					styledText.redraw();
					styledText.setCaretOffset(offset);
				}
			}
		});
	}

	public CodeLensStrategy addTarget(String target) {
		targets.add(target);
		return this;
	}

	public CodeLensStrategy removeTarget(String target) {
		targets.remove(target);
		return this;
	}

	public void dispose() {

	}

	@Override
	public void setDocument(IDocument document) {
		// TODO Auto-generated method stub

	}

	@Override
	public void reconcile(DirtyRegion dirtyRegion, IRegion subRegion) {
		onModelChange();
	}

	@Override
	public void reconcile(IRegion partition) {
		onModelChange();
	}

	@Override
	public void setProgressMonitor(IProgressMonitor monitor) {
		this.monitor = monitor;
	}

	public IProgressMonitor getProgressMonitor() {
		return monitor;
	}

	@Override
	public void initialReconcile() {
		onModelChange();
	}

}
