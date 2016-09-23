package org.eclipse.wst.json.core.databinding.internal;

import org.eclipse.json.jsonpath.IJSONPath;
import org.eclipse.wst.json.core.databinding.IExtendedJSONPath;
import org.eclipse.wst.json.core.document.IJSONDocument;
import org.eclipse.wst.json.core.document.IJSONModel;
import org.eclipse.wst.json.core.document.IJSONNode;
import org.eclipse.wst.json.core.document.IJSONObject;
import org.eclipse.wst.json.core.document.IJSONPair;
import org.eclipse.wst.json.core.document.IJSONValue;
import org.eclipse.wst.sse.core.StructuredModelManager;
import org.eclipse.wst.sse.core.internal.provisional.text.IStructuredDocument;

public class JSONUpdaterHelper {

	private static final int NO_START_INDEX = -2;

	public static Object getValue(IStructuredDocument document, IJSONPath path) {
		IJSONModel model = null;
		try {
			model = (IJSONModel) StructuredModelManager.getModelManager().getModelForRead(document);
			IJSONPair pair = findByPath(model.getDocument(), path.getSegments());
			if (pair != null) {
				IJSONValue value = pair.getValue();
				return getValue(value);
			}
		} finally {
			if (model != null) {
				model.releaseFromRead();
			}
		}
		return null;
	}

	public static Object getValue(IJSONValue value) {
		switch (value.getNodeType()) {
		case IJSONNode.VALUE_BOOLEAN_NODE:
			return Boolean.parseBoolean(trimSPaces(value.getSimpleValue()));
		case IJSONNode.VALUE_NUMBER_NODE:
			return Integer.parseInt(trimSPaces(value.getSimpleValue()));
		case IJSONNode.VALUE_STRING_NODE:
			String s = trimSPaces(value.getSimpleValue());
			if (s.startsWith("\"")) {
				s = s.substring(1, s.length());
			}
			if (s.endsWith("\"")) {
				s = s.substring(0, s.length() - 1);
			}
			return s;
		}
		return value;
	}

	private static String trimSPaces(String s) {
		return s.replaceAll("[\n\t]", "").trim();
	}

	public static void setValue(IStructuredDocument document, IJSONPath path, Object value) {
		IJSONModel model = null;
		try {
			model = (IJSONModel) StructuredModelManager.getModelManager().getModelForEdit(document);
			IJSONNode parent = model.getDocument().getFirstChild();
			String[] segments = path.getSegments();
			String name = null;
			int replaceOffset = 0;
			int replaceLength = 0;
			boolean isArray = false;
			int startIndex = NO_START_INDEX;
			StringBuilder newContent = new StringBuilder();
			for (int i = 0; i < segments.length; i++) {
				name = segments[i];
				isArray = isArray(path, i);
				IJSONPair node = findByPath(parent, name);
				if (node != null) {
					parent = node;
					IJSONNode jsonValue = node.getValue();
					if (isObjectOrArray(jsonValue)) {
						parent = jsonValue;
						replaceOffset = getEndOffset(parent, true);
					} else {
						if (jsonValue != null) {
							replaceOffset = jsonValue.getStartOffset();
							replaceLength = jsonValue.getFirstStructuredDocumentRegion().getFirstRegion().getLength();
						} else {

						}
					}
				} else {
					if (isObjectOrArray(parent)) {
						replaceOffset = getEndOffset(parent, true);
						if (parent.hasChildNodes()) {
							newContent.append(",");
						}
					} else {
						newContent.append(isArray ? "[" : "{");
						if (startIndex == NO_START_INDEX) {
							startIndex = i - 1;
						}
					}
					newLineAndIndent(i, newContent);
					newContent.append("\"");
					newContent.append(name);
					newContent.append("\": ");
					parent = null;
				}
			}

			if (value instanceof String) {
				newContent.append("\"");
				newContent.append(value);
				newContent.append("\"");
			} else {
				newContent.append(value);
			}

			if (startIndex != NO_START_INDEX) {
				// close JSON object or Array
				for (int i = segments.length - 1; i > startIndex; i--) {
					if (i == -1) {
						newLineAndIndent(0, newContent);
						newContent.append("}");
					} else {
						name = segments[i];
						isArray = isArray(path, i);
						newLineAndIndent(i - 1, newContent);
						newContent.append(isArray ? "]" : "}");
					}
				}
			}
			document.replaceText(document, replaceOffset, replaceLength, newContent.toString());
		} finally {
			if (model != null) {
				model.releaseFromEdit();
			}
		}
	}

