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
package ts.eclipse.jface.images;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.graphics.Image;

import ts.TypeScriptKind;
import ts.client.IKindProvider;
import ts.utils.StringUtils;

/**
 * Image registry for tern images.
 *
 */
public class TypeScriptImagesRegistry {

	public static final String IMG_KEYWORD = "ts.eclipse.jface.IMG_KEYWORD";
	public static final String IMG_PACKAGE = "ts.eclipse.jface.IMG_PACKAGE";
	public static final String IMG_SCRIPT = "ts.eclipse.jface.IMG_SCRIPT";
	public static final String IMG_FOLDER = "ts.eclipse.jface.IMG_FOLDER";
	public static final String IMG_CLASS = "ts.eclipse.jface.IMG_CLASS";
	public static final String IMG_ALIAS = "ts.eclipse.jface.IMG_ALIAS";
	public static final String IMG_INTERFACE = "ts.eclipse.jface.IMG_INTERFACE";
	public static final String IMG_ENUM_DEFAULT = "ts.eclipse.jface.IMG_ENUM_DEFAULT";
	public static final String IMG_ENUM_PRIVATE = "ts.eclipse.jface.IMG_ENUM_PRIVATE";
	public static final String IMG_FIELD_DEFAULT = "ts.eclipse.jface.IMG_FIELD_DEFAULT";
	public static final String IMG_FIELD_PRIVATE = "ts.eclipse.jface.IMG_FIELD_PRIVATE";
	public static final String IMG_FIELD_PUBLIC = "ts.eclipse.jface.IMG_FIELD_PUBLIC";
	public static final String IMG_METHOD_DEFAULT = "ts.eclipse.jface.IMG_METHOD_DEFAULT";
	public static final String IMG_METHOD_PRIVATE = "ts.eclipse.jface.IMG_METHOD_PRIVATE";
	public static final String IMG_METHOD_PUBLIC = "ts.eclipse.jface.IMG_METHOD_PUBLIC";
	public static final String IMG_TYPE_DEFAULT = "ts.eclipse.jface.IMG_TYPE_DEFAULT";
	public static final String IMG_TYPE_PRIVATE = "ts.eclipse.jface.IMG_TYPE_PRIVATE";
	public static final String IMG_TYPE_PUBLIC = "ts.eclipse.jface.IMG_TYPE_PUBLIC";

	static {
		registerImageDescriptor(IMG_KEYWORD,
				ImageDescriptor.createFromFile(TypeScriptImagesRegistry.class, "keyword_obj.png"));
		registerImageDescriptor(IMG_PACKAGE,
				ImageDescriptor.createFromFile(TypeScriptImagesRegistry.class, "package_obj.gif"));
		registerImageDescriptor(IMG_SCRIPT,
				ImageDescriptor.createFromFile(TypeScriptImagesRegistry.class, "script_obj.gif"));
		registerImageDescriptor(IMG_FOLDER,
				ImageDescriptor.createFromFile(TypeScriptImagesRegistry.class, "fldr_obj.png"));		
		registerImageDescriptor(IMG_CLASS,
				ImageDescriptor.createFromFile(TypeScriptImagesRegistry.class, "class_obj.gif"));
		registerImageDescriptor(IMG_ALIAS,
				ImageDescriptor.createFromFile(TypeScriptImagesRegistry.class, "alias_obj.gif"));
		registerImageDescriptor(IMG_INTERFACE,
				ImageDescriptor.createFromFile(TypeScriptImagesRegistry.class, "int_obj.png"));
		registerImageDescriptor(IMG_ENUM_DEFAULT,
				ImageDescriptor.createFromFile(TypeScriptImagesRegistry.class, "enum_default_obj.gif"));
		registerImageDescriptor(IMG_ENUM_PRIVATE,
				ImageDescriptor.createFromFile(TypeScriptImagesRegistry.class, "enum_private_obj.gif"));
		registerImageDescriptor(IMG_FIELD_DEFAULT,
				ImageDescriptor.createFromFile(TypeScriptImagesRegistry.class, "field_default_obj.gif"));
		registerImageDescriptor(IMG_FIELD_PRIVATE,
				ImageDescriptor.createFromFile(TypeScriptImagesRegistry.class, "field_private_obj.gif"));
		registerImageDescriptor(IMG_FIELD_PUBLIC,
				ImageDescriptor.createFromFile(TypeScriptImagesRegistry.class, "field_public_obj.gif"));
		registerImageDescriptor(IMG_METHOD_DEFAULT,
				ImageDescriptor.createFromFile(TypeScriptImagesRegistry.class, "methdef_obj.gif"));
		registerImageDescriptor(IMG_METHOD_PRIVATE,
				ImageDescriptor.createFromFile(TypeScriptImagesRegistry.class, "methpri_obj.gif"));
		registerImageDescriptor(IMG_METHOD_PUBLIC,
				ImageDescriptor.createFromFile(TypeScriptImagesRegistry.class, "methpub_obj.gif"));
		registerImageDescriptor(IMG_TYPE_DEFAULT,
				ImageDescriptor.createFromFile(TypeScriptImagesRegistry.class, "type_obj.gif"));
		registerImageDescriptor(IMG_TYPE_PRIVATE,
				ImageDescriptor.createFromFile(TypeScriptImagesRegistry.class, "type_private_obj.gif"));
		registerImageDescriptor(IMG_TYPE_PUBLIC,
				ImageDescriptor.createFromFile(TypeScriptImagesRegistry.class, "type_public_obj.gif"));
	}

