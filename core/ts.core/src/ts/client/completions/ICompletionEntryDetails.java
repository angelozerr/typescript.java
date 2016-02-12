package ts.client.completions;

import java.util.List;

public interface ICompletionEntryDetails {

	String getName();

	String getKind();

	String getKindModifiers();

	List<SymbolDisplayPart> getDisplayParts();

	List<SymbolDisplayPart> getDocumentation();

}