	private static boolean isArray(IJSONPath path, int i) {
		if (path instanceof IExtendedJSONPath) {
			return ((IExtendedJSONPath) path).isArray(i);
		}
		return false;
	}

	private static int getEndOffset(IJSONNode parent, boolean inside) {
		if (parent == null) {
			return 0;
		}
		switch (parent.getNodeType()) {
		case IJSONNode.OBJECT_NODE:
			if (parent.hasChildNodes()) {
				IJSONNode lastChild = parent.getLastChild();
				boolean childInside = inside
						&& (isSimpleValue(lastChild) || (lastChild.getNodeType() == IJSONNode.PAIR_NODE
								&& isSimpleValue(((IJSONPair) lastChild).getValue())));
				return getEndOffset(lastChild, childInside);
			}
			return parent.getStartOffset() + (inside ? 1 : 0);
		case IJSONNode.PAIR_NODE:
			if (!inside) {
				return parent.getEndOffset();
			}

			IJSONPair pair = (IJSONPair) parent;
			IJSONValue value = (IJSONValue) pair.getValue();
			if (value != null) {
				return getEndOffset(value, false);
			}
			return pair.getEndOffset();
		case IJSONNode.VALUE_BOOLEAN_NODE:
		case IJSONNode.VALUE_NULL_NODE:
		case IJSONNode.VALUE_NUMBER_NODE:
		case IJSONNode.VALUE_STRING_NODE:
			return parent.getStartOffset() + parent.getFirstStructuredDocumentRegion().getFirstRegion().getLength();
		default:
			return parent.getEndOffset();
		}
	}

	private static boolean isSimpleValue(IJSONNode node) {
		if (node == null) {
			return false;
		}
		return node.getNodeType() == IJSONNode.VALUE_BOOLEAN_NODE || node.getNodeType() == IJSONNode.VALUE_NULL_NODE
				|| node.getNodeType() == IJSONNode.VALUE_NUMBER_NODE
				|| node.getNodeType() == IJSONNode.VALUE_STRING_NODE;
	}

	private static boolean isObjectOrArray(IJSONNode node) {
		if (node == null) {
			return false;
		}
		int nodeType = node.getNodeType();
		return nodeType == IJSONNode.OBJECT_NODE || nodeType == IJSONNode.ARRAY_NODE;
	}

	private static void newLineAndIndent(int indent, StringBuilder newContent) {
		newContent.append("\n");
		for (int j = 0; j <= indent; j++) {
			newContent.append("\t");
		}
	}

	public static IJSONPair findByPath(IJSONDocument document, String[] segments) {
		IJSONNode parent = document.getFirstChild();
		IJSONPair pair = null;
		for (int i = 0; i < segments.length; i++) {
			pair = findByPath(parent, segments[i]);
			if (pair != null) {
				parent = pair.getValue();
			}
		}
		return pair;
	}

	private static IJSONPair findByPath(IJSONNode node, String name) {
		if (node == null || node.getNodeType() != IJSONNode.OBJECT_NODE) {
			return null;
		}

		IJSONObject obj = (IJSONObject) node;
		for (int i = 0; i < obj.getLength(); i++) {
			try {
				IJSONNode n = (IJSONNode) obj.getClass().getMethod("item", int.class).invoke(obj, i);
				if (n.getNodeType() == IJSONNode.PAIR_NODE) {
					IJSONPair pair = (IJSONPair) n;
					if (name.equals(pair.getName())) {
						return pair;
					}
				}
			} catch (Exception e) {
				return null;
			}
		}
		return null;
	}

}
