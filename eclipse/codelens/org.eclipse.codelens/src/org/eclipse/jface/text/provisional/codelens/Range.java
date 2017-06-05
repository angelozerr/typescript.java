package org.eclipse.jface.text.provisional.codelens;

public class Range {

	/**
	 * Line number on which the range starts (starts at 1).
	 */
	public final int startLineNumber;

	/**
	 * Column on which the range starts in line `startLineNumber` (starts at 1).
	 */
	public final int startColumn;

	
	public Range(int startLineNumber, int startColumn) {
		this.startLineNumber = startLineNumber;
		this.startColumn = startColumn;
	}
	/**
	 * Column on which the range starts in line `startLineNumber` (starts at 1).
	 */
	// readonly startColumn: number;
	/**
	 * Line number on which the range ends.
	 */
	// readonly endLineNumber: number;
	/**
	 * Column on which the range ends in line `endLineNumber`.
	 */
	// readonly endColumn: number;
}
