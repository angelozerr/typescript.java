package ts.internal.client;

import java.util.concurrent.Callable;

import com.eclipsesource.json.JsonObject;

public interface ICallbackItem<T> extends Callable<T> {

	boolean complete(JsonObject response);

}
