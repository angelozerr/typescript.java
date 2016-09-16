package org.eclipse.wst.json.core.databinding;

import org.eclipse.core.databinding.property.list.IListProperty;

public interface IJSONListProperty extends IJSONProperty, IListProperty {

	/**
	 * Returns a master-detail combination of this property and the specified
	 * value property.
	 *
	 * @param propertyName
	 *            the value property to observe. May be nested e.g.
	 *            "parent.name"
	 * @return a nested combination of this property and the specified value
	 *         property.
	 * @see #values(IJSONValueProperty)
	 */
	public IJSONListProperty values(String propertyName);

	/**
	 * Returns a master-detail combination of this property and the specified
	 * value property.
	 *
	 * @param propertyName
	 *            the value property to observe. May be nested e.g.
	 *            "parent.name"
	 * @param valueType
	 *            the value type of the named property
	 * @return a master-detail combination of this property and the specified
	 *         value property.
	 * @see #values(IJSONValueProperty)
	 */
	public IJSONListProperty values(String propertyName, Class valueType);

	/**
	 * Returns a master-detail combination of this property and the specified
	 * value property. The returned property will observe the specified value
	 * property for all elements observed by this list property.
	 * <p>
	 * Example:
	 *
	 * <pre>
	 * // Observes the list-typed &quot;children&quot; property of a Person object,
	 * // where the elements are Person objects
	 * IJSONListProperty children = JSONProperties.list(Person.class, &quot;children&quot;, Person.class);
	 * // Observes the string-typed &quot;name&quot; property of a Person object
	 * IJSONValueProperty name = JSONProperties.value(Person.class, &quot;name&quot;);
	 * // Observes the names of children of a Person object.
	 * IJSONListProperty childrenNames = children.values(name);
	 * </pre>
	 *
	 * @param property
	 *            the detail property to observe
	 * @return a master-detail combination of this property and the specified
	 *         value property.
	 */
	public IJSONListProperty values(IJSONValueProperty property);
}
