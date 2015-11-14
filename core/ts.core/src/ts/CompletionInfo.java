package ts;

public class CompletionInfo implements ICompletionInfo {

	private final boolean memberCompletion;
	private final boolean newIdentifierLocation;
	private final ICompletionEntry[] entries;

	public CompletionInfo(boolean memberCompletion, boolean newIdentifierLocation, ICompletionEntry[] entries) {
		this.memberCompletion = memberCompletion;
		this.newIdentifierLocation = newIdentifierLocation;
		this.entries = entries;
	}

	@Override
	public boolean isMemberCompletion() {
		return memberCompletion;
	}

	@Override
	public boolean isNewIdentifierLocation() {
		return newIdentifierLocation;
	}

	@Override
	public ICompletionEntry[] getEntries() {
		return entries;
	}

}
