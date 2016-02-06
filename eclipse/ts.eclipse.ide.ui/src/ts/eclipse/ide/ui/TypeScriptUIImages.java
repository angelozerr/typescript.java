/**
 *  Copyright (c) 2015-2016 Angelo ZERR.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package ts.eclipse.ide.ui;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;

import ts.TypeScriptKind;
import ts.eclipse.ide.internal.ui.Trace;
import ts.utils.StringUtils;

/**
 * Utility class to handle image resources.
 */
public class TypeScriptUIImages {
	
	// the image registry
	private static ImageRegistry imageRegistry;

	// map of image descriptors since these
	// will be lost by the image registry
	private static Map<String, ImageDescriptor> imageDescriptors;

	// base urls for images
	private static URL ICON_BASE_URL;

	private static final String URL_DLCL = "full/dlcl16/";
	private static final String URL_ELCL = "full/elcl16/";
	private static final String URL_OBJ = "full/obj16/";

	// General Object Images
	public static final String IMG_SCRIPT = "script";
	public static final String IMG_LOGO = "logo";
	public static final String IMG_PLUGIN = "plugin";
	public static final String IMG_TYPE_DEF = "typedef";

	// Enabled/Disbaled
	public static final String IMG_STOP_ENABLED = "stop_enabled";
	public static final String IMG_STOP_DISABLED = "stop_disabled";

	// Outline
	public static final String IMG_ELCL_SORT = "alphab_sort_co";
	public static final String IMG_ELCL_SYNCED = "synced";

	static {
		try {
			String pathSuffix = "icons/";
			ICON_BASE_URL = TypeScriptUIPlugin.getDefault().getBundle().getEntry(pathSuffix);
		} catch (Exception e) {
			Trace.trace(Trace.SEVERE, "Images error", e);
		}
	}

	/**
	 * Cannot construct an ImageResource. Use static methods only.
	 */
	private TypeScriptUIImages() {
		// do nothing
	}

	/**
	 * Dispose of element images that were created.
	 */
	protected static void dispose() {
		// do nothing
	}

	/**
	 * Return the image with the given key.
	 * 
	 * @param key
	 *            java.lang.String
	 * @return org.eclipse.swt.graphics.Image
	 */
	public static Image getImage(String key) {
		return getImage(key, null);
	}

	/**
	 * Return the image with the given key.
	 * 
	 * @param key
	 *            java.lang.String
	 * @return org.eclipse.swt.graphics.Image
	 */
	public static Image getImage(String key, String keyIfImageNull) {
		if (imageRegistry == null)
			initializeImageRegistry();
		Image image = imageRegistry.get(key);
		if (image == null) {
			if (keyIfImageNull != null) {
				return getImage(keyIfImageNull, null);
			}
			imageRegistry.put(key, ImageDescriptor.getMissingImageDescriptor());
			image = imageRegistry.get(key);
		}
		return image;
	}

	/**
	 * Return the image descriptor with the given key.
	 * 
	 * @param key
	 *            java.lang.String
	 * @return org.eclipse.jface.resource.ImageDescriptor
	 */
	public static ImageDescriptor getImageDescriptor(String key) {
		if (imageRegistry == null)
			initializeImageRegistry();
		ImageDescriptor id = imageDescriptors.get(key);
		if (id != null)
			return id;

		return ImageDescriptor.getMissingImageDescriptor();
	}

	/**
	 * Initialize the image resources.
	 */
	protected static void initializeImageRegistry() {
		imageRegistry = TypeScriptUIPlugin.getDefault().getImageRegistry();
		imageDescriptors = new HashMap<String, ImageDescriptor>();

		// load general object images
		registerImage(IMG_SCRIPT, URL_OBJ + IMG_SCRIPT + ".gif");
		registerImage(IMG_LOGO, URL_OBJ + IMG_LOGO + ".png");
		registerImage(IMG_PLUGIN, URL_OBJ + IMG_PLUGIN + ".gif");
		registerImage(IMG_TYPE_DEF, URL_OBJ + IMG_TYPE_DEF + ".gif");

		registerImage(IMG_STOP_ENABLED, URL_ELCL + "launch_stop.gif");
		registerImage(IMG_STOP_DISABLED, URL_DLCL + "launch_stop.gif");
		registerImage(IMG_ELCL_SORT, URL_ELCL + "alphab_sort_co.png");
		registerImage(IMG_ELCL_SYNCED, URL_ELCL + "synced.gif");
	}

	/**
	 * Register an image with the registry.
	 * 
	 * @param key
	 *            java.lang.String
	 * @param partialURL
	 *            java.lang.String
	 */
	public static void registerImage(String key, String partialURL) {
		try {
			ImageDescriptor id = ImageDescriptor.createFromURL(new URL(ICON_BASE_URL, partialURL));
			registerImageDescriptor(key, id);
		} catch (Exception e) {
			Trace.trace(Trace.SEVERE, "Error registering image " + key + " from " + partialURL, e);
		}
	}

	public static void registerImageDescriptor(String key, ImageDescriptor id) {
		if (imageRegistry == null)
			initializeImageRegistry();
		imageRegistry.put(key, id);
		imageDescriptors.put(key, id);
	}

	public static Image getTypeScriptKindImage(String kind, String kindModifiers, String containerKind) {
		TypeScriptKind tsKind = TypeScriptKind.getKind(kind);
		if (tsKind == null) {
			return null;
		}
		String[] parts = kindModifiers.split(",");
		boolean isInner = !StringUtils.isEmpty(containerKind);

		switch (tsKind) {
		case KEYWORD:
			return null;
		case MODULE:
			
		}
		return null;
	}

}