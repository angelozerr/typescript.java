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
package ts.eclipse.ide.internal.core;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;

import ts.eclipse.ide.core.resources.IIDETypeScriptProject;
import ts.eclipse.ide.core.resources.buildpath.ITsconfigBuildPath;
import ts.eclipse.ide.core.utils.TypeScriptResourceUtil;

/**
 * TypeScript nature tester.
 *
 */
public class TypeScriptNatureTester extends PropertyTester {

	private static final String IS_TYPESCRIPT_PROJECT_PROPERTY = "isTypeScriptProject";
	private static final String IS_TYPESCRIPT_RESOURCE_PROPERTY = "isTypeScriptResource";
	private static final String HAS_TYPESCRIPT_BUILDER_PROPERTY = "hasTypeScriptBuilder";
	private static final String CAN_ADD_TO_BUILDPATH_PROPERTY = "canAddToBuildPath";
	private static final String CAN_REMOVE_TO_BUILDPATH_PROPERTY = "canRemoveToBuildPath";
	private static final String CAN_RUN_COMPILE_PROPERTY = "canRunCompile";

	public TypeScriptNatureTester() {
		// Default constructor is required for property tester
	}

	/**
	 * Tests if the receiver object is a project is a TypeScript project
	 * 
	 * @return true if the receiver object is a Project that has a nature that
	 *         is treated as TypeScript nature, otherwise false is returned
	 */
	@Override
	public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
		if (IS_TYPESCRIPT_PROJECT_PROPERTY.equals(property)) {
			return testIsTypeScriptProject(receiver);
		} else if (IS_TYPESCRIPT_RESOURCE_PROPERTY.equals(property)) {
			return testIsTypeScriptResource(receiver);
		} else if (HAS_TYPESCRIPT_BUILDER_PROPERTY.equals(property)) {
			return testHasTypeScriptBuilder(receiver);
		} else if (CAN_ADD_TO_BUILDPATH_PROPERTY.equals(property)) {
			return testCanAddToBuildPath(receiver);
		} else if (CAN_REMOVE_TO_BUILDPATH_PROPERTY.equals(property)) {
			return testCanRemoveToBuildPath(receiver);
		} else if (CAN_RUN_COMPILE_PROPERTY.equals(property)) {
			return testCanRunCompile(receiver);
		}
		return false;
	}

	private boolean testIsTypeScriptProject(Object receiver) {
		if (receiver instanceof IAdaptable) {
			IProject project = (IProject) ((IAdaptable) receiver).getAdapter(IProject.class);
			if (project != null) {
				return TypeScriptResourceUtil.isTypeScriptProject(project);
			}
		}
		return false;
	}

	private boolean testIsTypeScriptResource(Object receiver) {
		if (receiver instanceof IAdaptable) {
			IResource resource = (IResource) ((IAdaptable) receiver).getAdapter(IResource.class);
			if (resource != null) {
				return TypeScriptResourceUtil.isTypeScriptProject(resource.getProject());
			}
		}
		return false;
	}

	private boolean testHasTypeScriptBuilder(Object receiver) {
		if (receiver instanceof IAdaptable) {
			IProject project = (IProject) ((IAdaptable) receiver).getAdapter(IProject.class);
			if (project != null) {
				return TypeScriptResourceUtil.hasTypeScriptBuilder(project);
			}
		}
		return false;
	}

	private boolean testCanAddToBuildPath(Object receiver) {
		if (receiver instanceof IAdaptable) {
			IResource resource = (IResource) ((IAdaptable) receiver).getAdapter(IResource.class);
			if (resource != null) {
				switch (resource.getType()) {
				case IResource.PROJECT:
				case IResource.FOLDER:
					return true;
				case IResource.FILE:
					if (TypeScriptResourceUtil.isTsConfigFile(resource)) {
						try {
							IIDETypeScriptProject tsProject = TypeScriptResourceUtil
									.getTypeScriptProject(resource.getProject());
							return !tsProject.getTypeScriptBuildPath().isInBuildPath((IFile) resource);

						} catch (CoreException e) {
						}
					}
				default:
					return false;
				}
			}
		}
		return false;
	}

	private boolean testCanRemoveToBuildPath(Object receiver) {
		IFile tsconfigFile = TypeScriptResourceUtil.getBuildPathContainer(receiver);
		if (tsconfigFile == null) {
			return false;
		}
		try {
			IIDETypeScriptProject tsProject = TypeScriptResourceUtil.getTypeScriptProject(tsconfigFile.getProject());
			return tsProject.getTypeScriptBuildPath().isInBuildPath(tsconfigFile);

		} catch (CoreException e) {
		}
		return true;
	}

	private boolean testCanRunCompile(Object receiver) {
		if (receiver instanceof ITsconfigBuildPath) {
			return true;
		}
		IFile tsconfigFile = TypeScriptResourceUtil.getBuildPathContainer(receiver);
		if (tsconfigFile != null) {
			return true;
		}
		return false;
	}
}
