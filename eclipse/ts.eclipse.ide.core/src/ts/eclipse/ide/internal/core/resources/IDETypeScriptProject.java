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

import ts.TypeScriptException;
import ts.client.ITypeScriptServiceClient;
import ts.compiler.ITypeScriptCompiler;
import ts.compiler.TypeScriptCompiler;
import ts.eclipse.ide.core.TypeScriptCorePlugin;
import ts.eclipse.ide.core.console.ITypeScriptConsoleConnector;
import ts.eclipse.ide.core.resources.IIDETypeScriptFile;
import ts.eclipse.ide.core.resources.IIDETypeScriptProject;
import ts.eclipse.ide.core.resources.IIDETypeScriptProjectSettings;
import ts.eclipse.ide.internal.core.Trace;
import ts.eclipse.ide.internal.core.console.TypeScriptConsoleConnectorManager;
import ts.resources.TypeScriptProject;

public class IDETypeScriptProject extends TypeScriptProject implements IIDETypeScriptProject {

	private static final QualifiedName TYPESCRIPT_PROJECT = new QualifiedName(
			TypeScriptCorePlugin.PLUGIN_ID + ".sessionprops", //$NON-NLS-1$
			"TypeScriptProject"); //$NON-NLS-1$

	private final IProject project;

	public IDETypeScriptProject(IProject project) throws CoreException {
		super(project.getLocation().toFile(), new IDETypeScriptProjectSettings(project));
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
	public synchronized IIDETypeScriptFile openFile(IResource file, IDocument document) throws TypeScriptException {
		String fileName = IDETypeScriptFile.getFileName(file);
		IIDETypeScriptFile tsFile = (IIDETypeScriptFile) super.getOpenedFile(fileName);
		if (tsFile == null) {
			tsFile = new IDETypeScriptFile(file, document, this);
		}
		if (!tsFile.isOpened()) {
			tsFile.open();
		}
		return tsFile;
	}

	@Override
	public void closeFile(IResource file) throws TypeScriptException {
		String fileName = IDETypeScriptFile.getFileName(file);
		IIDETypeScriptFile tsFile = (IIDETypeScriptFile) super.getOpenedFile(fileName);
		if (tsFile != null) {
			tsFile.close();
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
				} catch (TypeScriptException e) {
					Trace.trace(Trace.SEVERE, "Error while getting TypeScript client", e);
				}
			}
		}
	}

	private boolean isTraceOnConsole() {
		return getProjectSettings().isTraceOnConsole();
	}

	@Override
	protected ITypeScriptCompiler createCompiler() throws TypeScriptException {
		try {
			File nodeFile = null;
			File tsRepositoryFile = FileLocator.getBundleFile(Platform.getBundle("ts.repository"));
			File tscFile = new File(tsRepositoryFile, "node_modules/typescript/bin/tsc");
			return new TypeScriptCompiler(getProjectDir(), tscFile, nodeFile);
		} catch (IOException e) {
			throw new TypeScriptException(e);
		}
	}

	@Override
	public IIDETypeScriptProjectSettings getProjectSettings() {
		return (IIDETypeScriptProjectSettings) super.getProjectSettings();
	}
}
