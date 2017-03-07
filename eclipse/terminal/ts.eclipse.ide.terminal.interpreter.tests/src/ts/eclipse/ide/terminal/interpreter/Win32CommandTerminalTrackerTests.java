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
		test.processText("Microsoft Windows [version 6.1.7601]");
		test.processCarriageReturnLineFeed();
		test.processText("Copyright (c) 2009 Microsoft Corporation. Tous droits réservés.");
		test.processCarriageReturnLineFeed();
		test.processCarriageReturnLineFeed();
		test.processText("D:\\Logiciels\\eclipses\\eclipse-jee-neon-2-win32-x86_64\\eclipse>");
		test.processText("D:\\Logiciels\\eclipses\\eclipse-jee-neon-2-win32-x86_64\\eclipse>c");
		test.processText("D:\\Logiciels\\eclipses\\eclipse-jee-neon-2-win32-x86_64\\eclipse>cd");
		test.processText("D:\\Logiciels\\eclipses\\eclipse-jee-neon-2-win32-x86_64\\eclipse>cd .");
		test.processCarriageReturnLineFeed();
		test.processCarriageReturnLineFeed();
		test.processText("D:\\Logiciels\\eclipses\\eclipse-jee-neon-2-win32-x86_64\\eclipse>");

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
		test.processText("Microsoft Windows [version 6.1.7601]");
		test.processCarriageReturnLineFeed();
		test.processText("Copyright (c) 2009 Microsoft Corporation. Tous droits réservés.");
		test.processCarriageReturnLineFeed();
		test.processCarriageReturnLineFeed();
		test.processText("D:\\Logiciels\\eclipses\\eclipse-jee-neon-2-win32-x86_64\\eclipse>");
		
		// User types "cd .."
		test.processText("D:\\Logiciels\\eclipses\\eclipse-jee-neon-2-win32-x86_64\\eclipse>c");
		test.processText("D:\\Logiciels\\eclipses\\eclipse-jee-neon-2-win32-x86_64\\eclipse>cd");
		test.processText("D:\\Logiciels\\eclipses\\eclipse-jee-neon-2-win32-x86_64\\eclipse>cd .");
		test.processText("D:\\Logiciels\\eclipses\\eclipse-jee-neon-2-win32-x86_64\\eclipse>cd ..");

		// User types "Enter"
		test.processCarriageReturnLineFeed();
		test.processCarriageReturnLineFeed();
		test.processText("D:\\Logiciels\\eclipses\\eclipse-jee-neon-2-win32-x86_64>");
		
		// User types "cd eclipse"
		test.processText("D:\\Logiciels\\eclipses\\eclipse-jee-neon-2-win32-x86_64>c");
		test.processText("D:\\Logiciels\\eclipses\\eclipse-jee-neon-2-win32-x86_64>cd");
		test.processText("D:\\Logiciels\\eclipses\\eclipse-jee-neon-2-win32-x86_64>cd e");
		test.processText("D:\\Logiciels\\eclipses\\eclipse-jee-neon-2-win32-x86_64>cd ec");
		test.processText("D:\\Logiciels\\eclipses\\eclipse-jee-neon-2-win32-x86_64>cd ecl");
		test.processText("D:\\Logiciels\\eclipses\\eclipse-jee-neon-2-win32-x86_64>cd ecli");
		test.processText("D:\\Logiciels\\eclipses\\eclipse-jee-neon-2-win32-x86_64>cd eclip");
		test.processText("D:\\Logiciels\\eclipses\\eclipse-jee-neon-2-win32-x86_64>cd eclips");
		test.processText("D:\\Logiciels\\eclipses\\eclipse-jee-neon-2-win32-x86_64>cd eclipse");

		// User types "Enter"
		test.processCarriageReturnLineFeed();
		test.processCarriageReturnLineFeed();
		test.processText("D:\\Logiciels\\eclipses\\eclipse-jee-neon-2-win32-x86_64\\eclipse>");

		String expected = ""
				+ "SUBMIT: workingDir=D:\\Logiciels\\eclipses\\eclipse-jee-neon-2-win32-x86_64\\eclipse, command=cd ..\n"
				+ "TERMINATE: workingDir=D:\\Logiciels\\eclipses\\eclipse-jee-neon-2-win32-x86_64, command=cd ..\n"
				+ "SUBMIT: workingDir=D:\\Logiciels\\eclipses\\eclipse-jee-neon-2-win32-x86_64, command=cd eclipse\n"
				+ "TERMINATE: workingDir=D:\\Logiciels\\eclipses\\eclipse-jee-neon-2-win32-x86_64\\eclipse, command=cd eclipse";

		Assert.assertEquals(expected, test.toString());
	}

}
