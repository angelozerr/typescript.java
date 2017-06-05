package org.eclipse.jface.text.provisional.viewzones;

import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.GC;

public interface IViewZoneRenderer<T extends IViewZone> {

	void draw(T viewZone, int paintX, int paintY, GC gc, StyledText styledText);

}
