package ts.resources;

import ts.Location;
import ts.TSException;

public interface ITypeScriptFile {

	String getName();

	boolean isDirty();

	void setDirty(boolean dirty);
	
	void dispose();

	String getPrefix(int position);

	Location getLocation(int position) throws TSException;

	String getContents();

}
