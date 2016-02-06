package ts.internal.server;

import java.util.concurrent.Callable;

import com.eclipsesource.json.JsonObject;

public interface ICallbackItem<T> extends Callable<T> {

	boolean complete(JsonObject response);

}
