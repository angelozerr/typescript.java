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

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;

/**
 * Utility class to handle image resources.
 */
public class TypeScriptUIImageResource {

	// the image registry
	private static ImageRegistry imageRegistry;

	private static final String IMAGE_DIR = "typescript-images"; //$NON-NLS-1$

	// map of image descriptors since these
	// will be lost by the image registry
	private static Map<String, ImageDescriptor> imageDescriptors;

	// base urls for images
	private static URL ICON_BASE_URL;

	private static final String URL_DLCL = "full/dlcl16/";
	private static final String URL_ELCL = "full/elcl16/";
	private static final String URL_OBJ = "full/obj16/";
	private static final String URL_OVR = "full/ovr16/";

	// General Object Images
	public static final String IMG_LOGO = "logo";
	public static final String IMG_TYPESCRIPT_RESOURCES = "tsresources_obj";
	public static final String IMG_JSX = "jsx";
	
	// Enabled/Disbaled
	public static final String IMG_STOP_ENABLED = "stop_enabled";
	public static final String IMG_STOP_DISABLED = "stop_disabled";
	public static final String IMG_COLLAPSE_ALL_ENABLED = "collapseall_enabled";
	public static final String IMG_COLLAPSE_ALL_DISABLED = "collapseall_disabled";
	public static final String IMG_SYNCED_ENABLED = "synced_enabled";
	public static final String IMG_SYNCED_DISABLED = "synced_disabled";
	
	public static final String DESC_OVR_LIBRARY = "ovr_library";

	private static Map<ImageDescriptor, URL> fURLMap;
	private static final File fTempDir;

	private static int fImageCount;

	static {
		try {
			String pathSuffix = "icons/";
			ICON_BASE_URL = TypeScriptUIPlugin.getDefault().getBundle().getEntry(pathSuffix);
		} catch (Exception e) {
			TypeScriptUIPlugin.log("Images error", e);
		}

		fURLMap = new HashMap<ImageDescriptor, URL>();
		fTempDir = getTempDir();
		fImageCount = 0;
	}

	/**
	 * Cannot construct an ImageResource. Use static methods only.
	 */
	private TypeScriptUIImageResource() {
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
		registerImage(IMG_LOGO, URL_OBJ + IMG_LOGO + ".png");
		registerImage(IMG_TYPESCRIPT_RESOURCES, URL_OBJ + IMG_TYPESCRIPT_RESOURCES + ".gif");
		registerImage(IMG_JSX, URL_OBJ + IMG_JSX + ".png");
		
		registerImage(IMG_STOP_ENABLED, URL_ELCL + "launch_stop.gif");
		registerImage(IMG_STOP_DISABLED, URL_DLCL + "launch_stop.gif");
		registerImage(IMG_COLLAPSE_ALL_ENABLED, URL_ELCL + "collapseall.gif");
		registerImage(IMG_COLLAPSE_ALL_DISABLED, URL_DLCL + "collapseall.gif");
		registerImage(IMG_SYNCED_ENABLED, URL_ELCL + "synced.gif");
		registerImage(IMG_SYNCED_DISABLED, URL_DLCL + "synced.gif");

		registerImage(DESC_OVR_LIBRARY, URL_OVR + "library_ovr.gif");
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
			TypeScriptUIPlugin.log("Error while registering image " + key + " from " + partialURL, e);
		}
	}

	public static void registerImageDescriptor(String key, ImageDescriptor id) {
		if (imageRegistry == null)
			initializeImageRegistry();
		imageRegistry.put(key, id);
		imageDescriptors.put(key, id);
	}

	private static File getTempDir() {
		try {
			File imageDir = TypeScriptUIPlugin.getDefault().getStateLocation().append(IMAGE_DIR).toFile();
			if (imageDir.exists()) {
				// has not been deleted on previous shutdown
				delete(imageDir);
			}
			if (!imageDir.exists()) {
				imageDir.mkdir();
			}
			if (!imageDir.isDirectory()) {
				TypeScriptUIPlugin.logErrorMessage("Failed to create image directory " + imageDir.toString()); //$NON-NLS-1$
				return null;
			}
			return imageDir;
		} catch (IllegalStateException e) {
			// no state location
			return null;
		}
	}

	private static void delete(File file) {
		if (file.isDirectory()) {
			File[] listFiles = file.listFiles();
			for (int i = 0; i < listFiles.length; i++) {
				delete(listFiles[i]);
			}
		}
		file.delete();
	}

	public static URL getImageURL(ImageDescriptor descriptor) {
		if (fTempDir == null)
			return null;

		URL url = fURLMap.get(descriptor);
		if (url != null)
			return url;

		File imageFile = getNewFile();
		ImageData imageData = descriptor.getImageData();
		if (imageData == null) {
			return null;
		}

		ImageLoader loader = new ImageLoader();
		loader.data = new ImageData[] { imageData };
		loader.save(imageFile.getAbsolutePath(), SWT.IMAGE_PNG);

		try {
			url = imageFile.toURI().toURL();
			fURLMap.put(descriptor, url);
			return url;
		} catch (MalformedURLException e) {
			TypeScriptUIPlugin.log("Failed to create image directory ", e); //$NON-NLS-1$
		}
		return null;
	}

	private static File getNewFile() {
		File file;
		do {
			file = new File(fTempDir, String.valueOf(getImageCount()) + ".png"); //$NON-NLS-1$
		} while (file.exists());
		return file;
	}

	private static synchronized int getImageCount() {
		return fImageCount++;
	}
}