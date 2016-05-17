package ts.utils;

import java.util.List;

import ts.client.completions.SymbolDisplayPart;

public class TypeScriptHelper {

	/**
	 * Returns the TypeScript prefix completion for the given position of the
	 * given content.
	 * 
	 * @param contents
	 * @param position
	 * @return the TypeScript prefix completion for the given position of the
	 *         given content.
	 */
	public static String getPrefix(String contents, int position) {
		StringBuilder prefix = null;
		int i = position - 1;
		while (i >= 0) {
			char c = contents.charAt(i);
			if (!Character.isJavaIdentifierPart(c)) {
				break;
			} else {
				if (prefix == null) {
					prefix = new StringBuilder();
				}
				prefix.insert(0, c);
			}
			i--;
		}
		return prefix != null ? prefix.toString() : null;
	}

	public static String text(List<SymbolDisplayPart> parts) {
		if (parts == null || parts.size() < 1) {
			return null;
		}
		StringBuilder html = new StringBuilder("");
		for (SymbolDisplayPart part : parts) {
			html.append(part.getText());
		}
		html.append("");
		return html.toString();
	}
}
