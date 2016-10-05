package org.eclipse.wst.json.core.databinding;

import org.eclipse.json.jsonpath.IJSONPath;
import org.eclipse.json.jsonpath.JSONPath;
import org.eclipse.wst.json.core.databinding.internal.JSONListProperty;
import org.eclipse.wst.json.core.databinding.internal.JSONListPropertyDecorator;
import org.eclipse.wst.json.core.databinding.internal.JSONValueExistsProperty;
import org.eclipse.wst.json.core.databinding.internal.JSONValueProperty;
import org.eclipse.wst.json.core.databinding.internal.JSONValuePropertyDecorator;

public class JSONProperties {

	public static IJSONValueProperty value(IJSONPath path) {
		return value(path, null);
	}

	public static IJSONValueProperty value(IJSONPath path, Object defaultValue) {
		return new JSONValuePropertyDecorator(new JSONValueProperty(path, defaultValue), path);
	}

	public static IJSONValueProperty[] values(JSONPath path) {
		IJSONValueProperty[] properties = new IJSONValueProperty[1];
		for (int i = 0; i < properties.length; i++)
			properties[i] = value(path, null);
		return properties;
	}

	public static IJSONListProperty list(IJSONPath path) {
		return new JSONListPropertyDecorator(new JSONListProperty(path, null), path);
	}

	public static IJSONValueProperty valueExists(IJSONPath path, Object defaultValue) {
		return new JSONValuePropertyDecorator(new JSONValueExistsProperty(path, defaultValue), path);
	}
}
