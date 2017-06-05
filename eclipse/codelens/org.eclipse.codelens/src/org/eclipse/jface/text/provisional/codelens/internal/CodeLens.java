package org.eclipse.jface.text.provisional.codelens.internal;

import java.util.List;

import org.eclipse.jface.text.provisional.codelens.ICodeLens;
import org.eclipse.jface.text.provisional.codelens.Range;
import org.eclipse.jface.text.provisional.viewzones.ViewZoneChangeAccessor;

public class CodeLens {

	private CodeLensViewZone zone;
	private List<CodeLensData> _data;

	public CodeLens(List<CodeLensData> data, CodeLensHelper helper, ViewZoneChangeAccessor accessor) {
		Range range = data.get(0).getSymbol().getRange();
		zone = new CodeLensViewZone(range.startLineNumber - 1, 20);
		accessor.addZone(zone);
		_data = data;
	}

	public void dispose(CodeLensHelper helper, ViewZoneChangeAccessor accessor) {
		accessor.removeZone(zone);
	}

	public int getLineNumber() {
		return zone.getAfterLineNumber();
	}

	public void updateCodeLensSymbols(List<CodeLensData> data, CodeLensHelper helper) {
		this._data = data;
	}

	public List<CodeLensData> computeIfNecessary(Object object) {
		return this._data;
	}

	public void updateCommands(List<ICodeLens> resolvedSymbols) {
		if (resolvedSymbols == null || resolvedSymbols.size() < 1) {
			zone.setText("no command");
		} else {
			StringBuilder text = new StringBuilder();
			int i = 0;
			for (ICodeLens codeLens : resolvedSymbols) {
				if (i > 0) {
					text.append(" | ");
				}
				text.append(codeLens.getCommand().getTitle());
				i++;
			}
			zone.setText(text.toString());
		}
	}
}
