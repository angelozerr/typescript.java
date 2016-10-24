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
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonObject.Member;

import ts.eclipse.ide.core.resources.buildpath.ITsconfigBuildPath;
import ts.eclipse.ide.core.resources.buildpath.ITypeScriptBuildPath;
import ts.eclipse.ide.core.resources.buildpath.ITypeScriptBuildPathEntry;
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
	private List<ITsconfigBuildPath> tsconfigBuildPathList;
	private final List<ITypeScriptBuildPathEntry> entries;

	private static final ITsconfigBuildPath[] EMPTY_TSCONFIG_BUILD_PATH = new ITsconfigBuildPath[0];

	private static final Comparator<ITsconfigBuildPath> TSCONFIG_BUILD_PATH_COMPARATOR = new Comparator<ITsconfigBuildPath>() {

		@Override
		public int compare(ITsconfigBuildPath o1, ITsconfigBuildPath o2) {
			IFile c1 = o1.getTsconfigFile();
			IFile c2 = o2.getTsconfigFile();
			return Collator.getInstance().compare(c2.getProjectRelativePath().toString(),
					c1.getProjectRelativePath().toString());
		}
	};

	public TypeScriptBuildPath(IProject project) {
		this.project = project;
		this.entries = new ArrayList<ITypeScriptBuildPathEntry>();
		this.tsconfigBuildPathList = null;
	}

	@Override
	public ITsconfigBuildPath[] getTsconfigBuildPaths() {
		return getTsconfigBuildPathList().toArray(EMPTY_TSCONFIG_BUILD_PATH);
	}

	@Override
	public boolean hasRootContainers() {
		return getTsconfigBuildPathList().size() > 0;
	}

	private List<ITsconfigBuildPath> getTsconfigBuildPathList() {
		if (tsconfigBuildPathList == null) {
			tsconfigBuildPathList = buildTsconfigBuildPathList(entries, project);
		}
		return tsconfigBuildPathList;
	}

	private List<ITsconfigBuildPath> buildTsconfigBuildPathList(List<ITypeScriptBuildPathEntry> entries,
			IProject project) {
		List<ITsconfigBuildPath> containers = new ArrayList<ITsconfigBuildPath>(entries.size());
		String path = null;
		for (ITypeScriptBuildPathEntry entry : entries) {
			path = entry.getPath().toString();
			if (!StringUtils.isEmpty(path)) {
				containers.add(new TsconfigBuildPath(project.getFile(path)));
			}
		}
		Collections.sort(containers, TSCONFIG_BUILD_PATH_COMPARATOR);
		return containers;
	}

	public static ITypeScriptBuildPath load(IProject project, String json) {
		TypeScriptBuildPath buildPath = new TypeScriptBuildPath(project);
		JsonObject object = Json.parse(json).asObject();
		for (Member member : object) {
			IPath path = new Path(member.getName());
			path = toTsconfigFilePath(path, project);
			if (project.exists(path)) {
				TypeScriptBuildPathEntry entry = new TypeScriptBuildPathEntry(path);
				buildPath.addEntry(entry);
			}
		}
		return buildPath;
	}

	private static IPath toTsconfigFilePath(IPath path, IProject project) {
		if (path.isEmpty()) {
			return path.append(FileUtils.TSCONFIG_JSON);
		}
		if (project.exists(path)) {
			IResource resource = project.findMember(path);
			if (resource.getType() == IResource.FOLDER) {
				return path.append(FileUtils.TSCONFIG_JSON);
			}
		}
		return path;
	}

	@Override
	public void addEntry(ITypeScriptBuildPathEntry entry) {
		if (!entries.contains(entry)) {
			entries.add(entry);
			this.tsconfigBuildPathList = null;
		}
	}

	@Override
	public void addEntry(IFile tsconfigFile) {
		addEntry(createEntry(tsconfigFile));
	}

	@Override
	public void removeEntry(ITypeScriptBuildPathEntry entry) {
		entries.remove(entry);
		this.tsconfigBuildPathList = null;
	}

	@Override
	public void removeEntry(IFile tsconfigFile) {
		removeEntry(createEntry(tsconfigFile));
	}

	private ITypeScriptBuildPathEntry createEntry(IFile tsconfigFile) {
		return new TypeScriptBuildPathEntry(tsconfigFile.getProjectRelativePath());

	}

	@Override
	public boolean isInScope(IResource resource) {
		return findTsconfigBuildPath(resource) != null;
	}

	@Override
	public void clear() {
		entries.clear();
		this.tsconfigBuildPathList = null;
	}

	@Override
	public boolean isInBuildPath(IFile tsconfigFile) {
		return getTsconfigBuildPath(tsconfigFile) != null;
	}

	@Override
	public ITsconfigBuildPath findTsconfigBuildPath(IResource resource) {
		for (ITsconfigBuildPath tsconfigBuildPath : getTsconfigBuildPathList()) {
			IContainer container = tsconfigBuildPath.getTsconfigFile().getParent();
			if (container.getFullPath().isPrefixOf(resource.getFullPath())) {
				return tsconfigBuildPath;
			}
		}
		return null;
	}

	@Override
	public ITsconfigBuildPath getTsconfigBuildPath(IFile tsconfigFile) {
		for (ITsconfigBuildPath tsconfigBuildPath : getTsconfigBuildPathList()) {
			if (tsconfigBuildPath.getTsconfigFile().equals(tsconfigFile)) {
				return tsconfigBuildPath;
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
