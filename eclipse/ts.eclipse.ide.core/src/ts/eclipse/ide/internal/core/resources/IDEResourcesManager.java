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
package ts.eclipse.ide.internal.core.resources;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionDelta;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IRegistryChangeEvent;
import org.eclipse.core.runtime.IRegistryChangeListener;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.core.runtime.content.IContentTypeManager.ContentTypeChangeEvent;
import org.eclipse.core.runtime.content.IContentTypeManager.IContentTypeChangeListener;

import ts.client.ScriptKindName;
import ts.eclipse.ide.core.TypeScriptCorePlugin;
import ts.eclipse.ide.core.preferences.TypeScriptCorePreferenceConstants;
import ts.eclipse.ide.core.resources.IIDETypeScriptProject;
import ts.eclipse.ide.core.resources.ITypeScriptElementChangedListener;
import ts.eclipse.ide.core.resources.ITypeScriptResourceParticipant;
import ts.eclipse.ide.core.resources.UseSalsa;
import ts.eclipse.ide.core.resources.buildpath.ITypeScriptBuildPath;
import ts.eclipse.ide.core.utils.PreferencesHelper;
import ts.eclipse.ide.internal.core.Trace;
import ts.resources.ITypeScriptResourcesManagerDelegate;
import ts.utils.FileUtils;

public class IDEResourcesManager implements ITypeScriptResourcesManagerDelegate, IRegistryChangeListener {

	private static IDEResourcesManager instance = new IDEResourcesManager();
	private final List<ITypeScriptElementChangedListener> listeners;

	private boolean useJsAsJsx;

	private static final String EXTENSION_TYPESCRIPT_RESOURCE_PARTICIPANTS = "typeScriptResourceParticipants";
	private static final String CLASS_ATTR = "class";
	private boolean registryListenerIntialized;
	private boolean extensionResourceParticipantsLoaded;
	private List<ITypeScriptResourceParticipant> resourceParticipants;

	/**
	 * Contents Types IDS
	 */
	private static final String JS_CONTENT_TYPE_ID = "org.eclipse.wst.jsdt.core.jsSource";
	private static final String TS_CONTENT_TYPE_ID = "ts.eclipse.ide.core.tsSource";
	private static final String TSX_CONTENT_TYPE_ID = "ts.eclipse.ide.core.tsxSource";
	private static final String JSX_CONTENT_TYPE_ID = "ts.eclipse.ide.core.jsxSource";

	private IDEResourcesManager() {
		this.registryListenerIntialized = false;
		this.resourceParticipants = new ArrayList<>();
		this.listeners = new ArrayList<ITypeScriptElementChangedListener>();
		updateUseJsAsJsx(Platform.getContentTypeManager().getContentType(JSX_CONTENT_TYPE_ID));

		Platform.getContentTypeManager().addContentTypeChangeListener(new IContentTypeChangeListener() {

			@Override
			public void contentTypeChanged(ContentTypeChangeEvent event) {
				IContentType contentType = event.getContentType();
				if (contentType != null && JSX_CONTENT_TYPE_ID.equals(contentType.getId())) {
					updateUseJsAsJsx(contentType);
				}
			}
		});
	}

	private void updateUseJsAsJsx(IContentType jsxContentType) {
		String[] fileSpecs = jsxContentType.getFileSpecs(IContentType.FILE_EXTENSION_SPEC);
		useJsAsJsx = fileSpecs != null && Arrays.asList(fileSpecs).contains(FileUtils.JS_EXTENSION);
	}

	public static IDEResourcesManager getInstance() {
		return instance;
	}

	@Override
	public IDETypeScriptProject getTypeScriptProject(Object obj, boolean force) throws IOException {
		if (obj instanceof IProject) {
			IProject project = (IProject) obj;
			if (project.getLocation() == null) {
				return null;
			}
			try {
				if (force) {
					// Dispose TypeScript project if exists
					IDETypeScriptProject tsProject = getTypeScriptProject(project);
					if (tsProject != null) {
						tsProject.dispose();
					}
				}
				IDETypeScriptProject tsProject = getTypeScriptProject(project);
				if (tsProject == null) {
					tsProject = create(project);
				}
				return tsProject;
			} catch (Exception ex) {
				Trace.trace(Trace.SEVERE,
						"Error while creating TypeScript project [" + project.getName() + "]: " + ex.getMessage(), ex);
			}
		}
		return null;
	}

