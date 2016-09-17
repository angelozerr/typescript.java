/*******************************************************************************
 * Copyright (c) 2008, 2015 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 194734)
 *     Matthew Hall - bugs 195222, 264307, 265561, 301774
 *     Ovidio Mallo - bug 306633
 ******************************************************************************/

package org.eclipse.wst.json.core.databinding.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.databinding.observable.list.ListDiff;
import org.eclipse.core.databinding.property.INativePropertyListener;
import org.eclipse.core.databinding.property.ISimplePropertyListener;
import org.eclipse.core.databinding.property.list.SimpleListProperty;
import org.eclipse.json.jsonpath.IJSONPath;
import org.eclipse.wst.json.core.document.IJSONArray;
import org.eclipse.wst.json.core.document.IJSONNode;
import org.eclipse.wst.json.core.document.IJSONValue;
import org.eclipse.wst.sse.core.internal.provisional.text.IStructuredDocument;

/**
 * @since 3.3
 *
 */
public class JSONListProperty extends SimpleListProperty {
	private final IJSONPath propertyDescriptor;
	private final Class elementType;

	/**
	 * @param propertyDescriptor
	 * @param elementType
	 */
	public JSONListProperty(IJSONPath propertyDescriptor, Class elementType) {
		this.propertyDescriptor = propertyDescriptor;
		this.elementType = null;
		// this.elementType = elementType == null ? BeanPropertyHelper
		// .getCollectionPropertyElementType(propertyDescriptor)
		// : elementType;
	}

	@Override
	public Object getElementType() {
		return elementType;
	}

	@Override
	protected List doGetList(Object source) {
		IStructuredDocument document = (IStructuredDocument) source;
		IJSONPath path = propertyDescriptor;
		Object value = JSONUpdaterHelper.getValue(document, path);
		if (value instanceof IJSONArray) {
			IJSONArray array = (IJSONArray) value;
			List<Object> result = new ArrayList<Object>();
			for (int i = 0; i < array.getLength(); i++) {
				try {
					IJSONNode n = (IJSONNode) array.getClass().getMethod("item", int.class).invoke(array, i);
					result.add(JSONUpdaterHelper.getValue((IJSONValue) n));
				} catch (Exception e) {
					return null;
				}
			}
			return result;
		}

		// return asList(BeanPropertyHelper.readProperty(source,
		// propertyDescriptor));
		return new ArrayList<String>();
	}

	private List asList(Object propertyValue) {
		if (propertyValue == null)
			return Collections.EMPTY_LIST;
		// if (propertyDescriptor.getPropertyType().isArray())
		// return Arrays.asList((Object[]) propertyValue);
		return null;// (List) propertyValue;
	}

	@Override
	protected void doSetList(Object source, List list, ListDiff diff) {
		doSetList(source, list);
	}

	@Override
	protected void doSetList(Object source, List list) {
		// BeanPropertyHelper.writeProperty(source, propertyDescriptor,
		// convertListToBeanPropertyType(list));
	}

	private Object convertListToBeanPropertyType(List list) {
		Object propertyValue = list;
		// if (propertyDescriptor.getPropertyType().isArray()) {
		// Class componentType = propertyDescriptor.getPropertyType()
		// .getComponentType();
		// Object[] array = (Object[]) Array.newInstance(componentType, list
		// .size());
		// list.toArray(array);
		// propertyValue = array;
		// }
		return propertyValue;
	}

	@Override
	public INativePropertyListener adaptListener(final ISimplePropertyListener listener) {
		// return new BeanPropertyListener(this, propertyDescriptor, listener) {
		// @Override
		// protected IDiff computeDiff(Object oldValue, Object newValue) {
		// return Diffs
		// .computeListDiff(asList(oldValue), asList(newValue));
		// }
		// };
		return null;
	}

	@Override
	public String toString() {
		// String s = BeanPropertyHelper.propertyName(propertyDescriptor) +
		// "[]"; //$NON-NLS-1$
		// if (elementType != null)
		// s += "<" + BeanPropertyHelper.shortClassName(elementType) + ">";
		// //$NON-NLS-1$//$NON-NLS-2$
		// return s;
		return super.toString();
	}
}
