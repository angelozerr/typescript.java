package ts.eclipse.ide.internal.core.launch;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.IStreamListener;
import org.eclipse.debug.core.model.IStreamMonitor;

import ts.eclipse.ide.core.resources.jsconfig.IDETsconfigJson;
import ts.eclipse.ide.core.utils.TypeScriptResourceUtil;
import ts.eclipse.ide.internal.core.Trace;
import ts.utils.FileUtils;

public class TscStreamListener implements IStreamListener {

	private final IContainer container;
	private final IDETsconfigJson tsconfig;
	private final List<IFile> filesToRefresh;

	public TscStreamListener(IContainer container) throws CoreException {
		this.container = container;
		this.tsconfig = TypeScriptResourceUtil.findTsconfig(container);
		this.filesToRefresh = new ArrayList<IFile>();
	}

	@Override
	public void streamAppended(String text, IStreamMonitor monitor) {
		Scanner scanner = null;
		try {
			scanner = new Scanner(text);
			while (scanner.hasNextLine()) {
				String line = scanner.nextLine();
				line = line.trim(); // remove leading whitespace
				if (line.endsWith(FileUtils.TS_EXTENSION) || line.endsWith(FileUtils.TSX_EXTENSION)) {
					addFile(line);
				} else if (line.contains("Compilation complete. Watching for file changes.")) {
					refreshFiles();
				}
			}
		} finally {
			if (scanner != null) {
				scanner.close();
			}
		}
	}

	private void addFile(String filePath) {
		IPath path = new Path(filePath);
		if (container.exists(path)) {
			IFile file = container.getFile(path);
			if (!filesToRefresh.contains(file)) {
				filesToRefresh.add(file);
			}
		}
	}

	public List<IFile> getFilesToRefresh() {
		return filesToRefresh;
	}

	public void refreshFiles() {
		for (IFile tsFile : getFilesToRefresh()) {
			try {
				TypeScriptResourceUtil.refreshAndCollectEmittedFiles(tsFile, tsconfig, true, null);
			} catch (CoreException e) {
				Trace.trace(Trace.SEVERE, "Error while tsc compilation when ts file is refreshed", e);
			}
		}
	}

	public boolean isWatch() {
		return tsconfig != null && tsconfig.getCompilerOptions() != null && tsconfig.getCompilerOptions().isWatch();
	}

}
