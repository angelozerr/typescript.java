package ts.client;

import ts.TypeScriptException;

public interface ITypeScriptAsynchCollector {

	void startCollect();

	void endCollect();

	void onError(TypeScriptException e);
}