	private synchronized IDETypeScriptProject create(IProject project) throws CoreException, IOException {
		IDETypeScriptProject tsProject = getTypeScriptProject(project);
		if (tsProject != null) {
			return tsProject;
		}
		tsProject = new IDETypeScriptProject(project);
		try {
			tsProject.load();
		} catch (IOException e) {
			Trace.trace(Trace.SEVERE, "Error while loading TypeScript project", e);
			throw e;
		}
		return tsProject;
	}

	/**
	 * Returns true if the given project contains one or several "tsconfig.json"
	 * file(s) false otherwise.
	 * 
	 * To have a very good performance, "tsconfig.json" is not searched by
	 * scanning the whole files of the project but it checks if "tsconfig.json"
	 * exists in several folders ('/tsconfig.json' or '/src/tsconfig.json).
	 * Those folders can be customized with preferences buildpath
	 * {@link TypeScriptCorePreferenceConstants#TYPESCRIPT_BUILD_PATH}.
	 * 
	 * @param project
	 *            Eclipse project.
	 * @return true if the given project contains one or several "tsconfig.json"
	 *         file(s) false otherwise.
	 */
	public boolean isTypeScriptProject(IProject project) {
		// check that TypeScript project have build path.
		try {
			IDETypeScriptProject tsProject = getTypeScriptProject(project, false);
			return tsProject != null && tsProject.getTypeScriptBuildPath().hasRootContainers();
		} catch (Exception e) {
			Trace.trace(Trace.SEVERE, "Error while getting TypeScript project", e);
		}
		return false;
	}

	public boolean hasSalsaNature(IProject project) {
		UseSalsa useSalsa = PreferencesHelper.getUseSalsa();
		switch (useSalsa) {
		case Never:
			return false;
		case EveryTime:
			return true;
		case WhenNoJSDTNature:
			try {
				return !project.hasNature("org.eclipse.wst.jsdt.core.jsNature");
			} catch (CoreException e) {
				return false;
			}
		}
		return false;
	}

	private IDETypeScriptProject getTypeScriptProject(IProject project) throws CoreException {
		return IDETypeScriptProject.getTypeScriptProject(project);
	}

	protected String getExtension(Object fileObject) {
		if (fileObject instanceof IFile) {
			return ((IFile) fileObject).getFileExtension();
		} else if (fileObject instanceof File) {
			return FileUtils.getFileExtension(((File) fileObject).getName());
		} else if (fileObject instanceof String) {
			return FileUtils.getFileExtension((String) fileObject);
		}
		return null;
	}

	protected String getFileName(Object fileObject) {
		if (fileObject instanceof IFile) {
			return ((IFile) fileObject).getName();
		} else if (fileObject instanceof File) {
			return ((File) fileObject).getName();
		} else if (fileObject instanceof String) {
			return (String) fileObject;
		}
		return null;
	}

	@Override
	public boolean isJsFile(Object fileObject) {
		ScriptKindName kind = getScriptKind(fileObject);
		return kind != null && (ScriptKindName.JS.equals(kind));
	}

	@Override
	public boolean isJsxFile(Object fileObject) {
		ScriptKindName kind = getScriptKind(fileObject);
		return kind != null && (ScriptKindName.JSX.equals(kind));
	}

	@Override
	public boolean isTsFile(Object fileObject) {
		ScriptKindName kind = getScriptKind(fileObject);
		return kind != null && (ScriptKindName.TS.equals(kind));
	}

	@Override
	public boolean isTsxFile(Object fileObject) {
		ScriptKindName kind = getScriptKind(fileObject);
		return kind != null && (ScriptKindName.TSX.equals(kind));
	}

	@Override
	public boolean isTsOrTsxFile(Object fileObject) {
		ScriptKindName kind = getScriptKind(fileObject);
		return kind != null && (ScriptKindName.TS.equals(kind) || ScriptKindName.TSX.equals(kind));
	}

	@Override
	public boolean isDefinitionTsFile(Object fileObject) {
		String name = getFileName(fileObject);
		name = name != null ? name.toLowerCase() : null;
		return name != null && (name.endsWith(FileUtils.DEFINITION_TS_EXTENSION));
	}

