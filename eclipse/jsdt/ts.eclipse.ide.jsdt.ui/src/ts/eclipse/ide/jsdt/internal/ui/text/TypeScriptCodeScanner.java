/*******************************************************************************
 * Copyright (c) 2000, 2016 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Philippe Ombredanne <pombredanne@nexb.com> - https://bugs.eclipse.org/bugs/show_bug.cgi?id=150989
 *     Shane Bryzak <sbryzak@redhat.com> - https://bugs.eclipse.org/bugs/show_bug.cgi?id=477030
 *******************************************************************************/

package ts.eclipse.ide.jsdt.internal.ui.text;


import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.IWhitespaceDetector;
import org.eclipse.jface.text.rules.IWordDetector;
import org.eclipse.jface.text.rules.SingleLineRule;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.rules.WhitespaceRule;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.wst.jsdt.core.JavaScriptCore;
import org.eclipse.wst.jsdt.internal.ui.javaeditor.SemanticHighlightings;
import org.eclipse.wst.jsdt.internal.ui.text.AbstractJavaScanner;
import org.eclipse.wst.jsdt.internal.ui.text.CombinedWordRule;
import org.eclipse.wst.jsdt.internal.ui.text.ISourceVersionDependent;
import org.eclipse.wst.jsdt.internal.ui.text.JavaWhitespaceDetector;
import org.eclipse.wst.jsdt.internal.ui.text.JavaWordDetector;
import org.eclipse.wst.jsdt.ui.PreferenceConstants;
import org.eclipse.wst.jsdt.ui.text.IColorManager;
import org.eclipse.wst.jsdt.ui.text.IJavaScriptColorConstants;


/**
 * A Java code scanner.
 */
public final class TypeScriptCodeScanner extends AbstractJavaScanner {

	/**
	 * Rule to detect java operators.
	 *
	 * 
	 */
	private static final class OperatorRule implements IRule {

		/** Java operators */
		private final char[] JAVA_OPERATORS= { ';', '.', '=', '/', '\\', '+', '-', '*', '<', '>', ':', '?', '!', ',', '|', '&', '^', '%', '~'};
		/** Token to return for this rule */
		private final IToken fToken;

		/**
		 * Creates a new operator rule.
		 *
		 * @param token Token to use for this rule
		 */
		public OperatorRule(IToken token) {
			fToken= token;
		}

		/**
		 * Is this character an operator character?
		 *
		 * @param character Character to determine whether it is an operator character
		 * @return <code>true</code> iff the character is an operator, <code>false</code> otherwise.
		 */
		public boolean isOperator(char character) {
			for (int index= 0; index < JAVA_OPERATORS.length; index++) {
				if (JAVA_OPERATORS[index] == character)
					return true;
			}
			return false;
		}

		/*
		 * @see org.eclipse.jface.text.rules.IRule#evaluate(org.eclipse.jface.text.rules.ICharacterScanner)
		 */
		public IToken evaluate(ICharacterScanner scanner) {

			int character= scanner.read();
			if (isOperator((char) character)) {
				do {
					character= scanner.read();
				} while (isOperator((char) character));
				scanner.unread();
				return fToken;
			} else {
				scanner.unread();
				return Token.UNDEFINED;
			}
		}
	}

	/**
	 * Rule to detect java brackets.
	 *
	 * 
	 */
	private static final class BracketRule implements IRule {

		/** Java brackets */
		private final char[] JAVA_BRACKETS= { '(', ')', '{', '}', '[', ']' };
		/** Token to return for this rule */
		private final IToken fToken;

		/**
		 * Creates a new bracket rule.
		 *
		 * @param token Token to use for this rule
		 */
		public BracketRule(IToken token) {
			fToken= token;
		}

		/**
		 * Is this character a bracket character?
		 *
		 * @param character Character to determine whether it is a bracket character
		 * @return <code>true</code> iff the character is a bracket, <code>false</code> otherwise.
		 */
		public boolean isBracket(char character) {
			for (int index= 0; index < JAVA_BRACKETS.length; index++) {
				if (JAVA_BRACKETS[index] == character)
					return true;
			}
			return false;
		}

