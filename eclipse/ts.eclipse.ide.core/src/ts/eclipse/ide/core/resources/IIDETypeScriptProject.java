package ts.eclipse.ide.core.resources;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.text.IDocument;

import ts.TSException;
import ts.resources.ITypeScriptFile;
import ts.resources.ITypeScriptProject;

public interface IIDETypeScriptProject extends ITypeScriptProject {

	/**
	 * Returns the Eclispe project.
	 * 
	 * @return
	 */
	IProject getProject();

	ITypeScriptFile getOpenedFile(IResource file, IDocument document) throws TSException;

}