	@Override
	public boolean isTsOrTsxOrJsxFile(Object fileObject) {
		ScriptKindName kind = getScriptKind(fileObject);
		return kind != null && (ScriptKindName.TS.equals(kind) || ScriptKindName.TSX.equals(kind)
				|| ScriptKindName.JSX.equals(kind));
	}

	@Override
	public boolean isTsxOrJsxFile(Object fileObject) {
		ScriptKindName kind = getScriptKind(fileObject);
		return kind != null && (ScriptKindName.TSX.equals(kind) || ScriptKindName.JSX.equals(kind));
	}

	/**
	 * Returns the {@link ScriptKindName} from the given file object and null
	 * otherwise.
	 * 
	 * @param fileObject
	 * @return the {@link ScriptKindName} from the given file object and null
	 *         otherwise.
	 */
	public ScriptKindName getScriptKind(Object fileObject) {
		String ext = getExtension(fileObject);
		if (ext != null) {
			ext = ext.toLowerCase();
			if (FileUtils.TS_EXTENSION.equals(ext)) {
				return ScriptKindName.TS;
			} else if (FileUtils.TSX_EXTENSION.equals(ext)) {
				return ScriptKindName.TSX;
			} else if (FileUtils.JSX_EXTENSION.equals(ext)) {
				return ScriptKindName.JSX;
			} else if (FileUtils.JS_EXTENSION.equals(ext)) {
				return useJsAsJsx ? ScriptKindName.JSX : ScriptKindName.JS;
			}
		}
		if (fileObject instanceof IFile) {
			try {
				IContentType contentType = ((IFile) fileObject).getContentDescription().getContentType();
				if (contentType != null) {
					String contentTypeId = contentType.getId();
					if (TS_CONTENT_TYPE_ID.equals(contentTypeId)) {
						return ScriptKindName.TS;
					} else if (TSX_CONTENT_TYPE_ID.equals(contentTypeId)) {
						return ScriptKindName.TSX;
					} else if (JSX_CONTENT_TYPE_ID.equals(contentTypeId)) {
						return ScriptKindName.JSX;
					} else if (JS_CONTENT_TYPE_ID.equals(contentTypeId)) {
						return useJsAsJsx ? ScriptKindName.JSX : ScriptKindName.JS;
					}
				}
			} catch (Exception e) {
				return null;
			}
		}
		return null;
	}

	public boolean isJsOrJsMapFile(Object fileObject) {
		if (fileObject instanceof IFile) {
			return FileUtils.isJsOrJsMapFile(((IFile) fileObject).getName());
		} else if (fileObject instanceof File) {
			return FileUtils.isJsOrJsMapFile(((File) fileObject).getName());
		} else if (fileObject instanceof String) {
			return FileUtils.isJsOrJsMapFile((String) fileObject);
		}
		return false;
	}

