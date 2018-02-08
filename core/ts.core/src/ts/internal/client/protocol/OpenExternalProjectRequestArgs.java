package ts.internal.client.protocol;

import java.util.List;

import ts.client.ScriptKindName;
import ts.cmd.tsc.CompilerOptions;

/**
 * @see https://github.com/Microsoft/TypeScript/blob/master/src/server/protocol.ts
 */
public class OpenExternalProjectRequestArgs {

	public static class ExternalFile {
		/**
		 * Name of file file
		 */
		String fileName;
		/**
		 * Script kind of the file
		 */
		ScriptKindName scriptKind;
		/**
		 * Whether file has mixed content (i.e. .cshtml file that combines html markup
		 * with C#/JavaScript)
		 */
		Boolean hasMixedContent;
		/**
		 * Content of the file
		 */
		String content;

		public ExternalFile(String fileName, ScriptKindName scriptKind, Boolean hasMixedContent, String content) {
			this.fileName = fileName;
			this.scriptKind = scriptKind;
			this.hasMixedContent = hasMixedContent;
			this.content = content;
		}

	}

	/**
	 * Project name
	 */
	String projectFileName;
	/**
	 * List of root files in project
	 */
	List<ExternalFile> rootFiles;
	/**
	 * Compiler options for the project
	 */
	CompilerOptions options;
	// /**
	// * Explicitly specified type acquisition for the project
	// */
	// typeAcquisition?: TypeAcquisition;
	public OpenExternalProjectRequestArgs(String projectFileName, List<ExternalFile> rootFiles,
			CompilerOptions options) {
		this.projectFileName = projectFileName;
		this.rootFiles = rootFiles;
		this.options = options;
	}
}