		/*
		 * @see org.eclipse.jface.text.rules.IRule#evaluate(org.eclipse.jface.text.rules.ICharacterScanner)
		 */
		public IToken evaluate(ICharacterScanner scanner) {

			int character= scanner.read();
			if (isBracket((char) character)) {
				do {
					character= scanner.read();
				} while (isBracket((char) character));
				scanner.unread();
				return fToken;
			} else {
				scanner.unread();
				return Token.UNDEFINED;
			}
		}
	}

	/**
	 * An annotation rule matches the '@' symbol, any following whitespace and
	 * optionally a following <code>interface</code> keyword.
	 *
	 * It does not match if there is a comment between the '@' symbol and
	 * the identifier. See https://bugs.eclipse.org/bugs/show_bug.cgi?id=82452
	 *
	 * @since 3.1
	 */
	private static class DecoratorRule implements IRule, ISourceVersionDependent {
		/**
		 * A resettable scanner supports marking a position in a scanner and
		 * unreading back to the marked position.
		 */
		private static final class ResettableScanner implements ICharacterScanner {
			private final ICharacterScanner fDelegate;
			private int fReadCount;

			/**
			 * Creates a new resettable scanner that will forward calls
			 * to <code>scanner</code>, but store a marked position.
			 *
			 * @param scanner the delegate scanner
			 */
			public ResettableScanner(final ICharacterScanner scanner) {
				Assert.isNotNull(scanner);
				fDelegate= scanner;
				mark();
			}

			/*
			 * @see org.eclipse.jface.text.rules.ICharacterScanner#getColumn()
			 */
			@Override
			public int getColumn() {
				return fDelegate.getColumn();
			}

			/*
			 * @see org.eclipse.jface.text.rules.ICharacterScanner#getLegalLineDelimiters()
			 */
			@Override
			public char[][] getLegalLineDelimiters() {
				return fDelegate.getLegalLineDelimiters();
			}

			/*
			 * @see org.eclipse.jface.text.rules.ICharacterScanner#read()
			 */
			@Override
			public int read() {
				int ch= fDelegate.read();
				if (ch != ICharacterScanner.EOF)
					fReadCount++;
				return ch;
			}

			/*
			 * @see org.eclipse.jface.text.rules.ICharacterScanner#unread()
			 */
			@Override
			public void unread() {
				if (fReadCount > 0)
					fReadCount--;
				fDelegate.unread();
			}

			/**
			 * Marks an offset in the scanned content.
			 */
			public void mark() {
				fReadCount= 0;
			}

			/**
			 * Resets the scanner to the marked position.
			 */
			public void reset() {
				while (fReadCount > 0)
					unread();

				while (fReadCount < 0)
					read();
			}
		}

		private final IWhitespaceDetector fWhitespaceDetector= new JavaWhitespaceDetector();
		private final IWordDetector fWordDetector= new JavaWordDetector();
		private final IToken decoratorToken;
		private final String fVersion;
		private boolean fIsVersionMatch;

		/**
		 * Creates a new rule.
		 *
		 * @param interfaceToken the token to return if
		 *        <code>'@\s*interface'</code> is matched
		 * @param atToken the token to return if <code>'@'</code>
		 *        is matched, but not <code>'@\s*interface'</code>
		 * @param version the lowest <code>JavaCore.COMPILER_SOURCE</code>
		 *        version that this rule is enabled
		 * @param currentVersion the current
		 *        <code>JavaCore.COMPILER_SOURCE</code> version
		 */
		public DecoratorRule(Token decoratorToken, String version, String currentVersion) {
			this.decoratorToken= decoratorToken;
			fVersion= version;
			setSourceVersion(currentVersion);
		}

