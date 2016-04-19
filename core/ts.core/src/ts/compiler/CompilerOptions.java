package ts.compiler;

/**
 * Instructs the TypeScript compiler how to compile .ts files.
 * 
 * @see http://json.schemastore.org/tsconfig
 *
 */
public class CompilerOptions {

	private String charset;
	private boolean declaration;

	private boolean listFiles;

	private boolean sourceMap;

	private String outDir;
	private boolean allowJs;

	public CompilerOptions() {
	}

	public CompilerOptions(CompilerOptions options) {
		this.setCharset(options.getCharset());
		this.setDeclaration(options.isDeclaration());

		this.setListFiles(options.isListFiles());

		this.setSourceMap(options.isSourceMap());
		
		this.setOutDir(options.getOutDir());
	}

	/**
	 * Returns the character set of the input files.
	 * 
	 * @return the character set of the input files.
	 */
	public String getCharset() {
		return charset;
	}

	/**
	 * The character set of the input files.
	 * 
	 * @param charset
	 */
	public void setCharset(String charset) {
		this.charset = charset;
	}

	/**
	 * Returns true of generates corresponding d.ts files and false otherwise.
	 * 
	 * @return true of generates corresponding d.ts files and false otherwise.
	 */
	public boolean isDeclaration() {
		return declaration;
	}

	/**
	 * Set to true of generates corresponding d.ts files and false otherwise.
	 * 
	 * @param declaration
	 */
	public void setDeclaration(boolean declaration) {
		this.declaration = declaration;
	}

	/**
	 * Returns Print names of files part of the compilation.
	 * 
	 * @return
	 */
	public boolean isListFiles() {
		return listFiles;
	}

	/**
	 * Set Print names of files part of the compilation.
	 * 
	 * @param listFiles
	 */
	public void setListFiles(boolean listFiles) {
		this.listFiles = listFiles;
	}

	/**
	 * Generates corresponding '.map' file.
	 * 
	 * @return
	 */
	public boolean isSourceMap() {
		return sourceMap;
	}

	/**
	 * Generates corresponding '.map' file.
	 * 
	 * @param sourceMap
	 */
	public void setSourceMap(boolean sourceMap) {
		this.sourceMap = sourceMap;
	}

	public String getOutDir() {
		return outDir;
	}

	public void setOutDir(String outDir) {
		this.outDir = outDir;
	}

	public boolean isAllowJs() {
		return allowJs;
	}

	public void setAllowJs(boolean allowJs) {
		this.allowJs = allowJs;
	}

}
