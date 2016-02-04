package ts.eclipse.ide.core.resources;

import org.eclipse.core.resources.IResource;

import ts.resources.ITypeScriptFile;

public interface IIDETypeScriptFile extends ITypeScriptFile {

	IResource getResource();

}
