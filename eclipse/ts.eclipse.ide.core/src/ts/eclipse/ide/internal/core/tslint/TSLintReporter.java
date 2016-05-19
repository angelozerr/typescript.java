package ts.eclipse.ide.internal.core.tslint;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;

import ts.client.Location;
import ts.cmd.ITypeScriptLinterHandler;
import ts.cmd.Severity;
import ts.cmd.tslint.TslintHelper;
import ts.eclipse.ide.core.utils.TypeScriptResourceUtil;
import ts.eclipse.ide.core.utils.WorkbenchResourceUtil;
import ts.nodejs.INodejsProcess;
import ts.nodejs.NodejsProcessAdapter;

public class TSLintReporter extends NodejsProcessAdapter {

	@Override
	public void onMessage(INodejsProcess process, String response) {
		TslintHelper.processJsonMessage(response, new ITypeScriptLinterHandler() {

			@Override
			public void addError(String file, Location startLoc, Location endLoc, Severity severity, String code,
					String message) {
				IFile tsFile = WorkbenchResourceUtil.findFileFromWorkspace(file);
				if (tsFile != null && tsFile.exists()) {
					try {
						String error = TypeScriptResourceUtil.formatTslintError(code, message);
						TypeScriptResourceUtil.addTscMarker(tsFile, error, IMarker.SEVERITY_ERROR, startLoc.getLine(),
								startLoc.getPosition(), endLoc.getPosition());
					} catch (CoreException e) {

					}
				}
			}
		});
	}
}
