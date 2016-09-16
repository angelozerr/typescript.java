package org.eclipse.wst.json.core.databinding.internal;

import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.property.value.IValueProperty;
import org.eclipse.core.databinding.property.value.ValueProperty;
import org.eclipse.json.jsonpath.IJSONPath;
import org.eclipse.wst.json.core.databinding.IJSONValueProperty;

public class JSONValuePropertyDecorator extends ValueProperty implements IJSONValueProperty {

	private final IValueProperty delegate;
	private final IJSONPath path;

	public JSONValuePropertyDecorator(IValueProperty delegate, IJSONPath path) {
		this.delegate = delegate;
		this.path = path;
	}

	@Override
	public Object getValueType() {
		return delegate.getValueType();
	}

	@Override
	protected Object doGetValue(Object source) {
		return delegate.getValue(source);
	}

	@Override
	protected void doSetValue(Object source, Object value) {
		delegate.setValue(source, value);
	}

	@Override
	public IObservableValue observe(Object source) {
		return new JSONObservableValueDecorator(delegate.observe(source), path);
	}

	@Override
	public IObservableValue observe(Realm realm, Object source) {
		return new JSONObservableValueDecorator(delegate.observe(realm, source), path);
	}

	@Override
	public IJSONPath getPath() {
		return path;
	}

}
