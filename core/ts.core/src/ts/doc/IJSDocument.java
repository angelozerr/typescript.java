package ts.doc;

import ts.server.ITSClient;

public interface IJSDocument {

	String getName();
	
	String getValue();
	
	ITSClient getClient();
}
