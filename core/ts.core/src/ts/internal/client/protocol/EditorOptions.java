package ts.internal.client.protocol;

import com.eclipsesource.json.JsonObject;

/**
 * Editor Options
 * 
 * @see https://github.com/Microsoft/TypeScript/blob/master/src/server/protocol.
 *      d.ts
 */
public class EditorOptions extends JsonObject {

	
	
	/** Number of spaces for each tab. Default value is 4. */
	// tabSize?: number;

	/** Number of spaces to indent during formatting. Default value is 4. */
	// indentSize?: number;

	/**
	 * The new line character to be used. Default value is the OS line
	 * delimiter.
	 */
	// newLineCharacter?: string;

	/** Whether tabs should be converted to spaces. Default value is true. */
	// convertTabsToSpaces?: boolean;
}
