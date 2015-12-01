# typescript.java

Goal of typescript.java is to provide the capability to consume TypeScript language Service with tsserver in a Java context. Any Java IDE like Eclipse, Netbeans, WebStorm could consume typescript.java.

typescript.java provides :

 * a simple SWT/JFace integration.
 * an Eclipse IDE with JSDT integration. 

# Similar Project

## Java 

See https://github.com/BestSolution-at/java-tsserver which provides the capability to consume tsserevr with Java. So why developping an another tsclient?

 * java-tsserver requires Java8. Eclipse IDE uses Java7.
 * It uses GSON, I prefer using minimal-json and serialize JSON to Pojo/Collector at hand.
 * Uses tsclient API instead of using TypeScript Language Service API.

## Eclipse

 * [Eclipse TypeScript](https://github.com/palantir/eclipse-typescript)
 * [TypeEcs](http://typecsdev.com/)
 
Those plugins provide a lot of advanced features for TypeScript (debug, refactoring, etc) compare to typescript.java. So why developping an another Eclipse Plugins?

Here my idea:

 * use JSDT JavaScript Editor instead of developping custom editor (DONE for completion, hyperlink, hover).
 * use WTP Validator instead of Builder for validation (TODO).
 * consume "official" tsserver instead of consumming custom bridge language service like Eclipse TypeScript/TypeEcs have done:
  * user will able to update the tsserver just with "npm install typescript".
  * other editors consumes "tsserver", so there are a big community which uses "tsserver":
    * [Visual Studio Code](https://code.visualstudio.com/) (Microsoft)
    * [Typescript-Sublime-plugin](https://github.com/Microsoft/Typescript-Sublime-plugin) (Microsoft)
    * [A Vim plugin for TypeScript](https://github.com/Quramy/tsuquyomi)
 * use async "event" of tsserver to improve performance for validation for instance.
 * use async completion, etc