	/**
	 * Returns the image from the image registry with the given key.
	 * 
	 * @param key
	 *            of the image
	 * @return the image from the image registry with the given key.
	 */
	public static Image getImage(String key) {
		ImageRegistry imageRegistry = JFaceResources.getImageRegistry();
		return imageRegistry.get(key);
	}

	/**
	 * Returns the image descriptor from the image registry with the given key.
	 * 
	 * @param key
	 *            of the image
	 * @return the image descriptor from the image registry with the given key.
	 */
	public static ImageDescriptor getImageDescriptor(String key) {
		ImageRegistry imageRegistry = JFaceResources.getImageRegistry();
		return imageRegistry.getDescriptor(key);
	}

	private static void registerImageDescriptor(String key, ImageDescriptor descriptor) {
		ImageRegistry imageRegistry = JFaceResources.getImageRegistry();
		imageRegistry.put(key, descriptor);
	}

	public static Image getImage(IKindProvider entry) {
		return getTypeScriptImage(entry.getKind(), entry.getKindModifiers(), null);
	}

	public static Image getTypeScriptImage(String kind, String kindModifiers, String containerKind) {
		String imageKey = getImageKey(kind, kindModifiers, containerKind);
		if (imageKey != null) {
			return getImage(imageKey);
		}
		return null;
	}

	public static ImageDescriptor getTypeScriptImageDescriptor(String kind, String kindModifiers,
			String containerKind) {
		String imageKey = getImageKey(kind, kindModifiers, containerKind);
		if (imageKey != null) {
			return getImageDescriptor(imageKey);
		}
		return null;
	}

	private static String getImageKey(String kind, String kindModifiers, String containerKind) {
		TypeScriptKind tsKind = TypeScriptKind.getKind(kind);
		if (tsKind == null) {
			return null;
		}
		List<TypeScriptKind> parts = getParts(kindModifiers);
		boolean isInner = !StringUtils.isEmpty(containerKind);
		boolean isStatic = parts.contains(TypeScriptKind.STATIC);
		String imageKey = null;

		switch (tsKind) {
		case KEYWORD:
			imageKey = IMG_KEYWORD;
			break;
		case ALIAS:
			imageKey = IMG_ALIAS;
			break;
		case MODULE:
			imageKey = IMG_PACKAGE;
			break;
		case SCRIPT:
			imageKey = IMG_SCRIPT;
			break;
		case DIRECTORY:
			imageKey = IMG_FOLDER;
			break;			
		case ENUM:
			imageKey = getKey(parts, IMG_ENUM_DEFAULT, IMG_ENUM_PRIVATE, null);
			break;
		case VAR:
		case PROPERTY:
		case LET:
			imageKey = getKey(parts, IMG_FIELD_DEFAULT, IMG_FIELD_PRIVATE, IMG_FIELD_PUBLIC);
			break;
		case TYPE:
			imageKey = getKey(parts, IMG_TYPE_DEFAULT, IMG_TYPE_PRIVATE, IMG_TYPE_PUBLIC);
			break;
		case METHOD:
		case FUNCTION:
			imageKey = getKey(parts, IMG_METHOD_DEFAULT, IMG_METHOD_PRIVATE, IMG_METHOD_PUBLIC);
			break;
		case CLASS:
			if (!isInner) {
				imageKey = IMG_CLASS;
			}
			break;
		case INTERFACE:
			if (!isInner) {
				imageKey = IMG_INTERFACE;
			}
			break;
		default:
		}
		return imageKey;
	}

	private static List<TypeScriptKind> getParts(String kindModifiers) {
		if (StringUtils.isEmpty(kindModifiers)) {
			return Collections.emptyList();
		}
		String[] parts = kindModifiers.split(",");
		List<TypeScriptKind> kinds = new ArrayList<TypeScriptKind>();
		for (int i = 0; i < parts.length; i++) {
			TypeScriptKind tsKind = TypeScriptKind.getKind(parts[i]);
			if (tsKind != null) {
				kinds.add(tsKind);
			}
		}
		return kinds;
	}

	private static String getKey(List<TypeScriptKind> parts, String defaultKey, String privateKey, String publicKey) {
		if (parts.contains(TypeScriptKind.PRIVATE)) {
			return privateKey;
		} else if (parts.contains(TypeScriptKind.PUBLIC)) {
			return publicKey;
		}
		return defaultKey;
	}
}
