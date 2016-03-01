package ts.eclipse.ide.internal.core.repository;

import ts.repository.ITypeScriptRepositoryManager;
import ts.repository.TypeScriptRepositoryManager;

public class IDETypeScriptRepositoryManager extends TypeScriptRepositoryManager {

	public static final ITypeScriptRepositoryManager INSTANCE = new IDETypeScriptRepositoryManager();
}
