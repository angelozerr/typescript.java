package ts.eclipse.ide.terminal.interpreter.internal;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.eclipse.tm.internal.terminal.provisional.api.Logger;

public class AnsiHandler {

	/** This is a character processing state: Initial state. */
	private static final int ANSISTATE_INITIAL = 0;

	/** This is a character processing state: We've seen an escape character. */
	private static final int ANSISTATE_ESCAPE = 1;

	/**
	 * This is a character processing state: We've seen a '[' after an escape
	 * character. Expecting a parameter character or a command character next.
	 */
	private static final int ANSISTATE_EXPECTING_PARAMETER_OR_COMMAND = 2;

	/**
	 * This is a character processing state: We've seen a ']' after an escape
	 * character. We are now expecting an operating system command that
	 * reprograms an intelligent terminal.
	 */
	private static final int ANSISTATE_EXPECTING_OS_COMMAND = 3;

	/**
	 * This is a character processing state: We've seen a '[?' after an escape
	 * character. Expecting a parameter character or a command character next.
	 */
	private static final int ANSISTATE_EXPECTING_DEC_PRIVATE_COMMAND = 4;

	/**
	 * This is a character processing state: We've seen one of ()*+-./ after an
	 * escape character. Expecting a character set designation character.
	 */
	private static final int ANSISTATE_EXPECTING_CHARSET_DESIGNATION = 5;

	/**
	 * This field holds the current state of the Finite TerminalState Automaton
	 * (FSA) that recognizes ANSI escape sequences.
	 *
	 * @see #processNewText()
	 */
	private int ansiState = ANSISTATE_INITIAL;

	/**
	 * This field hold the saved absolute line number of the cursor when
	 * processing the "ESC 7" and "ESC 8" command sequences.
	 */
	private int savedCursorLine = 0;

	/**
	 * This field hold the saved column number of the cursor when processing the
	 * "ESC 7" and "ESC 8" command sequences.
	 */
	private int savedCursorColumn = 0;

	/**
	 * This field holds an array of StringBuffer objects, each of which is one
	 * parameter from the current ANSI escape sequence. For example, when
	 * parsing the escape sequence "\e[20;10H", this array holds the strings
	 * "20" and "10".
	 */
	private final StringBuffer[] ansiParameters = new StringBuffer[16];

	/**
	 * This field holds the OS-specific command found in an escape sequence of
	 * the form "\e]...\u0007".
	 */
	private final StringBuffer ansiOsCommand = new StringBuffer(128);

	/**
	 * This field holds the index of the next unused element of the array stored
	 * in field {@link #ansiParameters}.
	 */
	private int nextAnsiParameter = 0;

	boolean fCrAfterNewLine;

	private String text;

	private int index;

	public synchronized void parse(byte[] byteBuffer, int bytesRead, String encoding) {
		this.text = getText(byteBuffer, bytesRead, encoding);
		// System.err.println(text);
		this.index = 0;
		for (int i = 0; i < ansiParameters.length; ++i) {
			ansiParameters[i] = new StringBuffer();
		}
		processNewText();
	}

	private String getText(byte[] byteBuffer, int bytesRead, String encoding) {
		if (encoding == null) {
			return new String(byteBuffer, 0, bytesRead);
		}
		try {
			return new String(byteBuffer, 0, bytesRead, encoding);
		} catch (UnsupportedEncodingException e) {
			return new String(byteBuffer, 0, bytesRead);
		}
	}
	
