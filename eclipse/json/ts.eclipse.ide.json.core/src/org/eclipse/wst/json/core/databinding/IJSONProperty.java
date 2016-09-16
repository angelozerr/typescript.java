package org.eclipse.wst.json.core.databinding;

import org.eclipse.core.databinding.property.IProperty;
import org.eclipse.json.jsonpath.IJSONPath;

public interface IJSONProperty extends IProperty {

	IJSONPath getPath();
}
