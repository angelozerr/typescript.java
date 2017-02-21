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

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

import ts.eclipse.ide.terminal.interpreter.internal.CommandTerminalTracker;

/**
 * Win32 tracker test.	
 *
 */
public class Win32CommandTerminalTrackerTests {

	@Test
	public void cdDot() {
		CommandTerminalTracker test = new TrackerTest("D:\\Logiciels\\eclipses\\eclipse-jee-neon-2-win32-x86_64\\eclipse", null);
		test.processLines(Arrays.asList(), false);
		test.processLines(Arrays.asList("Microsoft Windows [version 6.1.7601]", "Copyright (c) 2009 Microsoft Corporation. Tous droits réservés.", "D:\\Logiciels\\eclipses\\eclipse-jee-neon-2-win32-x86_64\\eclipse>"), false);
		test.processLines(Arrays.asList("D:\\Logiciels\\eclipses\\eclipse-jee-neon-2-win32-x86_64\\eclipse>c"), false);
		test.processLines(Arrays.asList("D:\\Logiciels\\eclipses\\eclipse-jee-neon-2-win32-x86_64\\eclipse>cd"), false);
		test.processLines(Arrays.asList(), false);
		test.processLines(Arrays.asList("D:\\Logiciels\\eclipses\\eclipse-jee-neon-2-win32-x86_64\\eclipse>cd ."), false);
		test.processLines(Arrays.asList(), true);
		test.processLines(Arrays.asList("D:\\Logiciels\\eclipses\\eclipse-jee-neon-2-win32-x86_64\\eclipse>"), false);

		String expected = "" + 
				"SUBMIT: workingDir=D:\\Logiciels\\eclipses\\eclipse-jee-neon-2-win32-x86_64\\eclipse, command=cd .\n" + 
				"TERMINATE: workingDir=D:\\Logiciels\\eclipses\\eclipse-jee-neon-2-win32-x86_64\\eclipse, command=cd .";
		Assert.assertEquals(expected, test.toString());
	}

	@Test
	public void cdWithDirectoryChanged() {
		CommandTerminalTracker test = new TrackerTest("D:\\Logiciels\\eclipses\\eclipse-jee-neon-2-win32-x86_64\\eclipse", null);
		// Open Terminal
		test.processLines(Arrays.asList(), false);
		test.processLines(Arrays.asList("Microsoft Windows [version 6.1.7601]", "Copyright (c) 2009 Microsoft Corporation. Tous droits réservés.", "D:\\Logiciels\\eclipses\\eclipse-jee-neon-2-win32-x86_64\\eclipse>"), false);
		// Use types "cd .."
		test.processLines(Arrays.asList("D:\\Logiciels\\eclipses\\eclipse-jee-neon-2-win32-x86_64\\eclipse>c"), false);
		test.processLines(Arrays.asList("D:\\Logiciels\\eclipses\\eclipse-jee-neon-2-win32-x86_64\\eclipse>cd"), false);
		test.processLines(Arrays.asList(), false);
		test.processLines(Arrays.asList("D:\\Logiciels\\eclipses\\eclipse-jee-neon-2-win32-x86_64\\eclipse>cd ."), false);
		test.processLines(Arrays.asList("D:\\Logiciels\\eclipses\\eclipse-jee-neon-2-win32-x86_64\\eclipse>cd .."), false);
		// User type "Enter"
		test.processLines(Arrays.asList(), true);
		test.processLines(Arrays.asList("D:\\Logiciels\\eclipses\\eclipse-jee-neon-2-win32-x86_64>"), false);
		// User types "cd eclipse"
		test.processLines(Arrays.asList("D:\\Logiciels\\eclipses\\eclipse-jee-neon-2-win32-x86_64>c"), false);
		test.processLines(Arrays.asList("D:\\Logiciels\\eclipses\\eclipse-jee-neon-2-win32-x86_64>cd"), false);
		test.processLines(Arrays.asList(), false);
		test.processLines(Arrays.asList("D:\\Logiciels\\eclipses\\eclipse-jee-neon-2-win32-x86_64>cd e"), false);
		test.processLines(Arrays.asList("D:\\Logiciels\\eclipses\\eclipse-jee-neon-2-win32-x86_64>cd ec"), false);
		test.processLines(Arrays.asList("D:\\Logiciels\\eclipses\\eclipse-jee-neon-2-win32-x86_64>cd ecl"), false);
		test.processLines(Arrays.asList("D:\\Logiciels\\eclipses\\eclipse-jee-neon-2-win32-x86_64>cd ecli"), false);
		test.processLines(Arrays.asList("D:\\Logiciels\\eclipses\\eclipse-jee-neon-2-win32-x86_64>cd eclip"), false);
		test.processLines(Arrays.asList("D:\\Logiciels\\eclipses\\eclipse-jee-neon-2-win32-x86_64>cd eclips"), false);
		test.processLines(Arrays.asList("D:\\Logiciels\\eclipses\\eclipse-jee-neon-2-win32-x86_64>cd eclipse"), false);
		// User type "Enter"
		test.processLines(Arrays.asList(), true);
		test.processLines(Arrays.asList("D:\\Logiciels\\eclipses\\eclipse-jee-neon-2-win32-x86_64\\eclipse>"), false);

		String expected = "" + 
		"SUBMIT: workingDir=D:\\Logiciels\\eclipses\\eclipse-jee-neon-2-win32-x86_64\\eclipse, command=cd ..\n" + 
		"TERMINATE: workingDir=D:\\Logiciels\\eclipses\\eclipse-jee-neon-2-win32-x86_64, command=cd ..\n" + 
		"SUBMIT: workingDir=D:\\Logiciels\\eclipses\\eclipse-jee-neon-2-win32-x86_64, command=cd eclipse\n" + 
		"TERMINATE: workingDir=D:\\Logiciels\\eclipses\\eclipse-jee-neon-2-win32-x86_64\\eclipse, command=cd eclipse";
		
		Assert.assertEquals(expected, test.toString());
	}
	