	/**
	 * This method scans the newly received text, processing ANSI control
	 * characters and escape sequences and displaying normal text.
	 * 
	 * @throws IOException
	 */
	private void processNewText() {
		Logger.log("entered"); //$NON-NLS-1$

		// Scan the newly received text.

		while (hasNextChar()) {
			char character = getNextChar();
			if (character != '\n') {
				fCrAfterNewLine = false;
			}
			// System.err.print(character);
			switch (ansiState) {
			case ANSISTATE_INITIAL:
				switch (character) {
				case '\u0000':
					break; // NUL character. Ignore it.

				case '\u0007':
					processBEL(); // BEL (Control-G)
					break;

				case '\b':
					processBackspace(); // Backspace
					break;

				case '\t':
					processTab(); // Tab.
					break;

				case '\n':					
					if (fCrAfterNewLine)
						processCarriageReturnLineFeed(); // CRLF
					else 
						processNewline(); // Newline (Control-J)
					break;

				case '\r':
					fCrAfterNewLine = true;
					processCarriageReturn(); // Carriage Return (Control-M)
					break;

				case '\u001b':
					ansiState = ANSISTATE_ESCAPE; // Escape.
					break;

				default:
					processNonControlCharacters(character);
					break;
				}
				break;

			case ANSISTATE_ESCAPE:
				// We've seen an escape character. Here, we process the
				// character
				// immediately following the escape.

				switch (character) {
				case '[':
					ansiState = ANSISTATE_EXPECTING_PARAMETER_OR_COMMAND;
					nextAnsiParameter = 0;

					// Erase the parameter strings in preparation for optional
					// parameter characters.

					for (int i = 0; i < ansiParameters.length; ++i) {
						ansiParameters[i].delete(0, ansiParameters[i].length());
					}
					break;

				case ']':
					ansiState = ANSISTATE_EXPECTING_OS_COMMAND;
					ansiOsCommand.delete(0, ansiOsCommand.length());
					break;

				case ')':
				case '(':
				case '*':
				case '+':
				case '-':
				case '.':
				case '/':
					ansiState = ANSISTATE_EXPECTING_CHARSET_DESIGNATION;
					break;

				case '7':
					// Save cursor position and character attributes

					ansiState = ANSISTATE_INITIAL;
					// savedCursorLine = relativeCursorLine();
					// savedCursorColumn = getCursorColumn();
					break;

				case '8':
					// Restore cursor and attributes to previously saved
					// position

					ansiState = ANSISTATE_INITIAL;
					moveCursor(savedCursorLine, savedCursorColumn);
					break;

				case 'c':
					// Reset the terminal
					ansiState = ANSISTATE_INITIAL;
					// resetTerminal();
					break;

				default:
					Logger.log("Unsupported escape sequence: escape '" + character + "'"); //$NON-NLS-1$ //$NON-NLS-2$
					ansiState = ANSISTATE_INITIAL;
					break;
				}
				break;

			case ANSISTATE_EXPECTING_PARAMETER_OR_COMMAND:
				if (character == '?') {
					ansiState = ANSISTATE_EXPECTING_DEC_PRIVATE_COMMAND;
					break;
				}

				// Parameters can appear after the '[' in an escape sequence,
				// but they
				// are optional.

				if (character == '@' || (character >= 'A' && character <= 'Z')
						|| (character >= 'a' && character <= 'z')) {
					ansiState = ANSISTATE_INITIAL;
					processAnsiCommandCharacter(character);
				} else {
					processAnsiParameterCharacter(character);
				}
				break;

			case ANSISTATE_EXPECTING_OS_COMMAND:
				// A BEL (\u0007) character marks the end of the OSC sequence.

				if (character == '\u0007') {
					ansiState = ANSISTATE_INITIAL;
					processAnsiOsCommand();
				} else {
					ansiOsCommand.append(character);
				}
				break;

			case ANSISTATE_EXPECTING_DEC_PRIVATE_COMMAND:
				// Parameters can appear after the '[?' in an escape sequence,
				// but they
				// are optional.

				if (character == '@' || (character >= 'A' && character <= 'Z')
						|| (character >= 'a' && character <= 'z')) {
					ansiState = ANSISTATE_INITIAL;
					processDecPrivateCommandCharacter(character);
				} else {
					processAnsiParameterCharacter(character);
				}
				break;

			case ANSISTATE_EXPECTING_CHARSET_DESIGNATION:
				if (character != '%')
					ansiState = ANSISTATE_INITIAL;
				// Character set designation commands are ignored
				break;

			default:
				// This should never happen! If it does happen, it means there
				// is a
				// bug in the FSA. For robustness, we return to the initial
				// state.

				Logger.log("INVALID ANSI FSA STATE: " + ansiState); //$NON-NLS-1$
				ansiState = ANSISTATE_INITIAL;
				break;
			}
		}
	}

	private char getNextChar() {
		return text.charAt(index++);
	}

	private boolean hasNextChar() {
		return (index < text.length());
	}

	/**
	 * Put back one character to the stream. This method can push back exactly
	 * one character. The character is the next character returned by
	 * {@link #getNextChar}
	 * 
	 * @param c
	 *            the character to be pushed back.
	 */
	void pushBackChar(char c) {
		// assert fNextChar!=-1: "Already a character waiting:"+fNextChar;
		// //$NON-NLS-1$
		// fNextChar=c;
		index--;
	}


	protected void processBEL() {
		
	}
	
	protected void processDecPrivateCommandCharacter(char character) {
		// TODO Auto-generated method stub
		
	}

	protected void processAnsiOsCommand() {
		// TODO Auto-generated method stub
		
	}

	protected void processAnsiParameterCharacter(char character) {
		// TODO Auto-generated method stub
		
	}

	protected void processAnsiCommandCharacter(char character) {
		// TODO Auto-generated method stub
		
	}

	protected void moveCursor(int savedCursorLine2, int savedCursorColumn2) {
		//System.err.println(savedCursorColumn2);
	}

	/**
	 * This method processes a contiguous sequence of non-control characters.
	 * This is a performance optimization, so that we don't have to insert or
	 * append each non-control character individually to the StyledText widget.
	 * A non-control character is any character that passes the condition in the
	 * below while loop.
	 * 
	 * @throws IOException
	 */
	private void processNonControlCharacters(char character) {
		StringBuffer buffer = new StringBuffer();
		buffer.append(character);
		// Identify a contiguous sequence of non-control characters, starting at
		// firstNonControlCharacterIndex in newText.
		while (hasNextChar()) {
			character = getNextChar();
			if (character == '\u0000' || character == '\b' || character == '\t' || character == '\u0007'
					|| character == '\n' || character == '\r' || character == '\u001b') {
				pushBackChar(character);
				break;
			}
			buffer.append(character);
		}

		// Now insert the sequence of non-control characters in the StyledText
		// widget
		// at the location of the cursor.

		processText(buffer.toString());
	}

	protected void processText(String string) {
		//System.err.println(string);
	}

	protected void processCarriageReturnLineFeed() {
		
	}
	
	protected void processCarriageReturn() {

	}

	protected  void processNewline() {

	}

	protected void processTab() {
		
	}

	protected void processBackspace() {
		
	}

}
