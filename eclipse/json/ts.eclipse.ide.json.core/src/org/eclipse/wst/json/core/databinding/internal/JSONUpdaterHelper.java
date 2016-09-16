package org.eclipse.wst.json.core.databinding.internal;

import org.eclipse.json.jsonpath.IJSONPath;
import org.eclipse.wst.json.core.document.IJSONDocument;
import org.eclipse.wst.json.core.document.IJSONModel;
import org.eclipse.wst.json.core.document.IJSONNode;
import org.eclipse.wst.json.core.document.IJSONObject;
import org.eclipse.wst.json.core.document.IJSONPair;
import org.eclipse.wst.json.core.document.IJSONValue;
import org.eclipse.wst.sse.core.StructuredModelManager;
import org.eclipse.wst.sse.core.internal.provisional.text.IStructuredDocument;

public class JSONUpdaterHelper {

	public static Object getValue(IStructuredDocument document, IJSONPath path) {
		IJSONModel model = null;
		try {
			model = (IJSONModel) StructuredModelManager.getModelManager().getModelForRead(document);
			IJSONPair pair = findByPath(model.getDocument(), path.getSegments());
			if (pair != null) {
				IJSONValue value = pair.getValue();
				switch (value.getNodeType()) {
				case IJSONNode.VALUE_BOOLEAN_NODE:
					return Boolean.parseBoolean(value.getSimpleValue().replaceAll("[\n\ts]", ""));
				case IJSONNode.VALUE_NUMBER_NODE:
					return Integer.parseInt(value.getSimpleValue());
				case IJSONNode.VALUE_STRING_NODE:
					return value.getSimpleValue();
				}
				return value;
			}
		} finally {
			if (model != null) {
				model.releaseFromRead();
			}
		}
		return null;
	}

	public static void setValue(IStructuredDocument document, IJSONPath path, Object value) {
		IJSONModel model = null;
		try {
			model = (IJSONModel) StructuredModelManager.getModelManager().getModelForEdit(document);
			IJSONNode root = model.getDocument().getFirstChild();
			IJSONNode lastParentNotNull = validateParent(root);
			String[] segments = path.getSegments();
			StringBuilder newContent = new StringBuilder();
			int nbObjectToClose = 0;
			IJSONPair pair = null;
			IJSONNode parent = lastParentNotNull;
			String name = null;
			int startIndex = -1;
			for (int i = 0; i < segments.length; i++) {
				name = segments[i];
				IJSONPair node = findByPath(parent, name);
				if (node != null) {
					// JSON pair founded
					pair = ((IJSONPair) node);
					parent = validateParent(pair.getValue());
					if (parent != null) {
						lastParentNotNull = parent;
					}
				} else {
					// JSON pair not founded
					if (parent == null) {
						newContent.append("{");
						nbObjectToClose++;
					}
					if (startIndex == -1) {
						startIndex = i + 1;
					}
					newLineAndIndent(i, newContent);
					newContent.append("\"");
					newContent.append(name);
					newContent.append("\": ");
					parent = null;
				}

			}
			newContent.append(value);

			for (int i = 0; i < nbObjectToClose; i++) {
				newLineAndIndent(nbObjectToClose - i - startIndex, newContent);
				newContent.append("}");
			}

			if (root == null) {
				// newContent.append("\n}");
				document.set(newContent.toString());
			} else {
				if (pair != null) {
					IJSONValue replaceValue = pair.getValue();
					int offset = replaceValue.getStartOffset();
					int length = replaceValue.getEndOffset() - replaceValue.getStartOffset();
					if (isObjectOrArray(replaceValue)) {
						offset++;
						length = length - 2;
					}
					document.replaceText(document, offset, length, newContent.toString());
				} else {
					if (lastParentNotNull.hasChildNodes()) {
						newContent.append(",");
					}
					document.replaceText(document, lastParentNotNull.getStartOffset() + 1, 0, newContent.toString());
				}
			}
		} finally {
			if (model != null) {
				model.releaseFromEdit();
			}
		}

	}

	private static IJSONNode validateParent(IJSONNode node) {
		return isObjectOrArray(node) ? node : null;
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
