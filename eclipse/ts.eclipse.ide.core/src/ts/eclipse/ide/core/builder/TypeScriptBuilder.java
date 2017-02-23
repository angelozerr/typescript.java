/**
 *  Copyright (c) 2015-2017 Angelo ZERR.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package ts.eclipse.ide.core.builder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import ts.TypeScriptException;
import ts.client.CommandNames;
import ts.eclipse.ide.core.TypeScriptCorePlugin;
import ts.eclipse.ide.core.resources.IIDETypeScriptProject;
import ts.eclipse.ide.core.resources.buildpath.ITsconfigBuildPath;
import ts.eclipse.ide.core.resources.buildpath.ITypeScriptBuildPath;
import ts.eclipse.ide.core.resources.jsconfig.IDETsconfigJson;
import ts.eclipse.ide.core.utils.TypeScriptResourceUtil;
import ts.eclipse.ide.internal.core.Trace;

/**
 * Builder to transpile TypeScript files into JavaScript files and source map if
 * needed.
 *
 */
public class TypeScriptBuilder extends IncrementalProjectBuilder {

	public static final String ID = "ts.eclipse.ide.core.typeScriptBuilder";

	@Override
	protected IProject[] build(int kind, Map<String, String> args, final IProgressMonitor monitor)
			throws CoreException {
		IProject project = this.getProject();
		if (!TypeScriptResourceUtil.isTypeScriptProject(project)) {
			return null;
		}

		IIDETypeScriptProject tsProject = TypeScriptResourceUtil.getTypeScriptProject(project);
		if (kind == FULL_BUILD) {
			fullBuild(tsProject, monitor);
		} else {
			IResourceDelta delta = getDelta(getProject());
			if (delta == null) {
				fullBuild(tsProject, monitor);
			} else {
				incrementalBuild(tsProject, delta, monitor);
			}
		}
		return null;
	}

	private void fullBuild(IIDETypeScriptProject tsProject, IProgressMonitor monitor) throws CoreException {
		ITypeScriptBuildPath buildPath = tsProject.getTypeScriptBuildPath();
		ITsconfigBuildPath[] tsContainers = buildPath.getTsconfigBuildPaths();
		for (int i = 0; i < tsContainers.length; i++) {
			ITsconfigBuildPath tsContainer = tsContainers[i];
			/*
			 * try { IDETsconfigJson tsconfig = tsContainer.getTsconfig(); if
			 * (tsconfig == null || tsconfig.isCompileOnSave()) {
			 * tsProject.getCompiler().compile(tsconfig, null); } } catch
			 * (TypeScriptException e) { Trace.trace(Trace.SEVERE,
			 * "Error while tsc compilation", e); }
			 */
		}
	}

	private void incrementalBuild(IIDETypeScriptProject tsProject, IResourceDelta delta, IProgressMonitor monitor)
			throws CoreException {
		if (tsProject.canSupport(CommandNames.CompileOnSaveEmitFile)) {
			// compile with tsserver (since TypeScript 2.0.5)
			compileWithTsserver(tsProject, delta, monitor);
		} else {
			// compile with tsc (more slow than tsserver).
			compileWithTsc(tsProject, delta, monitor);
		}
	}

