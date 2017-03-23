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
package ts.internal.client.protocol;

import java.util.List;
import java.util.stream.Collectors;

import com.google.gson.JsonObject;

import ts.client.CommandNames;
import ts.client.external.ExternalFile;
import ts.client.external.ExternalProject;

/**
 * A request to open or update external project.
 * 
 * @see https://github.com/Microsoft/TypeScript/blob/master/src/server/protocol.ts
 */
public class OpenExternalProjectRequest extends Request<ExternalProject> {

	public OpenExternalProjectRequest(String projectFileName, List<String> rootFileNames) {
		super(CommandNames.OpenExternalProject.getName(),
				new ExternalProject(projectFileName, createExternalFileList(rootFileNames)));
	}

	private static List<ExternalFile> createExternalFileList(List<String> fileNames) {
		return fileNames.stream().map(ExternalFile::new).collect(Collectors.toList());
	}

	@Override
	public Response<Boolean> parseResponse(JsonObject json) {
		return GsonHelper.DEFAULT_GSON.fromJson(json, OpenExternalProjectResponse.class);
	}

}
