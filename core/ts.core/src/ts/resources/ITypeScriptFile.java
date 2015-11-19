package ts.resources;

import ts.LineOffset;
import ts.TSException;

public interface ITypeScriptFile {

	String getName();

	boolean isDirty();

	void setDirty(boolean dirty);
	
	void dispose();

	String getPrefix(int position);

	LineOffset getLineOffset(int position) throws TSException;

	String getContents();

}
