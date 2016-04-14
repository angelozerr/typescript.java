package ts.resources.jsonconfig;

public class CompilerOptions {

	private String outDir;

	private boolean alloyJs;

	public String getOutDir() {
		return outDir;
	}

	public void setOutDir(String outDir) {
		this.outDir = outDir;
	}

	public boolean isAlloyJs() {
		return alloyJs;
	}

	public void setAlloyJs(boolean alloyJs) {
		this.alloyJs = alloyJs;
	}

}
