/**
 *  Copyright (c) 2015-2017 Angelo ZERR.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Lorenzo Dalla Vecchia <lorenzo.dallavecchia@webratio.com> - initial API and implementation
 */
package ts.client.external;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents an external project
 */
public class ExternalProject {
	/**
	 * Project name
	 */
	private String projectFileName;
	/**
	 * List of root files in project
	 */
	private List<ExternalFile> rootFiles;
	/**
	 * Compiler options for the project
	 */
	private ExternalProjectCompilerOptions options;

	public ExternalProject(String projectFileName, List<ExternalFile> rootFiles) {
		this.projectFileName = projectFileName;
		this.rootFiles = new ArrayList<ExternalFile>(rootFiles);
	}

	public String getProjectFileName() {
		return projectFileName;
	}

	public List<ExternalFile> getRootFiles() {
		return Collections.unmodifiableList(rootFiles);
	}

	public ExternalProjectCompilerOptions getOptions() {
		return options;
	}

}
