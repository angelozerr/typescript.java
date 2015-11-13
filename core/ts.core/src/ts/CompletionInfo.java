package ts;

/**
 * @see https://github.com/Microsoft/TypeScript/blob/master/src/services/services.ts
 *
 */
public interface CompletionInfo {

	boolean isMemberCompletion();
	/**
	 * Returns true when the current location also allows for a new identifier
	 * @return true when the current location also allows for a new identifier
	 */
	boolean isNewIdentifierLocation();  
	
	CompletionEntry[] getEntries();
}
