package ts.eclipse.ide.internal.core.resources;

import java.io.File;
import java.io.IOException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.jface.text.IDocument;

import ts.TSException;
import ts.eclipse.ide.core.TypeScriptCorePlugin;
import ts.eclipse.ide.core.console.ITypeScriptConsoleConnector;
import ts.eclipse.ide.core.resources.IDETypeScriptFile;
import ts.eclipse.ide.core.resources.IIDETypeScriptProject;
import ts.eclipse.ide.internal.core.Trace;
import ts.eclipse.ide.internal.core.console.TypeScriptConsoleConnectorManager;
import ts.resources.ITypeScriptFile;
import ts.resources.TypeScriptProject;
import ts.server.ITypeScriptServiceClient;
import ts.server.ITypeScriptServiceClientFactory;
import ts.server.nodejs.NodeJSTypeScriptServiceClient;

public class IDETypeScriptProject extends TypeScriptProject
		implements IIDETypeScriptProject, ITypeScriptServiceClientFactory {

	private static final QualifiedName TYPESCRIPT_PROJECT = new QualifiedName(
			TypeScriptCorePlugin.PLUGIN_ID + ".sessionprops", //$NON-NLS-1$
			"TypeScriptProject"); //$NON-NLS-1$

	private final IProject project;

	public IDETypeScriptProject(IProject project) throws CoreException {
		super(project.getLocation().toFile(), null);
		this.project = project;
		project.setSessionProperty(TYPESCRIPT_PROJECT, this);
	}

	/**
	 * Returns the Eclispe project.
	 * 
	 * @return
	 */
	@Override
	public IProject getProject() {
		return project;
	}

	public static boolean hasTypeScriptNature(IProject project) {
		return true;
	}

	public static IDETypeScriptProject getTypeScriptProject(IProject project) throws CoreException {
		return (IDETypeScriptProject) project.getSessionProperty(TYPESCRIPT_PROJECT);
	}

	public void load() throws IOException {

	}

	@Override
	public ITypeScriptFile getOpenedFile(IResource file, IDocument document) throws TSException {
		String fileName = IDETypeScriptFile.getFileName(file);
		ITypeScriptFile tsFile = super.getOpenedFile(fileName);
		if (tsFile == null) {
			tsFile = new IDETypeScriptFile(file, document);
			super.openFile(tsFile);
		}
		return tsFile;
	}

	@Override
	public ITypeScriptServiceClient create(File projectDir) throws TSException {
		try {
			File nodeFile = null;
			File tsRepositoryFile = FileLocator.getBundleFile(Platform.getBundle("ts.repository"));
			File tsserverFile = new File(tsRepositoryFile, "node_modules/typescript/bin/tsserver");
			return new NodeJSTypeScriptServiceClient(getProjectDir(), tsserverFile, nodeFile);
		} catch (IOException e) {
			throw new TSException(e);
		}
	}

	@Override
	protected void onCreateClient(ITypeScriptServiceClient client) {
		configureConsole();
	}

	@Override
	public void configureConsole() {
		synchronized (serverLock) {
			if (hasClient()) {
				// There is a TypeScript client instance., Retrieve the well
				// connector
				// the
				// the eclipse console.
				try {
					ITypeScriptServiceClient client = getClient();
					ITypeScriptConsoleConnector connector = TypeScriptConsoleConnectorManager.getManager()
							.getConnector(client);
					if (connector != null) {
						if (isTraceOnConsole()) {
							// connect the tern server to the eclipse console.
							connector.connectToConsole(client, this);
						} else {
							// disconnect the tern server to the eclipse
							// console.
							connector.disconnectToConsole(client, this);
						}
					}
				} catch (TSException e) {
					Trace.trace(Trace.SEVERE, "Error while getting TypeScript client", e);
				}
			}
		}
	}

	private boolean isTraceOnConsole() {
		return true;
	}

}
