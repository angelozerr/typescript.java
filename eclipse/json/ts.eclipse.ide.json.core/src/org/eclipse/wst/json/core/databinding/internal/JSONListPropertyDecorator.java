package org.eclipse.wst.json.core.databinding.internal;

import java.util.List;

import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.list.ListDiff;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.property.list.IListProperty;
import org.eclipse.core.databinding.property.list.ListProperty;
import org.eclipse.json.jsonpath.IJSONPath;
import org.eclipse.json.jsonpath.JSONPath;
import org.eclipse.wst.json.core.databinding.IJSONListProperty;
import org.eclipse.wst.json.core.databinding.IJSONValueProperty;
import org.eclipse.wst.json.core.databinding.JSONProperties;

public class JSONListPropertyDecorator extends ListProperty implements IJSONListProperty {
	private final IListProperty delegate;
	private final IJSONPath path;

	/**
	 * @param delegate
	 * @param path
	 */
	public JSONListPropertyDecorator(IListProperty delegate, IJSONPath path) {
		this.delegate = delegate;
		this.path = path;
	}

	@Override
	public Object getElementType() {
		return delegate.getElementType();
	}

	@Override
	protected List doGetList(Object source) {
		return delegate.getList(source);
	}

	@Override
	protected void doSetList(Object source, List list) {
		delegate.setList(source, list);
	}

	@Override
	protected void doUpdateList(Object source, ListDiff diff) {
		delegate.updateList(source, diff);
	}

	@Override
	public IJSONListProperty values(String propertyName) {
		return values(propertyName, null);
	}

	@Override
	public IJSONListProperty values(String propertyName, Class valueType) {
		Class JSONClass = (Class) delegate.getElementType();
		return values(JSONProperties.value(path));
	}

	@Override
	public IJSONListProperty values(IJSONValueProperty property) {
		return new JSONListPropertyDecorator(super.values(property), property.getPath());
	}


	@Override
	public IObservableList observe(Object source) {
		return new JSONObservableListDecorator(delegate.observe(source), path);
	}

	@Override
	public IObservableList observe(Realm realm, Object source) {
		return new JSONObservableListDecorator(delegate.observe(realm, source), path);
	}

	@Override
	public IObservableList observeDetail(IObservableValue master) {
		return new JSONObservableListDecorator(delegate.observeDetail(master), path);
	}

	@Override
	public String toString() {
		return delegate.toString();
	}
	
	@Override
	public IJSONPath getPath() {
		return path;
	}
}
