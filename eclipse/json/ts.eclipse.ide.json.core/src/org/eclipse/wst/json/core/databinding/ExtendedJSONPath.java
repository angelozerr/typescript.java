package org.eclipse.wst.json.core.databinding;

import java.util.ArrayList;
import java.util.List;

public class ExtendedJSONPath implements IExtendedJSONPath {

	public static final String TOKEY_ARRAY = "[*]";

	private final String[] segments;
	private List<Integer> indexOfSegmentArray;

	public ExtendedJSONPath(String[] segments) {
		this.segments = process(segments);
	}

	private String[] process(String[] segments) {
		this.indexOfSegmentArray = new ArrayList<Integer>();
		String[] newSegments = new String[segments.length];
		String name = null;
		boolean isArray = false;
		for (int i = 0; i < segments.length; i++) {
			name = segments[i];
			isArray = name.endsWith("[*]");
			if (isArray) {
				name = name.substring(0, name.length() - 3);
				indexOfSegmentArray.add(i);
			}
			newSegments[i] = name;
		}
		return newSegments;
	}

	public ExtendedJSONPath(String expression) {
		this(expression.split("[.]"));
	}

	@Override
	public String toString() {
		StringBuilder buffer = new StringBuilder();
		buffer.append("/");
		if (segments != null) {
			for (String seg : segments) {
				buffer.append(seg);
				buffer.append("/");
			}
		}
		return buffer.toString();
	}

	@Override
	public boolean isArray(int index) {
		return indexOfSegmentArray.contains(index);
	}

	@Override
	public String[] getSegments() {
		return segments;
	}
}
