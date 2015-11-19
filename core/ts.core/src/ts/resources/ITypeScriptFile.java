package ts.resources;

import ts.TSException;

public interface ITypeScriptFile {

	String getName();

	boolean isDirty();

	void setDirty(boolean dirty);
	
	void dispose();

	String getPrefix(int position);

	int getOffset(int position) throws TSException;

	int getLine(int position) throws TSException;

	String getContents();

}
