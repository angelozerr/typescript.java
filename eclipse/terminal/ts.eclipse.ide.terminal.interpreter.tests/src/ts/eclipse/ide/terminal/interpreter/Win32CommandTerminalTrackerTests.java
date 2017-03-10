/**
 *  Copyright (c) 2015-2017 Angelo ZERR.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 *  
 */
package ts.eclipse.ide.terminal.interpreter;

import org.junit.Assert;
import org.junit.Test;

/**
 * Win32 tracker test.
 *
 */
public class Win32CommandTerminalTrackerTests {

	@Test
	public void cdDot() {
		TrackerTest test = new TrackerTest(
				"D:\\Logiciels\\eclipses\\eclipse-jee-neon-2-win32-x86_64\\eclipse", null);
		test.processText("Microsoft Windows [version 6.1.7601]", 80);
		test.processCarriageReturnLineFeed();
		test.processText("Copyright (c) 2009 Microsoft Corporation. Tous droits réservés.", 80);
		test.processCarriageReturnLineFeed();
		test.processCarriageReturnLineFeed();
		test.processText("D:\\Logiciels\\eclipses\\eclipse-jee-neon-2-win32-x86_64\\eclipse>", 80);
		test.processText("D:\\Logiciels\\eclipses\\eclipse-jee-neon-2-win32-x86_64\\eclipse>c", 80);
		test.processText("D:\\Logiciels\\eclipses\\eclipse-jee-neon-2-win32-x86_64\\eclipse>cd", 80);
		test.processText("D:\\Logiciels\\eclipses\\eclipse-jee-neon-2-win32-x86_64\\eclipse>cd .", 80);
		test.processCarriageReturnLineFeed();
		test.processCarriageReturnLineFeed();
		test.processText("D:\\Logiciels\\eclipses\\eclipse-jee-neon-2-win32-x86_64\\eclipse>", 80);

		String expected = ""
				+ "SUBMIT: workingDir=D:\\Logiciels\\eclipses\\eclipse-jee-neon-2-win32-x86_64\\eclipse, command=cd .\n"
				+ "TERMINATE: workingDir=D:\\Logiciels\\eclipses\\eclipse-jee-neon-2-win32-x86_64\\eclipse, command=cd .";
		Assert.assertEquals(expected, test.toString());
	}

	@Test
	public void cdWithDirectoryChanged() {
		TrackerTest test = new TrackerTest(
				"D:\\Logiciels\\eclipses\\eclipse-jee-neon-2-win32-x86_64\\eclipse", null);

		// Open Terminal
		test.processText("Microsoft Windows [version 6.1.7601]", 80);
		test.processCarriageReturnLineFeed();
		test.processText("Copyright (c) 2009 Microsoft Corporation. Tous droits réservés.", 80);
		test.processCarriageReturnLineFeed();
		test.processCarriageReturnLineFeed();
		test.processText("D:\\Logiciels\\eclipses\\eclipse-jee-neon-2-win32-x86_64\\eclipse>", 80);
		
		// User types "cd .."
		test.processText("D:\\Logiciels\\eclipses\\eclipse-jee-neon-2-win32-x86_64\\eclipse>c", 80);
		test.processText("D:\\Logiciels\\eclipses\\eclipse-jee-neon-2-win32-x86_64\\eclipse>cd", 80);
		test.processText("D:\\Logiciels\\eclipses\\eclipse-jee-neon-2-win32-x86_64\\eclipse>cd .", 80);
		test.processText("D:\\Logiciels\\eclipses\\eclipse-jee-neon-2-win32-x86_64\\eclipse>cd ..", 80);

		// User types "Enter"
		test.processCarriageReturnLineFeed();
		test.processCarriageReturnLineFeed();
		test.processText("D:\\Logiciels\\eclipses\\eclipse-jee-neon-2-win32-x86_64>", 80);
		
		// User types "cd eclipse"
		test.processText("D:\\Logiciels\\eclipses\\eclipse-jee-neon-2-win32-x86_64>c", 80);
		test.processText("D:\\Logiciels\\eclipses\\eclipse-jee-neon-2-win32-x86_64>cd", 80);
		test.processText("D:\\Logiciels\\eclipses\\eclipse-jee-neon-2-win32-x86_64>cd e", 80);
		test.processText("D:\\Logiciels\\eclipses\\eclipse-jee-neon-2-win32-x86_64>cd ec", 80);
		test.processText("D:\\Logiciels\\eclipses\\eclipse-jee-neon-2-win32-x86_64>cd ecl", 80);
		test.processText("D:\\Logiciels\\eclipses\\eclipse-jee-neon-2-win32-x86_64>cd ecli", 80);
		test.processText("D:\\Logiciels\\eclipses\\eclipse-jee-neon-2-win32-x86_64>cd eclip", 80);
		test.processText("D:\\Logiciels\\eclipses\\eclipse-jee-neon-2-win32-x86_64>cd eclips", 80);
		test.processText("D:\\Logiciels\\eclipses\\eclipse-jee-neon-2-win32-x86_64>cd eclipse", 80);

		// User types "Enter"
		test.processCarriageReturnLineFeed();
		test.processCarriageReturnLineFeed();
		test.processText("D:\\Logiciels\\eclipses\\eclipse-jee-neon-2-win32-x86_64\\eclipse>", 80);

		String expected = ""
				+ "SUBMIT: workingDir=D:\\Logiciels\\eclipses\\eclipse-jee-neon-2-win32-x86_64\\eclipse, command=cd ..\n"
				+ "TERMINATE: workingDir=D:\\Logiciels\\eclipses\\eclipse-jee-neon-2-win32-x86_64, command=cd ..\n"
				+ "SUBMIT: workingDir=D:\\Logiciels\\eclipses\\eclipse-jee-neon-2-win32-x86_64, command=cd eclipse\n"
				+ "TERMINATE: workingDir=D:\\Logiciels\\eclipses\\eclipse-jee-neon-2-win32-x86_64\\eclipse, command=cd eclipse";

		Assert.assertEquals(expected, test.toString());
	}

}
