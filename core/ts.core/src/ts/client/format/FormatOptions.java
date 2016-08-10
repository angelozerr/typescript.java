package ts.client.format;

import ts.utils.JsonHelper;

public class FormatOptions extends EditorOptions {

	public Boolean getInsertSpaceAfterCommaDelimiter() {
		return JsonHelper.getBoolean(this, "insertSpaceAfterCommaDelimiter");
	}

	public void setInsertSpaceAfterCommaDelimiter(Boolean insertSpaceAfterCommaDelimiter) {
		super.set("insertSpaceAfterCommaDelimiter", insertSpaceAfterCommaDelimiter);
	}

	public Boolean getInsertSpaceAfterSemicolonInForStatements() {
		return JsonHelper.getBoolean(this, "insertSpaceAfterSemicolonInForStatements");
	}

	public void setInsertSpaceAfterSemicolonInForStatements(Boolean insertSpaceAfterSemicolonInForStatements) {
		super.set("insertSpaceAfterSemicolonInForStatements", insertSpaceAfterSemicolonInForStatements);
	}

	public Boolean getInsertSpaceBeforeAndAfterBinaryOperators() {
		return JsonHelper.getBoolean(this, "insertSpaceBeforeAndAfterBinaryOperators");
	}

	public void setInsertSpaceBeforeAndAfterBinaryOperators(Boolean insertSpaceBeforeAndAfterBinaryOperators) {
		super.set("insertSpaceBeforeAndAfterBinaryOperators", insertSpaceBeforeAndAfterBinaryOperators);
	}

	public Boolean getInsertSpaceAfterKeywordsInControlFlowStatements() {
		return JsonHelper.getBoolean(this, "insertSpaceAfterKeywordsInControlFlowStatements");
	}

	public void setInsertSpaceAfterKeywordsInControlFlowStatements(
			Boolean insertSpaceAfterKeywordsInControlFlowStatements) {
		super.set("insertSpaceAfterKeywordsInControlFlowStatements", insertSpaceAfterKeywordsInControlFlowStatements);
	}

	public Boolean getInsertSpaceAfterFunctionKeywordForAnonymousFunctions() {
		return JsonHelper.getBoolean(this, "insertSpaceAfterFunctionKeywordForAnonymousFunctions");
	}

	public void setInsertSpaceAfterFunctionKeywordForAnonymousFunctions(
			Boolean insertSpaceAfterFunctionKeywordForAnonymousFunctions) {
		super.set("insertSpaceAfterFunctionKeywordForAnonymousFunctions",
				insertSpaceAfterFunctionKeywordForAnonymousFunctions);
	}

	public Boolean getInsertSpaceAfterOpeningAndBeforeClosingNonemptyParenthesis() {
		return JsonHelper.getBoolean(this, "insertSpaceAfterOpeningAndBeforeClosingNonemptyParenthesis");
	}

	public void setInsertSpaceAfterOpeningAndBeforeClosingNonemptyParenthesis(
			Boolean insertSpaceAfterOpeningAndBeforeClosingNonemptyParenthesis) {
		super.set("insertSpaceAfterOpeningAndBeforeClosingNonemptyParenthesis",
				insertSpaceAfterOpeningAndBeforeClosingNonemptyParenthesis);
	}

	public Boolean getInsertSpaceAfterOpeningAndBeforeClosingNonemptyBrackets() {
		return JsonHelper.getBoolean(this, "insertSpaceAfterOpeningAndBeforeClosingNonemptyBrackets");
	}

	public void setInsertSpaceAfterOpeningAndBeforeClosingNonemptyBrackets(
			Boolean insertSpaceAfterOpeningAndBeforeClosingNonemptyBrackets) {
		super.set("insertSpaceAfterOpeningAndBeforeClosingNonemptyBrackets",
				insertSpaceAfterOpeningAndBeforeClosingNonemptyBrackets);
	}

	public Boolean getPlaceOpenBraceOnNewLineForFunctions() {
		return JsonHelper.getBoolean(this, "placeOpenBraceOnNewLineForFunctions");
	}

	public void setPlaceOpenBraceOnNewLineForFunctions(Boolean placeOpenBraceOnNewLineForFunctions) {
		super.set("placeOpenBraceOnNewLineForFunctions", placeOpenBraceOnNewLineForFunctions);
	}

	public Boolean getPlaceOpenBraceOnNewLineForControlBlocks() {
		return JsonHelper.getBoolean(this, "placeOpenBraceOnNewLineForControlBlocks");
	}

	public void setPlaceOpenBraceOnNewLineForControlBlocks(Boolean placeOpenBraceOnNewLineForControlBlocks) {
		super.set("placeOpenBraceOnNewLineForControlBlocks", placeOpenBraceOnNewLineForControlBlocks);
	}

}
