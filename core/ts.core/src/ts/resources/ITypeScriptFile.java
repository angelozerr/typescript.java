package ts.resources;

import ts.Location;
import ts.TSException;
import ts.server.completions.ITypeScriptCompletionCollector;
import ts.server.definition.ITypeScriptDefinitionCollector;

public interface ITypeScriptFile {

	ITypeScriptProject getProject();
	
	String getName();

	boolean isDirty();

	void setDirty(boolean dirty);

	void dispose();

	String getPrefix(int position);

	Location getLocation(int position) throws TSException;

	int getPosition(int line, int offset) throws TSException;

	String getContents();

	void open() throws TSException;

	void close() throws TSException;
	
	void completions(int position, ITypeScriptCompletionCollector collector) throws TSException;

	void definition(int position, ITypeScriptDefinitionCollector collector) throws TSException;


}
