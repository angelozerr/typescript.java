# Language Service Plugins with Proxies

[Language Service Plugins with Proxies](https://github.com/Microsoft/TypeScript/issues/11976) is planned for the future of TypeScript (TypeScript 2.3 ?). This feature gives the capability to TypeScript tsserver completion, definition, diagnostics, etc with custom plugins. Today it exists 2 plugins :

 * Angular2 language service which provide completion, definition, diagnostics for @Component/template
 * tslint which provides diagnostics, codefix with tslint.
 
When a TypeScript file must be validated by tslint, the tslint language service plugin uses the parsed TypeScript file from tsserver and avoid double parsing of TypeScript file (one by tsserver and one by tslint).

Current work can be found in [this PR](https://github.com/Microsoft/TypeScript/pull/12231)

# What is the goal of tsserver-plugins?

[Language Service Plugins with Proxies](https://github.com/Microsoft/TypeScript/issues/11976)  will be available for a future version of TypeScript (TypeScript 2.3?). For the impatient guys, 
[tsserver-plugins](https://github.com/angelozerr/tsserver-plugins) gives you the capability to consume plugins with old version of TypeScript. Instead of starting tsserver with the [bin/tsserver](https://github.com/Microsoft/TypeScript/blob/master/bin/tsserver) command, you start your tsserver with [bin/tsserver-plugins](https://github.com/angelozerr/tsserver-plugins/bin/tsserver-plugins) which overrides the createLanguageService to load plugins declared in your tsconfig.json like this:

```json
{
  "compilerOptions": {
    "plugins": [
			{ "name": "tslint-language-service"}, 
			{ "name": "@angular/language-service"}
		]
  }
}
```

the [bin/tsserver-plugins](https://github.com/angelozerr/tsserver-plugins/bin/tsserver-plugins) concat the wrapped JS which load the plugins from tsconfig.json with the given tsserver.js filr of your TypeScript node modules.
