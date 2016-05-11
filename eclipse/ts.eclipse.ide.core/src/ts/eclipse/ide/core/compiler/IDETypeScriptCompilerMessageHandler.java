package ts.eclipse.ide.core.compiler;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

import ts.client.Location;
import ts.compiler.ITypeScriptCompilerMessageHandler;
import ts.compiler.TypeScriptCompilerSeverity;
import ts.eclipse.ide.core.resources.jsconfig.IDETsconfigJson;
import ts.eclipse.ide.core.utils.TypeScriptResourceUtil;
import ts.eclipse.ide.internal.core.Trace;

public class IDETypeScriptCompilerMessageHandler implements ITypeScriptCompilerMessageHandler {

	/** Constant for marker type. */
	private static final String MARKER_TYPE = "ts.eclipse.ide.core.typeScriptProblem";

	private final IContainer container;
	private final IDETsconfigJson tsconfig;
	private final List<IFile> filesToRefresh;

	public IDETypeScriptCompilerMessageHandler(IContainer container) throws CoreException {
		this.container = container;
		this.tsconfig = TypeScriptResourceUtil.findTsconfig(container);
		this.filesToRefresh = new ArrayList<IFile>();
		container.deleteMarkers(MARKER_TYPE, true, IResource.DEPTH_INFINITE);
	}

	@Override
	public void addFile(String filePath) {
		IFile file = getFile(filePath);
		if (file != null && !filesToRefresh.contains(file)) {
			filesToRefresh.add(file);
		}
	}

	private IFile getFile(String filePath) {
		IPath path = new Path(filePath);
		if (container.exists(path)) {
			return container.getFile(path);
		}
		return null;
	}

	public List<IFile> getFilesToRefresh() {
		return filesToRefresh;
	}

	@Override
	public void refreshFiles() {
		for (IFile tsFile : getFilesToRefresh()) {
			try {
				TypeScriptResourceUtil.refreshAndCollectEmittedFiles(tsFile, tsconfig, true, null);
			} catch (CoreException e) {
				Trace.trace(Trace.SEVERE, "Error while tsc compilation when ts file is refreshed", e);
			}
		}
	}

	public IDETsconfigJson getTsconfig() {
		return tsconfig;
	}

	@Override
	public void addError(String filename, Location startLoc, Location endLoc, TypeScriptCompilerSeverity severity,
			String code, String message) {
		IFile file = getFile(filename);
		if (file != null) {
			try {
				IMarker marker = file.createMarker(MARKER_TYPE);
				marker.setAttribute(IMarker.MESSAGE, message);
				marker.setAttribute(IMarker.SEVERITY, getSeverity(severity));
				marker.setAttribute(IMarker.LINE_NUMBER, startLoc.getLine());
			} catch (CoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private int getSeverity(TypeScriptCompilerSeverity severity) {
		switch (severity) {
		case error:
			return IMarker.SEVERITY_ERROR;
		case info:
			return IMarker.SEVERITY_INFO;
		default:
			return IMarker.SEVERITY_WARNING;
		}
	}

}
