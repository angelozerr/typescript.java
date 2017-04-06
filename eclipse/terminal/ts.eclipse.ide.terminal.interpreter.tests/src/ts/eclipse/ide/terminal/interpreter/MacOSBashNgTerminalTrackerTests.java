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
import org.junit.Ignore;
import org.junit.Test;

/**
 * Mac OS bash tracker test.
 *
 */
public class MacOSBashNgTerminalTrackerTests {

	@Test
	@Ignore
	public void ngClass() {
		TrackerTest test = new TrackerTest();
		//new TrackerTest("C:/Users/azerr/Documents/mon-workspace/angular2-200/src/app",
		//		"ng generate class pascalou  --spec false", "C:/Users/azerr");
		test.processText("bash-3.2$ PS1='\\w\\$ '", 80);
		test.processCarriageReturnLineFeed();
		test.processText("ng generate class pascalou  --spec false", 80);
		test.processCarriageReturnLineFeed();
		test.processText("~/Documents/mon-workspace/angular2-200/src/app$ ng generate class pascalou  --sp ", 80);
		test.processText("ec false", 80);
		test.processCarriageReturnLineFeed();
		test.processText("installing class", 80);
		test.processCarriageReturnLineFeed();
		test.processText("  ", 80);
		test.processText("create", 80);
		test.processText(" src/app/pascalou.ts", 80);
		test.processCarriageReturnLineFeed();
		test.processText("~/Documents/mon-workspace/angular2-200/src/app$ ", 80);

		String expected = ""
				+ "SUBMIT: workingDir=C:/Users/azerr/Documents/mon-workspace/angular2-200/src/app, command=ng generate class pascalou  --spec false\n"
				+ "EXECUTING:installing class\n" 
				+ "EXECUTING:  \n" 
				+ "EXECUTING:create\n"
				+ "EXECUTING: src/app/pascalou.ts\n"
				+ "TERMINATE: workingDir=C:/Users/azerr/Documents/mon-workspace/angular2-200/src/app, command=ng generate class pascalou  --spec false";

		Assert.assertEquals(expected, test.toString());
	}

	@Test
	@Ignore
	public void ngClass2() {
		//TrackerTest test = new TrackerTest("C:/Users/azerr/Documents/mon-workspace/angular2-200/src/app",
		//		"ng generate class sample  --spec false", "C:/Users/azerr");
		TrackerTest test = new TrackerTest();
		test.processText("bash-3.2$ ", 80);
		test.processText("PS1='\\w\\$ '", 80);
		test.processCarriageReturnLineFeed();
		test.processText("ng generate class sample  --spec false", 80);
		test.processCarriageReturnLineFeed();
		test.processText("~/Documents/mon-workspace/angular2-200/src/app$ ng generate class sample  --spec ", 80);
		test.processText(" false", 80);
		test.processCarriageReturnLineFeed();
		test.processText("installing class", 80);
		test.processCarriageReturnLineFeed();
		test.processText("  ", 80);
		test.processText("create", 80);
		test.processText(" src/app/sample.ts", 80);
		test.processCarriageReturnLineFeed();
		test.processText("~/Documents/mon-workspace/angular2-200/src/app$ ", 80);

		String expected = ""
				+ "SUBMIT: workingDir=C:/Users/azerr/Documents/mon-workspace/angular2-200/src/app, command=ng generate class sample  --spec false\n"
				+ "EXECUTING:installing class\n" 
				+ "EXECUTING:  \n" 
				+ "EXECUTING:create\n"
				+ "EXECUTING: src/app/sample.ts\n"
				+ "TERMINATE: workingDir=C:/Users/azerr/Documents/mon-workspace/angular2-200/src/app, command=ng generate class sample  --spec false";

		Assert.assertEquals(expected, test.toString());

	}
	
	@Test
	@Ignore
	public void t() {
		//TrackerTest test = new TrackerTest("/Users/pascalleclercq/Documents/mon-workspace/angular2-200/src/app", "ng generate class example  --spec false", "/Users/pascalleclercq");
		TrackerTest test = new TrackerTest();
		test.processText("PS1='\\w\\", 80);
		test.processText("$ '", 80);
		test.processCarriageReturnLineFeed();
		test.processCarriageReturnLineFeed();
		test.processText("ng generate class example  --spec false", 80);
		test.processCarriageReturnLineFeed();
		test.processCarriageReturnLineFeed();
		test.processText("bash-3.2$ PS1='\\w\\$ '", 80);
		test.processCarriageReturnLineFeed();
		test.processText("~/Documents/mon-workspace/angular2-200/src/app$ ", 80);
		test.processCarriageReturnLineFeed();
		test.processText("~/Documents/mon-workspace/angular2-200/src/app$ ng generate class example  --spe ", 150);
		test.processText("c false", 150);
		test.processCarriageReturnLineFeed();
		test.processCarriageReturnLineFeed();
		test.processText("installing class", 150);
		test.processCarriageReturnLineFeed();
		test.processText("  ", 150);
		test.processText("create", 150);
		test.processText(" src/app/example.ts", 150);
		test.processCarriageReturnLineFeed();
		test.processText("~/Documents/mon-workspace/angular2-200/src/app$ ", 150);
		test.processCarriageReturnLineFeed();
		test.processText("~/Documents/mon-workspace/angular2-200/src/app$ ", 150);

		String expected = ""
				+ "SUBMIT: workingDir=/Users/pascalleclercq/Documents/mon-workspace/angular2-200/src/app, command=ng generate class example  --spec false\n"
				+ "EXECUTING:installing class\n" 
				+ "EXECUTING:  \n" 
				+ "EXECUTING:create\n"
				+ "EXECUTING: src/app/example.ts\n"
				+ "TERMINATE: workingDir=/Users/pascalleclercq/Documents/mon-workspace/angular2-200/src/app, command=ng generate class example  --spec false\n"
				+ "SUBMIT: workingDir=/Users/pascalleclercq/Documents/mon-workspace/angular2-200/src/app, command=null\n"
				+ "TERMINATE: workingDir=/Users/pascalleclercq/Documents/mon-workspace/angular2-200/src/app, command=null";
		System.err.println(test);
		Assert.assertEquals(expected, test.toString());
		
	}
}
