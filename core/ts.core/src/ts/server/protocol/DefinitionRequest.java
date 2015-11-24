package ts.server.protocol;

/**
 * Go to definition request; value of command field is "definition". Return
 * response giving the file locations that define the symbol found in file at
 * location line, col.
 * 
 * @see https://github.com/Microsoft/TypeScript/blob/master/src/server/protocol.
 *      d.ts
 */
public class DefinitionRequest extends FileLocationRequest {

	public DefinitionRequest(String fileName, int line, int offset) {
		super(CommandNames.Definition, new FileLocationRequestArgs(fileName, line, offset));
	}

}
