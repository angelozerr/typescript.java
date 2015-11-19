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
import ts.eclipse.ide.core.resources.IDETypeScriptFile;
import ts.eclipse.ide.core.resources.IIDETypeScriptProject;
import ts.resources.ITypeScriptFile;
import ts.resources.TypeScriptProject;
import ts.server.ITypeScriptServiceClient;
import ts.server.nodejs.NodeJSTSClient;

public class IDETypeScriptProject extends TypeScriptProject implements IIDETypeScriptProject {

	private static final QualifiedName TYPESCRIPT_PROJECT = new QualifiedName(
			TypeScriptCorePlugin.PLUGIN_ID + ".sessionprops", //$NON-NLS-1$
			"TypeScriptProject"); //$NON-NLS-1$

	private final IProject project;
	private ITypeScriptServiceClient client;

	public IDETypeScriptProject(IProject project) throws CoreException {
		super(project.getLocation().toFile());
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

	public void dispose() {
		if (client != null) {
			client.dispose();
		}
	}

	@Override
	public ITypeScriptServiceClient getClient() throws TSException {
		if (client == null) {
			try {
				File nodeFile = null;
				File tsRepositoryFile = FileLocator.getBundleFile(Platform.getBundle("ts.repository"));
				File tsserverFile = new File(tsRepositoryFile, "node_modules/typescript/bin/tsserver");
				this.client = new NodeJSTSClient(getProjectDir(), tsserverFile, nodeFile);
			} catch (IOException e) {
				throw new TSException(e);
			}
		}
		return client;
	}

	@Override
	public ITypeScriptFile getFile(IResource file, IDocument document) throws TSException {
		String fileName = IDETypeScriptFile.getFileName(file);
		ITypeScriptFile tsFile = super.getFile(fileName);
		if (tsFile == null) {
			tsFile = new IDETypeScriptFile(file, document);
			super.openFile(tsFile);
		}
		return tsFile;
	}
}
