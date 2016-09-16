package org.eclipse.wst.json.core.databinding.internal;

import org.eclipse.core.databinding.property.INativePropertyListener;
import org.eclipse.core.databinding.property.ISimplePropertyListener;
import org.eclipse.core.databinding.property.value.SimpleValueProperty;
import org.eclipse.json.jsonpath.IJSONPath;
import org.eclipse.wst.sse.core.internal.provisional.text.IStructuredDocument;

public class JSONValueProperty extends SimpleValueProperty {

	private final IJSONPath path;
	private final Object defaultValue;

	public JSONValueProperty(IJSONPath path, Object defaultValue) {
		this.path = path;
		this.defaultValue = defaultValue;
	}

	@Override
	public Object getValueType() {
		return null;
	}

	@Override
	public INativePropertyListener adaptListener(ISimplePropertyListener listener) {
		return null;
	}

	@Override
	protected Object doGetValue(Object document) {
		Object value = JSONUpdaterHelper.getValue((IStructuredDocument) document, path);
		return value != null ? value : defaultValue;
	}

	@Override
	protected void doSetValue(Object document, Object value) {
		JSONUpdaterHelper.setValue((IStructuredDocument) document, path, value);
	}

}
