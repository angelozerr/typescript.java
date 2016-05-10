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
package ts.eclipse.ide.internal.core.resources.buildpath;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonObject.Member;

import ts.eclipse.ide.core.resources.buildpath.ITypeScriptBuildPath;
import ts.eclipse.ide.core.resources.buildpath.ITypeScriptBuildPathEntry;
import ts.eclipse.ide.core.resources.buildpath.ITypeScriptRootContainer;
import ts.eclipse.ide.core.utils.TypeScriptResourceUtil;
import ts.eclipse.ide.internal.core.resources.IDETypeScriptProjectSettings;
import ts.utils.FileUtils;
import ts.utils.StringUtils;

/**
 * TypeScript build path implementation.
 *
 */
public class TypeScriptBuildPath implements ITypeScriptBuildPath {

	private final IProject project;
	private List<ITypeScriptRootContainer> tsContainers;
	private final List<ITypeScriptBuildPathEntry> entries;

	private static final ITypeScriptRootContainer[] EMPTY_CONTAINER = new ITypeScriptRootContainer[0];

	private static final Comparator<ITypeScriptRootContainer> CONTAINER_COMPARATOR = new Comparator<ITypeScriptRootContainer>() {

		@Override
		public int compare(ITypeScriptRootContainer o1, ITypeScriptRootContainer o2) {
			IContainer c1 = o1.getContainer();
			IContainer c2 = o2.getContainer();
			return Collator.getInstance().compare(c2.getProjectRelativePath().toString(),
					c1.getProjectRelativePath().toString());
		}
	};

	public TypeScriptBuildPath(IProject project) {
		this.project = project;
		this.entries = new ArrayList<ITypeScriptBuildPathEntry>();
		this.tsContainers = null;
	}

	@Override
	public ITypeScriptRootContainer[] getRootContainers() {
		return getRootContainersList().toArray(EMPTY_CONTAINER);
	}

	@Override
	public boolean hasRootContainers() {
		return getRootContainersList().size() > 0;
	}

	private List<ITypeScriptRootContainer> getRootContainersList() {
		if (tsContainers == null) {
			tsContainers = buildContainers(entries, project);
		}
		return tsContainers;
	}

	private List<ITypeScriptRootContainer> buildContainers(List<ITypeScriptBuildPathEntry> entries, IProject project) {
		List<ITypeScriptRootContainer> containers = new ArrayList<ITypeScriptRootContainer>(entries.size());
		String path = null;
		for (ITypeScriptBuildPathEntry entry : entries) {
			path = entry.getPath().toString();
			if (StringUtils.isEmpty(path)) {
				containers.add(new TypeScriptRootContainer(project));
			} else {
				containers.add(new TypeScriptRootContainer(project.getFolder(path)));
			}
		}
		Collections.sort(containers, CONTAINER_COMPARATOR);
		return containers;
	}

	public static ITypeScriptBuildPath load(IProject project, String json) {
		TypeScriptBuildPath buildPath = new TypeScriptBuildPath(project);
		JsonObject object = Json.parse(json).asObject();
		for (Member member : object) {
			IPath path = new Path(member.getName());
			if (project.exists(path.append(FileUtils.TSCONFIG_JSON))) {
				TypeScriptBuildPathEntry entry = new TypeScriptBuildPathEntry(path);
				buildPath.addEntry(entry);
			}
		}
		return buildPath;
	}

	@Override
	public void addEntry(ITypeScriptBuildPathEntry entry) {
		if (!entries.contains(entry)) {
			entries.add(entry);
			this.tsContainers = null;
		}
	}

	@Override
	public void addEntry(IResource resource) {
		addEntry(createEntry(resource));
	}

	@Override
	public void removeEntry(ITypeScriptBuildPathEntry entry) {
		entries.remove(entry);
		this.tsContainers = null;
	}

	@Override
	public void removeEntry(IResource resource) {
		removeEntry(createEntry(resource));
	}

	private ITypeScriptBuildPathEntry createEntry(IResource resource) {
		if (resource.getType() == IResource.FILE) {
			return new TypeScriptBuildPathEntry(resource.getParent().getProjectRelativePath());
		}
		return new TypeScriptBuildPathEntry(resource.getProjectRelativePath());
	}

	@Override
	public boolean isInScope(IResource resource) {
		return findRootContainer(resource) != null;
	}

	@Override
	public void clear() {
		entries.clear();
		this.tsContainers = null;
	}

	@Override
	public boolean isRootContainer(IResource resource) {
		if (!(resource.getType() == IResource.PROJECT || resource.getType() == IResource.FOLDER)) {
			return false;
		}
		return getRootContainer((IContainer) resource) != null;
	}

	@Override
	public ITypeScriptRootContainer findRootContainer(IResource resource) {
		for (ITypeScriptRootContainer tsContainer : getRootContainersList()) {
			IContainer container = tsContainer.getContainer();
			if (container.getFullPath().isPrefixOf(resource.getFullPath())) {
				return tsContainer;
			}
		}
		return null;
	}

	@Override
	public ITypeScriptRootContainer getRootContainer(IContainer resource) {
		for (ITypeScriptRootContainer tsContainer : getRootContainersList()) {
			IContainer container = tsContainer.getContainer();
			if (container.equals(resource)) {
				return tsContainer;
			}
		}
		return null;
	}

	public void save(Writer writer) throws IOException {
		startJsonObject(writer);
		int i = 0;
		for (ITypeScriptBuildPathEntry entry : entries) {
			if (i > 0) {
				writer.append(",");
			}
			addJsonFieldName(writer, entry.getPath().toString());
			startJsonObject(writer);
			endJsonObject(writer);
			i++;
		}
		endJsonObject(writer);
	}

	private void addJsonFieldName(Writer writer, String name) throws IOException {
		writer.append("\"");
		writer.append(name);
		writer.append("\"");
		writer.append(":");
	}

	private void endJsonObject(Writer writer) throws IOException {
		writer.append("}");
	}

	private void startJsonObject(Writer writer) throws IOException {
		writer.append("{");
	}

	@Override
	public String toString() {
		StringWriter result = new StringWriter();
		try {
			save(result);
		} catch (IOException e) {
			// should never done
		}
		return result.toString();
	}

	@Override
	public ITypeScriptBuildPath copy() {
		TypeScriptBuildPath buildPath = new TypeScriptBuildPath(project);
		for (ITypeScriptBuildPathEntry entry : entries) {
			buildPath.addEntry(entry);
		}
		return buildPath;
	}

	@Override
	public void save() {
		getProjectSettings().updateBuildPath(this);
	}

	private IDETypeScriptProjectSettings getProjectSettings() {
		try {
			return (IDETypeScriptProjectSettings) TypeScriptResourceUtil.getTypeScriptProject(project)
					.getProjectSettings();
		} catch (Exception e) {
			return null;
		}
	}

}
