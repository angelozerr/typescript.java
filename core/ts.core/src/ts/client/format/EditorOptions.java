package ts.client.format;

import com.eclipsesource.json.JsonObject;

import ts.utils.JsonHelper;

/**
 * Editor Options
 * 
 * @see https://github.com/Microsoft/TypeScript/blob/master/src/server/protocol.
 *      d.ts
 */
public class EditorOptions extends JsonObject {

	public Integer getTabSize() {
		return JsonHelper.getInteger(this, "tabSize");
	}

	public void setTabSize(Integer tabSize) {
		super.set("tabSize", tabSize);
	}

	public Integer getIndentSize() {
		return JsonHelper.getInteger(this, "indentSize");
	}

	public void setIndentSize(Integer indentSize) {
		super.set("indentSize", indentSize);
	}

	public String getNewLineCharacter() {
		return super.getString("newLineCharacter", null);
	}

	public void setNewLineCharacter(String newLineCharacter) {
		super.set("newLineCharacter", newLineCharacter);
	}

	public Boolean getConvertTabsToSpaces() {
		return JsonHelper.getBoolean(this, "convertTabsToSpaces");
	}

	public void setConvertTabsToSpaces(Boolean convertTabsToSpaces) {
		super.set("convertTabsToSpaces", convertTabsToSpaces);
	}

}