		/*
		 * @see org.eclipse.jface.text.rules.IRule#evaluate(org.eclipse.jface.text.rules.ICharacterScanner)
		 */
		@Override
		public IToken evaluate(ICharacterScanner scanner) {
			if (!fIsVersionMatch)
				return Token.UNDEFINED;

			ResettableScanner resettable= new ResettableScanner(scanner);
			if (resettable.read() == '@')
				return readAnnotation(resettable);

			resettable.reset();
			return Token.UNDEFINED;
		}

		private IToken readAnnotation(ResettableScanner scanner) {
			if (!readIdentifier(scanner, null)) {
				scanner.reset();
				return Token.UNDEFINED;
			}
			while (readSegment(new ResettableScanner(scanner))) {
				// do nothing
			}
			return decoratorToken;
		}

		private boolean readSegment(ResettableScanner scanner) {
			scanner.mark();
			if (skipWhitespace(scanner) && skipDot(scanner) && skipWhitespace(scanner) && readIdentifier(scanner, null))
				return true;
			
			scanner.reset();
			return false;
		}


		private boolean skipDot(ICharacterScanner scanner) {
			int ch= scanner.read();
			if (ch == '.')
				return true;
			
			scanner.unread();
			return false;
		}

		private boolean readIdentifier(ICharacterScanner scanner, StringBuffer buffer) {
			int ch= scanner.read();
			boolean read= false;
			while (fWordDetector.isWordPart((char) ch)) {
				if (buffer != null)
					buffer.append((char) ch);
				ch= scanner.read();
				read= true;
			}

			if (ch != ICharacterScanner.EOF)
				scanner.unread();
			
			return read;
		}

		private boolean skipWhitespace(ICharacterScanner scanner) {
			while (fWhitespaceDetector.isWhitespace((char) scanner.read())) {
				// do nothing
			} 
			
			scanner.unread();
			return true;
		}

		/*
		 * @see org.eclipse.jdt.internal.ui.text.ISourceVersionDependent#setSourceVersion(java.lang.String)
		 */
		@Override
		public void setSourceVersion(String version) {
			fIsVersionMatch= true; //fVersion.compareTo(version) <= 0;
		}

	}
	
	private static final String SOURCE_VERSION= JavaScriptCore.COMPILER_SOURCE;

	static String[] fgKeywords= {
		"break", //$NON-NLS-1$
		"case", "catch", "class", "const", "continue", //$NON-NLS-5$ //$NON-NLS-4$ //$NON-NLS-3$ //$NON-NLS-2$ //$NON-NLS-1$
		"default", "delete", "debugger", "do", //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-1$
		"else", "export", "extends", //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-1$
		"finally", "for",  "function",//$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-2$ //$NON-NLS-1$
		"if", "implements", "in", "instanceof", "interface", //$NON-NLS-5$ //$NON-NLS-4$ //$NON-NLS-3$ //$NON-NLS-2$ //$NON-NLS-1$
		  "new", //$NON-NLS-2$ //$NON-NLS-1$ //$NON-NLS-1$ //$NON-NLS-1$ //$NON-NLS-1$
		"package", "private", "protected", "public", //$NON-NLS-4$ //$NON-NLS-3$ //$NON-NLS-2$ //$NON-NLS-1$
		"static", "super", "switch", //$NON-NLS-4$ //$NON-NLS-3$ //$NON-NLS-2$
		"this", "throw", "try","typeof",  //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-2$ //$NON-NLS-1$
		"var", //$NON-NLS-1$
		"while", "with", //$NON-NLS-1$ //$NON-NLS-2$
	
		// ES5 keywords
		"enum", //$NON-NLS-1$		
		
		// ES6 keywords
		"let",  //$NON-NLS-1$
		"of",  //$NON-NLS-1$
		"import", //$NON-NLS-1$
		"yield", //$NON-NLS-1$
		"function*",		 //$NON-NLS-1$		
	};

