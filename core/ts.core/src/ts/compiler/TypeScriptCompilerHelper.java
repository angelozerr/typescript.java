package ts.compiler;

import java.util.Scanner;

import ts.utils.FileUtils;
import ts.utils.StringUtils;

public class TypeScriptCompilerHelper {

	private static final String COMPILATION_COMPLETE_WATCHING_FOR_FILE_CHANGES = "Compilation complete. Watching for file changes.";
	
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
				}
			}
		} finally {
			if (scanner != null) {
				scanner.close();
			}
		}
	}
}
