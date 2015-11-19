package ts.doc;

import ts.server.ITypeScriptServiceClient;

public interface IJSDocument {

	String getName();
	
	String getValue();
	
	ITypeScriptServiceClient getClient();
}
