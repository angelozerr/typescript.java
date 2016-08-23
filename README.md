# typescript.java

[![Build Status](https://secure.travis-ci.org/angelozerr/typescript.java.png)](http://travis-ci.org/angelozerr/typescript.java)
[![Eclipse install](https://marketplace.eclipse.org/sites/all/themes/solstice/public/images/marketplace/btn-install.png)](http://marketplace.eclipse.org/marketplace-client-intro?mpc_install=3037133)

Goal of typescript.java is to provide the capability to consume TypeScript language Service with [tsserver](https://github.com/Microsoft/TypeScript/blob/master/bin/tsserver in a Java context. Any Java IDE like Eclipse, Netbeans, WebStorm could consume typescript.java.

## Core Features

Once that TypeScript Service client which consumes tsserver is created with Java using `core` module, you can use it in any Java context (Eclipse, Netbeans, etc). 
typescript.java provides the capability to use TypeScript language Service with tsserver with SWT : 

 * contentassist which uses typescript.java. If you start the SWT [TypeScriptEditor](https://github.com/angelozerr/typescript.java/blob/master/samples/ts.eclipse.swt.samples/src/ts/eclipse/swt/samples/TypeScriptEditor.java) demo, 
you will see contentassist available for JavaScript : 

![SWT TypeScript Editor](https://github.com/angelozerr/typescript.java/wiki/images/SWTTypeScriptEditor.png)

## Eclipse IDE

On top of `core` module typescript.java provides an **Eclipse IDE** integration with TypeScript. 

![JSDT TypeScript Completion](https://github.com/angelozerr/typescript.java/wiki/images/JSDTTypeScriptCompletion.png)  

To install TypeScript IDE, please start to read [Getting Started](https://github.com/angelozerr/typescript.java/wiki/Getting-Started) section.

This integration looks like [tern.java](https://github.com/angelozerr/tern.java); JSDT was extended (completion, hyperlink, hover, etc) to consumme the official TypeScript server 
[tsserver](https://github.com/Microsoft/TypeScript/blob/master/bin/tsserver).

# Similar Project

## Java 

See https://github.com/BestSolution-at/java-tsserver which provides the capability to consume tsserver with Java. So why developping an another tsclient?

 * java-tsserver requires Java8. Eclipse IDE uses Java7.
 * It uses GSON, I prefer using minimal-json and serialize JSON to Pojo/Collector at hand.
 * Uses tsclient API instead of using TypeScript Language Service API.

## Eclipse

 * [Eclipse TypeScript](https://github.com/palantir/eclipse-typescript)
 * [TypeEcs](http://typecsdev.com/)
 
Those plugins provide a lot of advanced features for TypeScript (debug, refactoring, etc) compare to typescript.java. So why developping an another Eclipse Plugins?

Here my idea:

 * use JSDT JavaScript Editor instead of developping custom editor (DONE for completion, hyperlink, hover).
 * use WTP Validator instead of Builder for validation (DONE).
 * consume "official" tsserver instead of consumming custom bridge language service like Eclipse TypeScript/TypeEcs have done:
  * user will able to update the tsserver just with "npm install typescript".
  * other editors consumes "tsserver", so there are a big community which uses "tsserver":
    * [Visual Studio Code](https://code.visualstudio.com/) (Microsoft)
    * [Typescript-Sublime-plugin](https://github.com/Microsoft/Typescript-Sublime-plugin) (Microsoft)
    * [A Vim plugin for TypeScript](https://github.com/Quramy/tsuquyomi)
 * use async "event" of tsserver to improve performance for validation for instance.
 * use async completion, etc
