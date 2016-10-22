package ts.internal.client.protocol;

import com.eclipsesource.json.JsonArray;

public class CodeFixRequestArgs extends FileRequestArgs {

	public CodeFixRequestArgs(String file, int startLine, int startOffset, int endLine, int endOffset) {
		super(file);
		super.add("startLine", startLine);
		super.add("startOffset", startOffset);
		super.add("endLine", endLine);
		super.add("endOffset", endOffset);
		
//		JsonArray errorCodes = new JsonArray();
//		errorCodes.add(2377);
//		super.add("errorCodes", errorCodes);
	}

}
