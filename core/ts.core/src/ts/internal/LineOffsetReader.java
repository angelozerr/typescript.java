package ts.internal;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import ts.LineOffset;

public class LineOffsetReader {

	private LineOffset lineOffset;

	public LineOffsetReader(String contents, int position) {
		List<Integer> lines = readLines(contents);
		int offset = position;
		int current = position;
		int line = 0;
		for (Integer lineOffset : lines) {
			if (line > 0) {
				current -= "\r\n".length();
			}
			if (current <= lineOffset) {
				offset = current;
				break;
			} else {
				current -= lineOffset;
			}
			line++;
		}
		this.lineOffset = new LineOffset(line + 1, offset + 1);
	}

	public LineOffset getLineOffset() {
		return lineOffset;
	}

	private List<Integer> readLines(final String input) {
		final List<Integer> list = new ArrayList<Integer>();
		try {
			final BufferedReader reader = new BufferedReader(new StringReader(input));
			String line = reader.readLine();
			while (line != null) {
				if (list.size() > 0) {
					list.add(line.length());// "\r\n".length());
				} else {
					list.add(line.length());
				}
				line = reader.readLine();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return list;
	}
}
