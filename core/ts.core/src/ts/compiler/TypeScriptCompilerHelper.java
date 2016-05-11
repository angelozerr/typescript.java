package ts.compiler;

import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ts.client.Location;
import ts.utils.FileUtils;
import ts.utils.StringUtils;

public class TypeScriptCompilerHelper {

	private static final Pattern TSC_ERROR_PATTERN = Pattern.compile(
			"^([^\\s].*)\\((\\d+|\\d+,\\d+|\\d+,\\d+,\\d+,\\d+)\\):\\s+(error|warning|info)\\s+(TS\\d+)\\s*:\\s*(.*)$");

	private static final String COMPILATION_COMPLETE_WATCHING_FOR_FILE_CHANGES = "Compilation complete. Watching for file changes.";

	public static void main(String[] args) {
		processMessage("a.ts(1,5): error TS1005: ';' expected.", null);
	}

	public static void processMessage(String text, ITypeScriptCompilerMessageHandler handler) {
		if (StringUtils.isEmpty(text)) {
			return;
		}

		Scanner scanner = null;
		try {
			scanner = new Scanner(text);
			while (scanner.hasNextLine()) {
				String line = scanner.nextLine();
				line = line.trim(); // remove leading whitespace
				if (line.endsWith(FileUtils.TS_EXTENSION) || line.endsWith(FileUtils.TSX_EXTENSION)) {
					handler.addFile(line);
				} else if (line.contains(COMPILATION_COMPLETE_WATCHING_FOR_FILE_CHANGES)) {
					handler.refreshFiles();
				} else {
					Matcher m = TSC_ERROR_PATTERN.matcher(line);
					if (m.matches()) {
						String file = m.group(1);
						String[] location = m.group(2).split(",");
						Location startLoc = createLocation(location, true);
						Location endLoc = createLocation(location, false);
						String severity = m.group(3);
						String code = m.group(4);
						String message = m.group(5);
						handler.addError(file, startLoc, endLoc, StringUtils.isEmpty(severity)
								? TypeScriptCompilerSeverity.info : TypeScriptCompilerSeverity.valueOf(severity), code,
								message);
					}
				}
			}
		} finally {
			if (scanner != null) {
				scanner.close();
			}
		}
	}

	private static Location createLocation(String[] location, boolean start) {
		if (start) {
			int line = getInt(location, 0);
			int offset = getInt(location, 1);
			return new Location(line, offset);
		}
		return null;
	}

	private static int getInt(String[] location, int index) {
		if (index < location.length) {
			return Integer.parseInt(location[index]);
		}
		return 0;
	}
}
