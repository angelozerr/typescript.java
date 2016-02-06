package ts.resources;

import ts.Location;
import ts.TypeScriptException;
import ts.server.completions.ITypeScriptCompletionCollector;
import ts.server.definition.ITypeScriptDefinitionCollector;

public interface ITypeScriptFile {

	ITypeScriptProject getProject();
	
	String getName();

	boolean isOpened();
	
	boolean isDirty();

	void setDirty(boolean dirty);

	String getPrefix(int position);

	Location getLocation(int position) throws TypeScriptException;

	int getPosition(int line, int offset) throws TypeScriptException;

	String getContents();

	void open() throws TypeScriptException;

	void close() throws TypeScriptException;
	
	void synch() throws TypeScriptException;
	
	void completions(int position, ITypeScriptCompletionCollector collector) throws TypeScriptException;

	void definition(int position, ITypeScriptDefinitionCollector collector) throws TypeScriptException;


}
