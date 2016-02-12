package ts.utils;

import com.eclipsesource.json.JsonArray;

public class JSONUtils {

	public static JsonArray toJson(String[] arr) {
		JsonArray json = new JsonArray();
		for (int i = 0; i < arr.length; i++) {
			json.add(arr[i]);
		}
		return json;
	}
}
