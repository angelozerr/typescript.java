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
 */package ts.eclipse.ide.terminal.interpreter;

import org.junit.Assert;
import org.junit.Test;

/**
 * Win32 tracker test.	
 *
 */
public class Win32NgTerminalTrackerTests {

	@Test
	public void ngClass() {
		TrackerTest test = new TrackerTest("C:\\Users\\azerr\\WS\\abcd\\src\\app", "ng generate class c1  --spec false");
		test.processText("Microsoft Windows [version 6.1.7601]", 80);
		test.processCarriageReturnLineFeed();
		test.processText("Copyright (c) 2009 Microsoft Corporation. Tous droits réservés.", 80);
		test.processCarriageReturnLineFeed();
		test.processCarriageReturnLineFeed();
		test.processText("C:\\Users\\azerr\\WS\\abcd\\src\\app>ng generate class c1  --spec false", 80);
		test.processCarriageReturnLineFeed();
		test.processText("installing class", 80);
		test.processCarriageReturnLineFeed();
		test.processText("  ", 80);
		test.processText("create", 80);
		test.processText(" src\\app\\c1.ts", 80);
		test.processCarriageReturnLineFeed();
		test.processCarriageReturnLineFeed();
		test.processText("C:\\Users\\azerr\\WS\\abcd\\src\\app>", 80);

		String expected = "" + 
				"SUBMIT: workingDir=C:\\Users\\azerr\\WS\\abcd\\src\\app, command=ng generate class c1  --spec false\n" + 
				"EXECUTING:installing class\n" + 
				"EXECUTING:  \n" +   
				"EXECUTING:create\n" + 
				"EXECUTING: src\\app\\c1.ts\n" + 
				"TERMINATE: workingDir=C:\\Users\\azerr\\WS\\abcd\\src\\app, command=ng generate class c1  --spec false"; 
				
		Assert.assertEquals(expected, test.toString());
	}
	
	@Test
	public void ngLongCommand() {
		TrackerTest test = new TrackerTest("C:\\Users\\azerr\\WS\\aa\\src\\app",
				"C:/Users/azerr/WS/aa/node_modules/.bin/ng.cmd generate class c  --spec false", "C:/Users/azerr");
		test.processText("Microsoft Windows [version 6.1.7601]", 94);
		test.processCarriageReturnLineFeed();
		test.processText("Copyright (c) 2009 Microsoft Corporation. Tous droits réservés.", 94);
		test.processCarriageReturnLineFeed();
		test.processCarriageReturnLineFeed();
		test.processText(
				"C:\\Users\\azerr\\WS\\aa\\src\\app>C:/Users/azerr/WS/aa/node_modules/.bin/ng.cmd generate class c  -",
				94);
		test.processCarriageReturnLineFeed();
		test.processText("-spec false", 94);
		test.processCarriageReturnLineFeed();
		test.processText("installing class", 94);
		test.processCarriageReturnLineFeed();
		test.processText("  ", 94);
		test.processText("create", 94);
		test.processText(" src\\app\\c.ts", 94);
		test.processCarriageReturnLineFeed();
		test.processCarriageReturnLineFeed();
		test.processText("C:\\Users\\azerr\\WS\\aa\\src\\app>", 94);

		String expected = "" + 
				"SUBMIT: workingDir=C:\\Users\\azerr\\WS\\aa\\src\\app, command=C:/Users/azerr/WS/aa/node_modules/.bin/ng.cmd generate class c  --spec false\n" + 
				"EXECUTING:installing class\n" + 
				"EXECUTING:  \n" +   
				"EXECUTING:create\n" + 
				"EXECUTING: src\\app\\c.ts\n" + 
				"TERMINATE: workingDir=C:\\Users\\azerr\\WS\\aa\\src\\app, command=C:/Users/azerr/WS/aa/node_modules/.bin/ng.cmd generate class c  --spec false"; 
				
		Assert.assertEquals(expected, test.toString());		
	}
}