	// see https://github.com/Microsoft/TypeScript/blob/master/doc/spec.md#2.2
	static String[] fgTypeScriptKeywords= {	
		// (Missing) Reserved Words
		"void",
		// keywords cannot be used as user defined type names, but are otherwise not restricted:
		"any", //$NON-NLS-1$
		"boolean", //$NON-NLS-1$
		"number", //$NON-NLS-1$
		"string", //$NON-NLS-1$
		"symbol", //$NON-NLS-1$
		// keywords have special meaning in certain contexts, but are valid identifiers:
		"abstract", //$NON-NLS-1$
		"as", //$NON-NLS-1$
		"async", //$NON-NLS-1$
		"await", //$NON-NLS-1$
		"constructor", //$NON-NLS-1$
		"declare", //$NON-NLS-1$
		"from", //$NON-NLS-1$
		"get", //$NON-NLS-1$
		"is", //$NON-NLS-1$
		"module", //$NON-NLS-1$
		"namespace", //$NON-NLS-1$
		"require", //$NON-NLS-1$
		"set", //$NON-NLS-1$
		"type" //$NON-NLS-1$			
	};

	private static final String RETURN= "return"; //$NON-NLS-1$

	private static String[] fgConstants= { "false", "null", "true" , "undefined"}; //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-2$ //$NON-NLS-1$

	private static final String ANNOTATION_BASE_KEY= PreferenceConstants.EDITOR_SEMANTIC_HIGHLIGHTING_PREFIX + SemanticHighlightings.ANNOTATION;
	private static final String ANNOTATION_COLOR_KEY= ANNOTATION_BASE_KEY + PreferenceConstants.EDITOR_SEMANTIC_HIGHLIGHTING_COLOR_SUFFIX;

	private static String[] fgTokenProperties= {
		IJavaScriptColorConstants.JAVA_KEYWORD,
		IJavaScriptColorConstants.JAVA_STRING,
		IJavaScriptColorConstants.JAVA_DEFAULT,
		IJavaScriptColorConstants.JAVA_KEYWORD_RETURN,
		IJavaScriptColorConstants.JAVA_OPERATOR,
		IJavaScriptColorConstants.JAVA_BRACKET,
		IJavaScriptColorConstants.JAVASCRIPT_TEMPLATE_LITERAL,
		ANNOTATION_COLOR_KEY,
		ITypeScriptColorConstants.DECORATOR
	};

	/**
	 * Creates a Java code scanner
	 *
	 * @param manager	the color manager
	 * @param store		the preference store 
	 */
	public TypeScriptCodeScanner(IColorManager manager, IPreferenceStore store) {
		super(manager, store);
		initialize();
	}

	/*
	 * @see AbstractJavaScanner#getTokenProperties()
	 */
	protected String[] getTokenProperties() {
		return fgTokenProperties;
	}

	/*
	 * @see AbstractJavaScanner#createRules()
	 */
	protected List createRules() {

		List rules= new ArrayList();

		// Add rule for character constants.
		Token token= getToken(IJavaScriptColorConstants.JAVA_STRING);
		rules.add(new SingleLineRule("'", "'", token, '\\')); //$NON-NLS-2$ //$NON-NLS-1$


		// Add generic whitespace rule.
		rules.add(new WhitespaceRule(new JavaWhitespaceDetector()));

		String version= getPreferenceStore().getString(SOURCE_VERSION);

		// Add JLS3 rule for /@\s*interface/ and /@\s*\w+/
		token = getToken(ITypeScriptColorConstants.DECORATOR);
		DecoratorRule decoratorRule = new DecoratorRule(token,
				"", version);
		rules.add(decoratorRule);
		
		// Add word rule for new keywords, 4077
		JavaWordDetector wordDetector= new JavaWordDetector();
		token= getToken(IJavaScriptColorConstants.JAVA_DEFAULT);
		CombinedWordRule combinedWordRule= new CombinedWordRule(wordDetector, token);
		
		// Add rule for operators
		token= getToken(IJavaScriptColorConstants.JAVA_OPERATOR);
		rules.add(new OperatorRule(token));

		// Add rule for brackets
		token= getToken(IJavaScriptColorConstants.JAVA_BRACKET);
		rules.add(new BracketRule(token));

		// Add word rule for keyword 'return'.
		CombinedWordRule.WordMatcher returnWordRule= new CombinedWordRule.WordMatcher();
		token= getToken(IJavaScriptColorConstants.JAVA_KEYWORD_RETURN);
		returnWordRule.addWord(RETURN, token);  
		combinedWordRule.addWordMatcher(returnWordRule);

		// Add word rule for keywords, types, and constants.
		CombinedWordRule.WordMatcher wordRule= new CombinedWordRule.WordMatcher();
		token= getToken(IJavaScriptColorConstants.JAVA_KEYWORD);
		for (int i=0; i<fgKeywords.length; i++)
			wordRule.addWord(fgKeywords[i], token);
		for (int i=0; i<fgConstants.length; i++)
			wordRule.addWord(fgConstants[i], token);
		// TypeScript key words
		for (int i=0; i<fgTypeScriptKeywords.length; i++)
			wordRule.addWord(fgTypeScriptKeywords[i], token);

		combinedWordRule.addWordMatcher(wordRule);

		rules.add(combinedWordRule);

		setDefaultReturnToken(getToken(IJavaScriptColorConstants.JAVA_DEFAULT));
		return rules;
	}
	
