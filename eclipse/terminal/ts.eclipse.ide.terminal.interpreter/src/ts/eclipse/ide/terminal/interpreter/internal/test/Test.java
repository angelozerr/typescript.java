package ts.eclipse.ide.terminal.interpreter.internal.test;

import java.util.Arrays;

public class Test {

//	void test() {
//		test.processLines(Arrays.asList(), false);
//		test.processLines(Arrays.asList(), true);
//		test.processLines(Arrays.asList("Microsoft Windows [version 6.1.7601]", "Copyright (c) 2009 Microsoft Corporation. Tous droits réservés.", "D:\_Personal\runtime-EclipseApplicationDemoTerminal\NewCli2\src\app>ng generate class a  --spec false", "You are running version 4.4.0 of Node, which will not be supported in future", "versions of the CLI. The official Node version that will be supported is 6.9 and greater.", "To disable this warning use "ng set --global warnings.nodeDeprecation=false".", "As a forewarning, we are moving the CLI npm package to \"@angular/cli\" with the next release,", "which will only support Node 6.9 and greater. This package will be officially deprecated", "shortly after.", "To disable this warning use "ng set --global warnings.packageDeprecation=false".", "installing class", "  ", "identical", " src\app\a.ts", "D:\_Personal\runtime-EclipseApplicationDemoTerminal\NewCli2\src\app>"), false);
//	}
//	
//	void test2() {
//		test.processLines(Arrays.asList(), true);
//		test.processLines(Arrays.asList("Microsoft Windows [version 6.1.7601]", "Copyright (c) 2009 Microsoft Corporation. Tous droits réservés."), false);
//		test.processLines(Arrays.asList("D:\\_Personal\\runtime-EclipseApplicationDemoTerminal>ng generate class aaaa  --spec false"), false);
//		test.processLines(Arrays.asList(), false);
//		test.processLines(Arrays.asList("You are running version 4.4.0 of Node, which will not be supported in future", "versions of the CLI. The official Node version that will be supported is 6.9 and greater.", "To disable this warning use "ng set --global warnings.nodeDeprecation=false".", "As a forewarning, we are moving the CLI npm package to "@angular/cli" with the next release,", "which will only support Node 6.9 and greater. This package will be officially deprecated", "shortly after.", "To disable this warning use "ng set --global warnings.packageDeprecation=false"."), false);
//		test.processLines(Arrays.asList("installing class"), false);
//		test.processLines(Arrays.asList("  ", "create", " src\app\aaaa.ts"), false);
//		test.processLines(Arrays.asList("D:\_Personal\runtime-EclipseApplicationDemoTerminal\NewCli2\src\app>"), false);
//	}
//	
//	void test3() {
//		test.processLines(Arrays.asList(), true);
//		test.processLines(Arrays.asList(), false);
//		test.processLines(Arrays.asList("Microsoft Windows [version 6.1.7601]", "Copyright (c) 2009 Microsoft Corporation. Tous droits réservés.", "D:\_Personal\runtime-EclipseApplicationDemoTerminal\NewCli2\src\app>ng generate class aaaaa  --spec false"), false);
//		test.processLines(Arrays.asList(), false);
//		test.processLines(Arrays.asList("You are running version 4.4.0 of Node, which will not be supported in future", "versions of the CLI. The official Node version that will be supported is 6.9 and greater.", "To disable this warning use "ng set --global warnings.nodeDeprecation=false".", "As a forewarning, we are moving the CLI npm package to "@angular//cli" with the next release,", "which will only support Node 6.9 and greater. This package will be officially deprecated", "shortly after.", "To disable this warning use "ng set --global warnings.packageDeprecation=false"."), false);
//		test.processLines(Arrays.asList("installing class"), false);
//		test.processLines(Arrays.asList("  ", "create", " src\app\aaaaa.ts"), false);
//		test.processLines(Arrays.asList("D:\_Personal\runtime-EclipseApplicationDemoTerminal\NewCli2\src\app>"), false);
//
//	}
	
	public static void main(String[] args) {
		test6();
	}
	
