package ts.server.collectors;

public interface ICompletionCollector extends ITypeScriptCollector {

	void addCompletionEntry(String name, String kind, String kindModifiers, String sortText);

}
