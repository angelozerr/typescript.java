package ts.eclipse.ide.core.compiler;

import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;

import ts.TypeScriptException;
import ts.compiler.ITypeScriptCompiler;
import ts.eclipse.ide.core.resources.jsconfig.IDETsconfigJson;

public interface IIDETypeScriptCompiler extends ITypeScriptCompiler {

	public void compile(IContainer container) throws TypeScriptException, CoreException;

	public void compile(IContainer container,  List<IFile> tsFiles) throws TypeScriptException, CoreException;

	public void compile(IDETsconfigJson tsconfig) throws TypeScriptException, CoreException;

	public void compile(IDETsconfigJson tsconfig,  List<IFile> tsFiles) throws TypeScriptException, CoreException;
}