	public boolean canConsumeTsserver(IProject project, Object fileObject) {
		if (!project.isAccessible()) {
			return false;
		}
		if (isJsFile(fileObject)) {
			return hasSalsaNature(project);
		}
		if (isTsOrTsxOrJsxFile(fileObject)) {
			return true;
		}
		// Use extension point
		loadExtensionResourceParticipants();
		for (ITypeScriptResourceParticipant participant : resourceParticipants) {
			if (participant.canConsumeTsserver(project, fileObject)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public String getTypeScriptFilename(Object fileObject) {
		if (fileObject instanceof IFile) {
			return FileUtils.getTypeScriptFilename(((IFile) fileObject).getName());
		} else if (fileObject instanceof File) {
			return FileUtils.getTypeScriptFilename(((File) fileObject).getName());
		} else if (fileObject instanceof String) {
			return FileUtils.getTypeScriptFilename((String) fileObject);
		}
		return null;
	}

	public void addTypeScriptElementChangedListener(ITypeScriptElementChangedListener listener) {
		synchronized (listeners) {
			if (!listeners.contains(listener)) {
				listeners.add(listener);
			}
		}
	}

	public void fireBuildPathChanged(IIDETypeScriptProject tsProject, ITypeScriptBuildPath oldBuildPath,
			ITypeScriptBuildPath newBuildPath) {
		synchronized (listeners) {
			for (ITypeScriptElementChangedListener listener : listeners) {
				listener.buildPathChanged(tsProject, oldBuildPath, newBuildPath);
			}
		}
	}

	public void fireTypeScriptVersionChanged(IIDETypeScriptProject tsProject, String oldVersion, String newVersion) {
		synchronized (listeners) {
			for (ITypeScriptElementChangedListener listener : listeners) {
				listener.typeScriptVersionChanged(tsProject, oldVersion, newVersion);
			}
		}
	}

	public void nodejsVersionChanged(IIDETypeScriptProject tsProject, String oldVersion, String newVersion) {
		synchronized (listeners) {
			for (ITypeScriptElementChangedListener listener : listeners) {
				listener.nodejsVersionChanged(tsProject, oldVersion, newVersion);
			}
		}
	}

	public void removeTypeScriptElementChangedListener(ITypeScriptElementChangedListener listener) {
		synchronized (listeners) {
			listeners.remove(listener);
		}
	}

	private synchronized void loadExtensionResourceParticipants() {
		if (extensionResourceParticipantsLoaded)
			return;
		// Immediately set the flag, as to ensure that this method is never
		// called twice
		extensionResourceParticipantsLoaded = true;

		Trace.trace(Trace.EXTENSION_POINT, "->- Loading .typeScriptResourceParticipants extension point ->-");

		IExtensionRegistry registry = Platform.getExtensionRegistry();
		IConfigurationElement[] cf = registry.getConfigurationElementsFor(TypeScriptCorePlugin.PLUGIN_ID,
				EXTENSION_TYPESCRIPT_RESOURCE_PARTICIPANTS);
		
		addExtensionResourceParticipants(cf);
		addRegistryListenerIfNeeded();

		Trace.trace(Trace.EXTENSION_POINT, "-<- Done loading .typeScriptResourceParticipants extension point -<-");
	}

	@Override
	public void registryChanged(final IRegistryChangeEvent event) {
		IExtensionDelta[] deltas = event.getExtensionDeltas(TypeScriptCorePlugin.PLUGIN_ID,
				EXTENSION_TYPESCRIPT_RESOURCE_PARTICIPANTS);
		if (deltas != null) {
			synchronized (this) {
				for (IExtensionDelta delta : deltas) {
					IConfigurationElement[] cf = delta.getExtension().getConfigurationElements();
					if (delta.getKind() == IExtensionDelta.ADDED) {
						addExtensionResourceParticipants(cf);
					} else {
						removeExtensionResourceParticipants(cf);
					}
				}
			}
		}
	}

	private void addExtensionResourceParticipants(IConfigurationElement[] cf) {
		for (IConfigurationElement ce : cf) {
			try {
				String className = ce.getAttribute(CLASS_ATTR);
				ITypeScriptResourceParticipant participant = (ITypeScriptResourceParticipant) ce
						.createExecutableExtension(CLASS_ATTR);
				synchronized (resourceParticipants) {
					resourceParticipants.add(participant);
				}
				Trace.trace(Trace.EXTENSION_POINT, "  Loaded typeScriptResourceParticipants: " + className);
			} catch (Throwable t) {
				Trace.trace(Trace.SEVERE, "  Error while loading typeScriptResourceParticipants", t);
			}
		}
	}

	private void removeExtensionResourceParticipants(IConfigurationElement[] cf) {
		for (IConfigurationElement ce : cf) {
			try {
				String className = ce.getAttribute(CLASS_ATTR);
				synchronized (resourceParticipants) {
					for (ITypeScriptResourceParticipant participant : resourceParticipants) {
						if (className.equals(participant.getClass().getName())) {
							resourceParticipants.remove(participant);
							Trace.trace(Trace.EXTENSION_POINT, "Unloaded typeScriptResourceParticipants: " + className);

							break;
						}
					}
				}
			} catch (Throwable t) {
				Trace.trace(Trace.SEVERE, "Error while unloading typeScriptResourceParticipants", t);
			}
		}
	}

	private void addRegistryListenerIfNeeded() {
		if (registryListenerIntialized)
			return;

		IExtensionRegistry registry = Platform.getExtensionRegistry();
		registry.addRegistryChangeListener(this, TypeScriptCorePlugin.PLUGIN_ID);
		registryListenerIntialized = true;
	}

	public void initialize() {

	}

	public void destroy() {
		Platform.getExtensionRegistry().removeRegistryChangeListener(this);
	}
}
