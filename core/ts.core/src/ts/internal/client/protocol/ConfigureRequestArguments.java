package ts.internal.client.protocol;

import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;

import ts.client.format.FormatOptions;

/**
 * Information found in a configure request.
 */
public class ConfigureRequestArguments extends JsonObject {

	public ConfigureRequestArguments(FormatOptions formatOptions, String file) {
		setFile(file);
		setFormatOptions(formatOptions);
	}

	/**
	 * Information about the host, for example 'Emacs 24.4' or 'Sublime Text
	 * version 3075'
	 */
	public String getHostInfo() {
		return super.getString("hostInfo", null);
	}

	/**
	 * If present, tab settings apply only to this file.
	 */
	public String getFile() {
		return super.getString("file", null);
	}

	public void setFile(String file) {
		super.set("file", file);
	}

	/**
	 * The format options to use during formatting and other code editing
	 * features.
	 */
	public FormatOptions getFormatOptions() {
		JsonValue value = super.get("formatOptions");
		return value != null ? (FormatOptions) value.asObject() : null;
	}

	public void setFormatOptions(FormatOptions formatOptions) {
		super.set("formatOptions", formatOptions);
	}
}
