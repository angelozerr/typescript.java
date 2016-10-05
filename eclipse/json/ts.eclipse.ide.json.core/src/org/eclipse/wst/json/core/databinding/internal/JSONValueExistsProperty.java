package org.eclipse.wst.json.core.databinding.internal;

import org.eclipse.core.databinding.property.INativePropertyListener;
import org.eclipse.core.databinding.property.ISimplePropertyListener;
import org.eclipse.core.databinding.property.value.SimpleValueProperty;
import org.eclipse.json.jsonpath.IJSONPath;
import org.eclipse.wst.sse.core.internal.provisional.text.IStructuredDocument;

public class JSONValueExistsProperty extends SimpleValueProperty {

	private final IJSONPath path;
	private final Object defaultValue;

	public JSONValueExistsProperty(IJSONPath path, Object defaultValue) {
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
		return JSONUpdaterHelper.isValueExists((IStructuredDocument) document, path);
	}

	@Override
	protected void doSetValue(Object document, Object value) {
		if (!(Boolean) value) {
			JSONUpdaterHelper.removePath((IStructuredDocument) document, path);
		} 
	}

}
