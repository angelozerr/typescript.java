package ts.eclipse.ide.jsdt.internal.ui.text.jsx;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.IWordDetector;
import org.eclipse.jface.text.rules.SingleLineRule;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.rules.WordRule;
import org.eclipse.wst.jsdt.internal.ui.text.AbstractJavaScanner;
import org.eclipse.wst.jsdt.ui.text.IColorManager;

public class JSXScanner extends AbstractJavaScanner {

	private int startOffset;

	public JSXScanner(IColorManager manager, IPreferenceStore store) {
		super(manager, store);
		initialize();
	}

	private static String[] fgTokenProperties = { IJSXColorConstants.TAG_BORDER, IJSXColorConstants.TAG_NAME,
			IJSXColorConstants.TAG_ATTRIBUTE_NAME, IJSXColorConstants.TAG_ATTRIBUTE_EQUALS,
			IJSXColorConstants.TAG_ATTRIBUTE_VALUE };

	@Override
	protected String[] getTokenProperties() {
		return fgTokenProperties;
	}

	public IDocument getDocument() {
		return fDocument;
	}

	@Override
	protected List<IRule> createRules() {
		List<IRule> rules = new ArrayList<IRule>();

		Token tagBorder = getToken(IJSXColorConstants.TAG_BORDER);
		Token tagName = getToken(IJSXColorConstants.TAG_NAME);
		Token tagAttributeName = getToken(IJSXColorConstants.TAG_ATTRIBUTE_NAME);
		Token tagAttributeEquals = getToken(IJSXColorConstants.TAG_ATTRIBUTE_EQUALS);
		Token tagAttributeValue = getToken(IJSXColorConstants.TAG_ATTRIBUTE_VALUE);

		rules.add(new SingleLineRule("\"", "\"", tagAttributeValue, '\\'));
		rules.add(new SingleLineRule("'", "'", tagAttributeValue, '\\'));
		rules.add(new SingleLineRule("{", "}", tagAttributeValue, '\\'));
		rules.add(new JSXTagRule(tagName, tagBorder));
		rules.add(new WordRule(new NameDetector(), tagAttributeName));

		// setDefaultReturnToken(token);
		return rules;
	}

	public class NameDetector implements IWordDetector {

		/**
		 * @see IWordDetector#isWordPart(char)
		 */
		public boolean isWordPart(char ch) {
			if (Character.isUnicodeIdentifierPart(ch)) {
				return true;
			}
			switch (ch) {
			case '.':
			case '-':
			case '_':
			case ':':
				return true;
			}
			return false;
		}

		/**
		 * @see IWordDetector#isWordStart(char)
		 */
		public boolean isWordStart(char ch) {
			if (Character.isUnicodeIdentifierStart(ch)) {
				return true;
			}
			switch (ch) {
			case '_':
			case ':':
				return true;
			}
			return false;
		}

	}

	class OpenTagRule implements IRule {

		private IToken token;

		public OpenTagRule(IToken token) {
			this.token = token;
		}

		@Override
		public IToken evaluate(ICharacterScanner scanner) {
			int ch = scanner.read();
			if (ch == '<') {
				ch = scanner.read();
				if (ch == '/') {
					return token;
				}
				scanner.unread();
				return token;
			} else if (ch == '>') {
				ch = scanner.read();
				if (ch == '/') {
					return token;
				}
				scanner.unread();
				return token;
			}
			scanner.unread();
			return Token.UNDEFINED;
		}
	}

	class JSXTagRule implements IRule {

		private IToken tagBorder;
		private IToken tagName;

		public JSXTagRule(IToken tagName, IToken tagBorder) {
			this.tagName = tagName;
			this.tagBorder = tagBorder;
		}

		public IToken evaluate(ICharacterScanner scanner) {
			int offset = JSXScanner.this.fOffset;
			int startOffset = JSXScanner.this.startOffset;
			int localOffset = offset - startOffset;
			int rangeEnd = JSXScanner.this.fRangeEnd;

			// start/end tag
			int ch = scanner.read();
			switch (ch) {
			case '<':
				if (localOffset == 0) {
					// first character
					ch = scanner.read();
					if (ch == '/') {
						return tagBorder;
					}
					scanner.unread();
					return tagBorder;
				}
				break;
			case '>':
				if (offset == rangeEnd - 1) {
					// last character
					return tagBorder;
				}
				break;
			case '/':
				if (offset == rangeEnd - 2) {
					// last-1 character
					ch = scanner.read();
					if (ch == '>') {
						return tagBorder;
					}
					scanner.unread();
				}
				break;
			}

			// Tag name
			if (localOffset <= 2) {
				if (ch == '>') {
					//scanner.unread();
					return tagBorder;
				}
				loop: while (true) {
					switch (ch) {
					case ICharacterScanner.EOF:
					case 0x09:
					case 0x0A:
					case 0x0D:
					case 0x20:
					case '>':
						break loop;
					}

					ch = scanner.read();
					// firstCharIsSup = false;
				}

				if (ch == '>') {
					scanner.unread();
				}
				return tagName;
			}
			scanner.unread();
			return Token.UNDEFINED;
		}
	}

	@Override
	public void setRange(IDocument document, int offset, int length) {
		this.startOffset = offset;
		super.setRange(document, offset, length);
	}
}