	/**
	 * Compile files with tsc.
	 * 
	 * @param tsProject
	 * @param delta
	 * @param monitor
	 * @throws CoreException
	 */
	private void compileWithTsc(IIDETypeScriptProject tsProject, IResourceDelta delta, IProgressMonitor monitor)
			throws CoreException {

		final ITypeScriptBuildPath buildPath = tsProject.getTypeScriptBuildPath();
		final Map<ITsconfigBuildPath, List<IFile>> tsFilesToCompile = new HashMap<ITsconfigBuildPath, List<IFile>>();
		final Map<ITsconfigBuildPath, List<IFile>> tsFilesToDelete = new HashMap<ITsconfigBuildPath, List<IFile>>();
		delta.accept(new IResourceDeltaVisitor() {

			@Override
			public boolean visit(IResourceDelta delta) throws CoreException {
				IResource resource = delta.getResource();
				if (resource == null) {
					return false;
				}
				switch (resource.getType()) {
				case IResource.ROOT:
					return true;
				case IResource.PROJECT:
					return TypeScriptResourceUtil.isTypeScriptProject((IProject) resource);
				case IResource.FOLDER:
					return buildPath.isInScope(resource);
				case IResource.FILE:
					int kind = delta.getKind();
					switch (kind) {
					case IResourceDelta.ADDED:
					case IResourceDelta.CHANGED:
						if (TypeScriptResourceUtil.isTsOrTsxFile(resource)
								&& !TypeScriptResourceUtil.isDefinitionTsFile(resource)) {
							addTsFile(buildPath, tsFilesToCompile, resource);
						}
						break;
					case IResourceDelta.REMOVED:
						if (TypeScriptResourceUtil.isTsOrTsxFile(resource)
								&& !TypeScriptResourceUtil.isDefinitionTsFile(resource)) {
							addTsFile(buildPath, tsFilesToDelete, resource);
						}
						break;
					}
					return false;
				}
				return false;
			}

			private void addTsFile(final ITypeScriptBuildPath buildPath,
					final Map<ITsconfigBuildPath, List<IFile>> tsFiles, IResource resource) {
				ITsconfigBuildPath tsContainer = buildPath.findTsconfigBuildPath(resource);
				if (tsContainer != null) {
					List<IFile> deltas = tsFiles.get(tsContainer);
					if (deltas == null) {
						deltas = new ArrayList<IFile>();
						tsFiles.put(tsContainer, deltas);
					}
					deltas.add((IFile) resource);
				}
			}
		});

		// Compile ts files *.ts
		for (Entry<ITsconfigBuildPath, List<IFile>> entries : tsFilesToCompile.entrySet()) {
			ITsconfigBuildPath tsContainer = entries.getKey();
			List<IFile> tsFiles = entries.getValue();
			try {
				// compile ts files
				IDETsconfigJson tsconfig = tsContainer.getTsconfig();
				if (!tsconfig.isBuildOnSave() && tsconfig.isCompileOnSave()
						&& tsProject.canSupport(CommandNames.CompileOnSaveEmitFile)) {
					// TypeScript >=2.0.5: compile is done with tsserver
					// compileWithTsserver(tsProject, tsFiles, tsconfig);
					compileWithTsc(tsProject, tsFiles, tsconfig);
				} else {
					// TypeScript < 2.0.5: compile is done with tsc which is not
					// very
					// performant.
					compileWithTsc(tsProject, tsFiles, tsconfig);
				}
				// validate ts files with tslint
				tsProject.getTslint().lint(tsconfig, tsFiles, tsProject.getProjectSettings());
			} catch (TypeScriptException e) {
				Trace.trace(Trace.SEVERE, "Error while tsc compilation", e);
			}
		}

		// Delete emitted files *.js, *.js.map
		for (Entry<ITsconfigBuildPath, List<IFile>> entries : tsFilesToDelete.entrySet()) {
			ITsconfigBuildPath tsContainer = entries.getKey();
			List<IFile> tsFiles = entries.getValue();
			IDETsconfigJson tsconfig = tsContainer.getTsconfig();
			for (IFile tsFile : tsFiles) {
				TypeScriptResourceUtil.deleteEmittedFiles(tsFile, tsconfig);
			}

		}
	}

	/**
	 * Compile the given ts files with tsc.
	 * 
	 * @param tsProject
	 * @param tsFiles
	 * @param tsconfig
	 * @throws TypeScriptException
	 * @throws CoreException
	 */
	private void compileWithTsc(IIDETypeScriptProject tsProject, List<IFile> tsFiles, IDETsconfigJson tsconfig)
			throws TypeScriptException, CoreException {
		tsProject.getCompiler().compile(tsconfig, tsFiles);
	}

	/**
	 * Compile files with tsserver (since TypeScript 2.0.5).
	 * 
	 * @param tsProject
	 * @param delta
	 * @param monitor
	 * @throws CoreException
	 */
	private void compileWithTsserver(IIDETypeScriptProject tsProject, IResourceDelta delta, IProgressMonitor monitor)
			throws CoreException {

		final List<IFile> updatedTsFiles = new ArrayList<>();
		final List<IFile> removedTsFiles = new ArrayList<>();
		delta.accept(new IResourceDeltaVisitor() {

			@Override
			public boolean visit(IResourceDelta delta) throws CoreException {
				IResource resource = delta.getResource();
				if (resource == null) {
					return false;
				}
				switch (resource.getType()) {
				case IResource.ROOT:
				case IResource.FOLDER:
					return true;
				case IResource.PROJECT:
					return TypeScriptResourceUtil.isTypeScriptProject((IProject) resource);
				case IResource.FILE:
					if (!TypeScriptResourceUtil.isTsOrTsxFile(resource)
							|| TypeScriptResourceUtil.isDefinitionTsFile(resource)) {
						return false;
					}
					int kind = delta.getKind();
					switch (kind) {
					case IResourceDelta.ADDED:
					case IResourceDelta.CHANGED:
						updatedTsFiles.add((IFile) resource);
						break;
					case IResourceDelta.REMOVED:
						removedTsFiles.add((IFile) resource);
						break;
					}
					return false;
				default:
					return false;
				}
			};
		});

		try {
			tsProject.compileWithTsserver(updatedTsFiles, removedTsFiles, monitor);
		} catch (TypeScriptException e) {
			throw new CoreException(new Status(IStatus.ERROR, TypeScriptCorePlugin.PLUGIN_ID,
					"Error while compiling with tsserver", e));
		}
	}
}