	static void test4() {
		TestCommandProcessor test = new TestCommandProcessor("D:\\_Personal\\runtime-EclipseApplicationDemoTerminal\\NewCli2\\src\\app", "ng generate class p  --spec false");
		test.processLines(Arrays.asList(), true);
		test.processLines(Arrays.asList(), false);
		test.processLines(Arrays.asList("Microsoft Windows [version 6.1.7601]", "Copyright (c) 2009 Microsoft Corporation. Tous droits réservés.", "D:\\_Personal\\runtime-EclipseApplicationDemoTerminal\\NewCli2\\src\\app>ng generate class p  --spec false"), false);
		test.processLines(Arrays.asList(), false);
		test.processLines(Arrays.asList("You are running version 4.4.0 of Node, which will not be supported in future", "versions of the CLI. The official Node version that will be supported is 6.9 and greater.", "To disable this warning use \"ng set --global warnings.nodeDeprecation=false\".", "As a forewarning, we are moving the CLI npm package to \"@angular/cli\" with the next release,", "which will only support Node 6.9 and greater. This package will be officially deprecated", "shortly after.", "To disable this warning use \"ng set --global warnings.packageDeprecation=false\"."), false);
		test.processLines(Arrays.asList("installing class", "  ", "create", " src\\app\\p.ts"), false);
		test.processLines(Arrays.asList("D:\\_Personal\\runtime-EclipseApplicationDemoTerminal\\NewCli2\\src\\app>"), false);
	}
	
	static void test5() {
		TestCommandProcessor test = new TestCommandProcessor("D:\\_Personal\\runtime-EclipseApplicationDemoTerminal\\NewCli2\\src\\app", "g generate class b  --spec false");
		test.processLines(Arrays.asList(), true);
		test.processLines(Arrays.asList(), false);
		test.processLines(Arrays.asList("Microsoft Windows [version 6.1.7601]", "Copyright (c) 2009 Microsoft Corporation. Tous droits réservés.", "D:\\_Personal\\runtime-EclipseApplicationDemoTerminal\\NewCli2\\src\\app>ng generate class b  --spec false"), false);
		test.processLines(Arrays.asList(), false);
		test.processLines(Arrays.asList("You are running version 4.4.0 of Node, which will not be supported in future", "versions of the CLI. The official Node version that will be supported is 6.9 and greater.", "To disable this warning use \"ng set --global warnings.nodeDeprecation=false\"."), false);
		test.processLines(Arrays.asList("As a forewarning, we are moving the CLI npm package to \"@angular/cli\" with the next release,", "which will only support Node 6.9 and greater. This package will be officially deprecated", "shortly after.", "To disable this warning use \"ng set --global warnings.packageDeprecation=false\"."), false);
		test.processLines(Arrays.asList("installing class"), false);
		test.processLines(Arrays.asList("  ", "create", " src\\app\\b.ts"), false);
		test.processLines(Arrays.asList("D:\\_Personal\\runtime-EclipseApplicationDemoTerminal\\NewCli2\\src\\app>"), false);
	}
	
	static void test6() {
		TestCommandProcessor test = new TestCommandProcessor("D:\\_Personal\\runtime-EclipseApplicationDemoTerminal\\NewCli2\\src\\app\\m", "cd D:\\_Personal\\runtime-EclipseApplicationDemoTerminal\\NewCli2\\src\\app");
		test.processLines(Arrays.asList(), true);
		test.processLines(Arrays.asList("D:\\_Personal\\runtime-EclipseApplicationDemoTerminal\\NewCli2\\src\\app\\m>cd D:\\_Personal\\runtime-EclipseApplicationDemoTerminal\\NewCli2\\src\\app", "D:\\_Personal\\runtime-EclipseApplicationDemoTerminal\\NewCli2\\src\\app>ng generate class oo  --spec false"), false);
		test.processLines(Arrays.asList("installing class", "  ", "create", " src\\app\\oo.ts", "D:\\_Personal\\runtime-EclipseApplicationDemoTerminal\\NewCli2\\src\\app>"), false);

	}
}
