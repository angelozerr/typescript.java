package ts.eclipse.ide.internal.core.resources.buildpath;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonObject.Member;

import ts.eclipse.ide.core.resources.buildpath.ITypeScriptBuildPath;
import ts.eclipse.ide.core.resources.buildpath.ITypeScriptBuildPathEntry;
import ts.utils.FileUtils;

public class TypeScriptBuildPath implements ITypeScriptBuildPath {

	private final IProject project;
	private List<IContainer> containers;
	private final List<ITypeScriptBuildPathEntry> entries;

	public TypeScriptBuildPath(IProject project) {
		this.project = project;
		this.entries = new ArrayList<ITypeScriptBuildPathEntry>();
		this.containers = null;
	}

	@Override
	public List<IContainer> getContainers() {
		if (containers == null) {
			containers = buildContainers(entries, project);
		}
		return containers;
	}

	private List<IContainer> buildContainers(List<ITypeScriptBuildPathEntry> entries, IProject project) {
		List<IContainer> containers = new ArrayList<IContainer>(entries.size());
		for (ITypeScriptBuildPathEntry entry : entries) {
			if ("/".equals(entry.getPath().toString())) {
				containers.add(project);
			} else {
				containers.add(project.getFolder(entry.getPath()));
			}
		}
		return containers;
	}

	@Override
	public List<ITypeScriptBuildPathEntry> getEntries() {
		return entries;
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

	public void addEntry(ITypeScriptBuildPathEntry entry) {
		if (!entries.contains(entry)) {
			entries.add(entry);
			this.containers = null;
		}
	}

	public void removeEntry(ITypeScriptBuildPathEntry entry) {
		entries.remove(entry);
		this.containers = null;
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
			savePatterns("includes", entry.getInclusionPatterns(), writer);
			writer.append(",");
			savePatterns("excludes", entry.getExclusionPatterns(), writer);
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

	private void savePatterns(String name, IPath[] patterns, Writer writer) throws IOException {
		int i = 0;
		addJsonFieldName(writer, name);
		writer.append("\"");
		if (patterns != null) {
			for (IPath pattern : patterns) {
				if (i > 0) {
					writer.append(",");
				}
				writer.append(pattern.toString());
				i++;
			}
		}
		writer.append("\"");
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
}
