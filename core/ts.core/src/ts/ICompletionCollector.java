package ts;

public interface ICompletionCollector {

	void addCompletionEntry(String name, String kind, String kindModifiers, String sortText);

}
