package org.eclipse.jface.text.provisional.viewzones;

/**
 * An accessor that allows for zones to be added or removed.
 */
public interface IViewZoneChangeAccessor {

	/**
	 * Create a new view zone.
	 * 
	 * @param zone
	 *            Zone to create
	 */
	void addZone(IViewZone zone);

	/**
	 * Remove a zone
	 * 
	 * @param zone
	 */
	void removeZone(IViewZone zone);

	/**
	 * Change a zone's position. The editor will rescan the `afterLineNumber`
	 * and `afterColumn` properties of a view zone.
	 */
	// void layoutZone(int id);

	/**
	 * Returns number of zones.
	 * 
	 * @return number of zones.
	 */
	int getSize();
}