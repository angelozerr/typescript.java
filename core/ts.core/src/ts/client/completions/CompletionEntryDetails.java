package ts.client.completions;

import java.util.ArrayList;
import java.util.List;

public class CompletionEntryDetails implements ICompletionEntryDetails {

	private final String name;
	private final String kind;
	private final String kindModifiers;
	private final List<SymbolDisplayPart> displayParts;
	private final List<SymbolDisplayPart> documentation;

	public CompletionEntryDetails(String name, String kind, String kindModifiers) {
		this.name = name;
		this.kind = kind;
		this.kindModifiers = kindModifiers;
		this.displayParts = new ArrayList<SymbolDisplayPart>();
		this.documentation = new ArrayList<SymbolDisplayPart>();
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getKind() {
		return kind;
	}

	@Override
	public String getKindModifiers() {
		return kindModifiers;
	}

	public void addDisplayPart(String text, String kind) {
		this.displayParts.add(new SymbolDisplayPart(text, kind));
	}

	@Override
	public List<SymbolDisplayPart> getDisplayParts() {
		return displayParts;
	}

	public void addDocumentation(String text, String kind2) {
		this.documentation.add(new SymbolDisplayPart(text, kind));
	}

	@Override
	public List<SymbolDisplayPart> getDocumentation() {
		return documentation;
	}

}
