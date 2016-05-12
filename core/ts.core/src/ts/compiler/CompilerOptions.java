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
	private String out;
	private String outFile;

	private boolean allowJs;
	private Boolean watch;

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

	/**
	 * Concatenate and emit output to single file. The order of concatenation is
	 * determined by the list of files passed to the compiler on the command
	 * line along with triple-slash references and imports. See output file
	 * order documentation for more details.
	 * 
	 * @return
	 */
	public String getOutFile() {
		return outFile;
	}

	/**
	 * Concatenate and emit output to single file. The order of concatenation is
	 * determined by the list of files passed to the compiler on the command
	 * line along with triple-slash references and imports. See output file
	 * order documentation for more details.
	 * 
	 * @param outFile
	 */
	public void setOutFile(String outFile) {
		this.outFile = outFile;
	}

	/**
	 * Same thing than outFile but deprectaed.
	 * 
	 * @return
	 */
	public String getOut() {
		return out;
	}

	/**
	 * Same thing than outFile but deprectaed.
	 * 
	 * @param out
	 */
	public void setOut(String out) {
		this.out = out;
	}

	public boolean isAllowJs() {
		return allowJs;
	}

	public void setAllowJs(boolean allowJs) {
		this.allowJs = allowJs;
	}

	public void setWatch(Boolean watch) {
		this.watch = watch;
	}

	public Boolean isWatch() {
		return watch;
	}

}
