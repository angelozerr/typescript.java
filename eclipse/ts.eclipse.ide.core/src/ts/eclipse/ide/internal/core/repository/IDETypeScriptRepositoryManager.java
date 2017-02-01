/**
 *  Copyright (c) 2015-2016 Angelo ZERR.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 *  Lorenzo Dalla Vecchia <lorenzo.dallavecchia@webratio.com> - loading of repositories from extension point
 */
package ts.eclipse.ide.internal.core.repository;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionDelta;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IRegistryChangeEvent;
import org.eclipse.core.runtime.IRegistryChangeListener;
import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Version;

import ts.eclipse.ide.core.TypeScriptCorePlugin;
import ts.eclipse.ide.core.repository.IIDETypeScriptRepositoryManager;
import ts.eclipse.ide.internal.core.Trace;
import ts.repository.ITypeScriptRepository;
import ts.repository.TypeScriptRepositoryManager;

public class IDETypeScriptRepositoryManager extends TypeScriptRepositoryManager
		implements IIDETypeScriptRepositoryManager, IRegistryChangeListener {

	public static final IIDETypeScriptRepositoryManager INSTANCE = new IDETypeScriptRepositoryManager();

	private static final String EXTENSION_TYPESCRIPT_REPOSITORIES = "typeScriptRepositories";

	private static final String PROJECT_LOC_TOKEN = "${project_loc:";
	private static final String WORKSPACE_LOC_TOKEN = "${workspace_loc:";
	private static final String END_TOKEN = "}";

	private boolean extensionRepositoriesLoaded;
	private boolean registryListenerIntialized;
	private final Map<File, ITypeScriptRepository> repositoriesByBaseDir;

	public IDETypeScriptRepositoryManager() {
		super();
		this.extensionRepositoriesLoaded = false;
		this.registryListenerIntialized = false;
		this.repositoriesByBaseDir = new HashMap<File, ITypeScriptRepository>();
	}

	@Override
	public String generateFileName(IResource resource, IProject project) {
		if (resource.getProject().equals(project)) {
			return new StringBuilder(PROJECT_LOC_TOKEN).append(resource.getProjectRelativePath().toString())
					.append(END_TOKEN).toString();
		}
		return new StringBuilder(WORKSPACE_LOC_TOKEN).append(resource.getFullPath().toString()).append(END_TOKEN)
				.toString();
	}

	@Override
	public IPath getPath(String path, IProject project) {
		if (path.startsWith(PROJECT_LOC_TOKEN)) {
			// ${project_loc:node_modules/typescript
			String projectPath = path.substring(PROJECT_LOC_TOKEN.length(),
					path.endsWith(END_TOKEN) ? path.length() - 1 : path.length());
			return project.getLocation().append(projectPath);
		} else if (path.startsWith(WORKSPACE_LOC_TOKEN)) {
			String wsPath = path.substring(WORKSPACE_LOC_TOKEN.length(),
					path.endsWith(END_TOKEN) ? path.length() - 1 : path.length());
			return ResourcesPlugin.getWorkspace().getRoot().getLocation().append(wsPath);
		}
		return null;
	}

	@Override
	public IResource getResource(String path, IProject project) {
		IPath location = getPath(path, project);
		if (location == null) {
			return null;
		}
		IContainer container = ResourcesPlugin.getWorkspace().getRoot().getContainerForLocation(location);
		if (container.exists()) {
			return container;
		}
		IFile file = ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(location);
		return file.exists() ? file : null;
	}

	@Override
	public ITypeScriptRepository getDefaultRepository() {
		loadExtensionRepositories();
		return super.getDefaultRepository();
	}

	@Override
	public ITypeScriptRepository getRepository(String name) {
		loadExtensionRepositories();
		return super.getRepository(name);
	}

	@Override
	public ITypeScriptRepository[] getRepositories() {
		loadExtensionRepositories();
		return super.getRepositories();
	}

	private synchronized void loadExtensionRepositories() {
		if (extensionRepositoriesLoaded)
			return;

		// Immediately set the flag, as to ensure that this method is never
		// called twice
		extensionRepositoriesLoaded = true;

		Trace.trace(Trace.EXTENSION_POINT, "->- Loading .typeScriptRepositories extension point ->-");

		IExtensionRegistry registry = Platform.getExtensionRegistry();
		IConfigurationElement[] cf = registry.getConfigurationElementsFor(TypeScriptCorePlugin.PLUGIN_ID,
				EXTENSION_TYPESCRIPT_REPOSITORIES);
		addExtensionRepositories(cf);
		resetDefaultRepository();
		addRegistryListenerIfNeeded();

		Trace.trace(Trace.EXTENSION_POINT, "-<- Done loading .typeScriptRepositories extension point -<-");
	}

	@Override
	public void registryChanged(final IRegistryChangeEvent event) {
		IExtensionDelta[] deltas = event.getExtensionDeltas(TypeScriptCorePlugin.PLUGIN_ID,
				EXTENSION_TYPESCRIPT_REPOSITORIES);
		if (deltas != null) {
			synchronized (this) {
				for (IExtensionDelta delta : deltas) {
					IConfigurationElement[] cf = delta.getExtension().getConfigurationElements();
					if (delta.getKind() == IExtensionDelta.ADDED) {
						addExtensionRepositories(cf);
					} else {
						removeExtensionRepositories(cf);
					}
				}
			}
		}
	}

	private void addExtensionRepositories(IConfigurationElement[] cf) {
		for (IConfigurationElement ce : cf) {
			try {
				File baseDir = computeActualBaseDir(ce);
				ITypeScriptRepository repository = createRepository(baseDir);
				synchronized (repositoriesByBaseDir) {
					repositoriesByBaseDir.put(baseDir, repository);
				}
				Trace.trace(Trace.EXTENSION_POINT, "  Loaded typeScriptRepositories: " + baseDir);
			} catch (Throwable t) {
				Trace.trace(Trace.SEVERE, "  Error while loading typeScriptRepositories", t);
			}
		}
	}

	private void removeExtensionRepositories(IConfigurationElement[] cf) {
		for (IConfigurationElement ce : cf) {
			try {
				File baseDir = computeActualBaseDir(ce);
				ITypeScriptRepository repository;
				synchronized (repositoriesByBaseDir) {
					repository = repositoriesByBaseDir.remove(baseDir);
				}
				if (repository != null) {
					ITypeScriptRepository removedRepository = removeRepository(repository.getName());
					if (removedRepository != repository) {
						Trace.trace(Trace.EXTENSION_POINT, "Unloaded typeScriptRepositories: " + baseDir);
					}
				}
			} catch (Throwable t) {
				Trace.trace(Trace.SEVERE, "Error while unloading typeScriptRepositories", t);
			}
		}
	}

	private static File computeActualBaseDir(IConfigurationElement ce) throws IOException {
		String bundleId = ce.getNamespaceIdentifier();
		File bundleDir = FileLocator.getBundleFile(Platform.getBundle(bundleId));
		if (!bundleDir.isDirectory()) {
			throw new RuntimeException("Bundle location " + bundleDir
					+ " cannot contribute a TypeScript repository because it is not a directory");
		}
		return new File(bundleDir, ce.getAttribute("baseDir"));
	}

	private void resetDefaultRepository() {

		// Sort available repositories by version in decreasing order
		List<ITypeScriptRepository> repositories = new ArrayList<ITypeScriptRepository>(
				Arrays.asList(super.getRepositories()));
		Collections.sort(repositories, new Comparator<ITypeScriptRepository>() {

			@Override
			public int compare(ITypeScriptRepository repo1, ITypeScriptRepository repo2) {
				Version v1 = extractVerion(repo1);
				Version v2 = extractVerion(repo2);
				return v2.compareTo(v1);
			}

			private Version extractVerion(ITypeScriptRepository repo) {
				try {
					return Version.parseVersion(repo.getTypesScriptVersion());
				} catch (IllegalArgumentException e) {
					return Version.emptyVersion;
				}
			}
		});

		// Reset the the default repository to the newest one available
		if (repositories.isEmpty()) {
			setDefaultRepository(null);
		} else {
			setDefaultRepository(repositories.get(0)); // first = newest
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
