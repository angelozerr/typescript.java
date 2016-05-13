package ts.compiler;

/**
 * Instructs the TypeScript compiler how to compile .ts files.
 * 
 * @see http://json.schemastore.org/tsconfig
 * @se https://www.typescriptlang.org/docs/handbook/compiler-options.html
 *
 */
public class CompilerOptions {

	private Boolean allowJs;

	private String charset;
	private boolean declaration;

	private boolean listFiles;

	private String mapRoot;

	private boolean sourceMap;

	private String outDir;
	private String out;
	private String outFile;

	private String sourceRoot;

	private boolean traceResolution;

	private Boolean watch;

	public CompilerOptions() {
	}

	public CompilerOptions(CompilerOptions options) {
		this.setAllowJs(options.isAllowJs());
		this.setCharset(options.getCharset());
		this.setDeclaration(options.isDeclaration());
		this.setListFiles(options.isListFiles());
		this.setMapRoot(options.getMapRoot());
		this.setSourceMap(options.isSourceMap());
		this.setSourceRoot(options.getSourceRoot());
		this.setOutDir(options.getOutDir());
	}

	/**
	 * Returns true if allow JavaScript files to be compiled and false
	 * otherwise.
	 * 
	 * @return true if allow JavaScript files to be compiled and false
	 *         otherwise.
	 */
	public Boolean isAllowJs() {
		return allowJs;
	}

	/**
	 * Allow JavaScript files to be compiled.
	 * 
	 * @param allowJs
	 */
	public void setAllowJs(Boolean allowJs) {
		this.allowJs = allowJs;
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
	 * 
	 * @return
	 */
	public String getMapRoot() {
		return mapRoot;
	}

	public void setMapRoot(String mapRoot) {
		this.mapRoot = mapRoot;
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

	/**
	 * Specifies the location where debugger should locate TypeScript files
	 * instead of source locations. Use this flag if the sources will be located
	 * at run-time in a different location than that at design-time. The
	 * location specified will be embedded in the sourceMap to direct the
	 * debugger where the source files where be located.
	 * 
	 * @return
	 */
	public String getSourceRoot() {
		return sourceRoot;
	}

	/**
	 * Specifies the location where debugger should locate TypeScript files
	 * instead of source locations. Use this flag if the sources will be located
	 * at run-time in a different location than that at design-time. The
	 * location specified will be embedded in the sourceMap to direct the
	 * debugger where the source files where be located.
	 * 
	 * @param sourceRoot
	 */
	public void setSourceRoot(String sourceRoot) {
		this.sourceRoot = sourceRoot;
	}

	/**
	 * Report module resolution log messages.
	 * 
	 * @return
	 */
	public boolean isTraceResolution() {
		return traceResolution;
	}

	/**
	 * Report module resolution log messages.
	 * 
	 * @param traceResolution
	 */
	public void setTraceResolution(boolean traceResolution) {
		this.traceResolution = traceResolution;
	}

	public void setWatch(Boolean watch) {
		this.watch = watch;
	}

	public Boolean isWatch() {
		return watch;
	}

}
