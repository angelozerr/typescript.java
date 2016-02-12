package ts.client.completions;

public class SymbolDisplayPart {

	private final String text;
	private final String kind;

	public SymbolDisplayPart(String text, String kind) {
		this.text = text;
		this.kind = kind;
	}

	public String getText() {
		return text;
	}

	public String getKind() {
		return kind;
	}

}