	@Test
	public void ngClass() {
		CommandTerminalTracker test = new TrackerTest("D:\\_Personal\\runtime-EclipseApplicationDemoTerminal\\NewCli2\\src\\app", "ng generate class p  --spec false");
		test.processLines(Arrays.asList(), true);
		test.processLines(Arrays.asList(), false);
		test.processLines(Arrays.asList("Microsoft Windows [version 6.1.7601]", "Copyright (c) 2009 Microsoft Corporation. Tous droits réservés.", "D:\\_Personal\\runtime-EclipseApplicationDemoTerminal\\NewCli2\\src\\app>ng generate class p  --spec false"), false);
		test.processLines(Arrays.asList(), false);
		test.processLines(Arrays.asList("You are running version 4.4.0 of Node, which will not be supported in future", "versions of the CLI. The official Node version that will be supported is 6.9 and greater.", "To disable this warning use \"ng set --global warnings.nodeDeprecation=false\".", "As a forewarning, we are moving the CLI npm package to \"@angular/cli\" with the next release,", "which will only support Node 6.9 and greater. This package will be officially deprecated", "shortly after.", "To disable this warning use \"ng set --global warnings.packageDeprecation=false\"."), false);
		test.processLines(Arrays.asList("installing class", "  ", "create", " src\\app\\p.ts"), false);
		test.processLines(Arrays.asList("D:\\_Personal\\runtime-EclipseApplicationDemoTerminal\\NewCli2\\src\\app>"), false);
		
		String expected = "" + 
				"SUBMIT: workingDir=D:\\_Personal\\runtime-EclipseApplicationDemoTerminal\\NewCli2\\src\\app, command=ng generate class p  --spec false\n" + 
				"EXECUTING:You are running version 4.4.0 of Node, which will not be supported in future\n" + 
				"EXECUTING:versions of the CLI. The official Node version that will be supported is 6.9 and greater.\n" + 
				"EXECUTING:To disable this warning use \"ng set --global warnings.nodeDeprecation=false\".\n" + 
				"EXECUTING:As a forewarning, we are moving the CLI npm package to \"@angular/cli\" with the next release,\n" + 
				"EXECUTING:which will only support Node 6.9 and greater. This package will be officially deprecated\n" + 
				"EXECUTING:shortly after.\n" + 
				"EXECUTING:To disable this warning use \"ng set --global warnings.packageDeprecation=false\".\n" + 
				"EXECUTING:installing class\n" + 
				"EXECUTING:  \n" +   
				"EXECUTING:create\n" + 
				"EXECUTING: src\\app\\p.ts\n" + 
				"TERMINATE: workingDir=D:\\_Personal\\runtime-EclipseApplicationDemoTerminal\\NewCli2\\src\\app, command=ng generate class p  --spec false"; 
				
				Assert.assertEquals(expected, test.toString());
	}
	
	@Test
	public void ngTwoClasses() {
		CommandTerminalTracker test = new TrackerTest("C:\\Users\\azerr\\WS\\a2\\src\\app", "ng generate class c6  --spec true");
		test.processLines(Arrays.asList(), true);
		test.processLines(Arrays.asList(), false);
		test.processLines(Arrays.asList("Microsoft Windows [version 6.1.7601]", "Copyright (c) 2009 Microsoft Corporation. Tous droits réservés.", "C:\\Users\\azerr\\WS\\a2\\src\\app>ng generate class c6  --spec true"), false);
		test.processLines(Arrays.asList(), false);
		test.processLines(Arrays.asList("installing class", "  ", "create", " src\\app\\c6.spec.ts", "  ", "create", " src\\app\\c6.ts"), false);
		test.processLines(Arrays.asList("C:\\Users\\azerr\\WS\\a2\\src\\app>"), false);
		test.processLines(Arrays.asList("C:\\Users\\azerr\\WS\\a2\\src\\app>cd C:\\Users\\azerr\\WS\\a2\\src\\app\\about"), true);
		test.processLines(Arrays.asList("C:\\Users\\azerr\\WS\\a2\\src\\app\\about>ng generate class c8  --spec false"), false);
		test.processLines(Arrays.asList(), false);
		test.processLines(Arrays.asList("installing class", "  ", "create", " src\\app\\about\\c8.ts"), false);
		test.processLines(Arrays.asList("C:\\Users\\azerr\\WS\\a2\\src\\app\\about>"), false);
		
		System.err.println(test);
	}
}