	/*
	 * @see org.eclipse.wst.jsdt.internal.ui.text.AbstractJavaScanner#getBoldKey(java.lang.String)
	 */
	protected String getBoldKey(String colorKey) {
		if ((ANNOTATION_COLOR_KEY).equals(colorKey))
			return ANNOTATION_BASE_KEY + PreferenceConstants.EDITOR_SEMANTIC_HIGHLIGHTING_BOLD_SUFFIX;
		return super.getBoldKey(colorKey);
	}
	
	/*
	 * @see org.eclipse.wst.jsdt.internal.ui.text.AbstractJavaScanner#getItalicKey(java.lang.String)
	 */
	protected String getItalicKey(String colorKey) {
		if ((ANNOTATION_COLOR_KEY).equals(colorKey))
			return ANNOTATION_BASE_KEY + PreferenceConstants.EDITOR_SEMANTIC_HIGHLIGHTING_ITALIC_SUFFIX;
		return super.getItalicKey(colorKey);
	}
	
	/*
	 * @see org.eclipse.wst.jsdt.internal.ui.text.AbstractJavaScanner#getStrikethroughKey(java.lang.String)
	 */
	protected String getStrikethroughKey(String colorKey) {
		if ((ANNOTATION_COLOR_KEY).equals(colorKey))
			return ANNOTATION_BASE_KEY + PreferenceConstants.EDITOR_SEMANTIC_HIGHLIGHTING_STRIKETHROUGH_SUFFIX;
		return super.getStrikethroughKey(colorKey);
	}
	
	/*
	 * @see org.eclipse.wst.jsdt.internal.ui.text.AbstractJavaScanner#getUnderlineKey(java.lang.String)
	 */
	protected String getUnderlineKey(String colorKey) {
		if ((ANNOTATION_COLOR_KEY).equals(colorKey))
			return ANNOTATION_BASE_KEY + PreferenceConstants.EDITOR_SEMANTIC_HIGHLIGHTING_UNDERLINE_SUFFIX;
		return super.getUnderlineKey(colorKey);
	}

	/*
	 * @see AbstractJavaScanner#affectsBehavior(PropertyChangeEvent)
	 */
	public boolean affectsBehavior(PropertyChangeEvent event) {
		return event.getProperty().equals(SOURCE_VERSION) || super.affectsBehavior(event);
	}

	/*
	 * @see AbstractJavaScanner#adaptToPreferenceChange(PropertyChangeEvent)
	 */
	public void adaptToPreferenceChange(PropertyChangeEvent event) {

		if (event.getProperty().equals(SOURCE_VERSION)) {

		} else if (super.affectsBehavior(event)) {
			super.adaptToPreferenceChange(event);
		}
	}
}
