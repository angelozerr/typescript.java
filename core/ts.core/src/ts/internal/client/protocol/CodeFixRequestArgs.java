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
package ts.internal.client.protocol;

import java.util.List;

/**
 * Instances of this interface specify errorcodes on a specific location in a
 * sourcefile.
 * 
 * @see https://github.com/Microsoft/TypeScript/blob/master/src/server/protocol.ts
 *
 */
public class CodeFixRequestArgs extends FileRequestArgs {

	/**
	 * The line number for the request (1-based).
	 */
	private Integer startLine;

	/**
	 * The character offset (on the line) for the request (1-based).
	 */
	private Integer startOffset;

	/**
	 * Position (can be specified instead of line/offset pair)
	 */
	/* @internal */
	private Integer startPosition;

	/**
	 * The line number for the request (1-based).
	 */
	private Integer endLine;

	/**
	 * The character offset (on the line) for the request (1-based).
	 */
	private Integer endOffset;

	/**
	 * Position (can be specified instead of line/offset pair)
	 */
	/* @internal */
	private Integer endPosition;

	/**
	 * Errorcodes we want to get the fixes for.
	 */
	private List<Integer> errorCodes;

	public CodeFixRequestArgs(String file, int startLine, int startOffset, int endLine, int endOffset,
			List<Integer> errorCodes) {
		this(file, startLine, startOffset, endLine, endOffset, errorCodes, null, null, null);
	}

	private CodeFixRequestArgs(String file, Integer startLine, Integer startOffset, Integer endLine, Integer endOffset,
			List<Integer> errorCodes, Integer startPosition, Integer endPosition, String projectName) {
		super(file, projectName);
		this.startLine = startLine;
		this.startOffset = startOffset;
		this.endLine = endLine;
		this.endOffset = endOffset;
		this.startPosition = startPosition;
		this.endPosition = endPosition;
		this.errorCodes = errorCodes;
	}

	public Integer getStartLine() {
		return startLine;
	}

	public Integer getStartOffset() {
		return startOffset;
	}

	public Integer getStartPosition() {
		return startPosition;
	}

	public Integer getEndLine() {
		return endLine;
	}

	public Integer getEndOffset() {
		return endOffset;
	}

	public Integer getEndPosition() {
		return endPosition;
	}

	public List<Integer> getErrorCodes() {
		return errorCodes;
	}

}
