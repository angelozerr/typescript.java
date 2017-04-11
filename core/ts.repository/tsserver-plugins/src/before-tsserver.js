if (typeof ts === "undefined") {
  ts =  {};
}
var ts;
try {
  var optionDeclarations;
  Object.defineProperty(ts, "optionDeclarations", {
    get: function() {
	  var pluginDecl =  {
        name: "plugins",
        type: "list",
        isTSConfigOnly: true,
        element: {
            name: "plugin",
            type: "object"
        }
      }
      optionDeclarations.push(pluginDecl);
      return optionDeclarations;
    },
    set: function(v) {
      optionDeclarations = v;
    },
    configurable: true,
    enumerable: true
  });	
  // Wrap ts.createLanguageService to load plugins declared in the tsconfig.json
  // See https://github.com/Microsoft/TypeScript/issues/11976
  var createLanguageService;
  Object.defineProperty(ts, "createLanguageService", {
    get: function() {
      return function(host, documentRegistry) {
    	
    	var languageService = createLanguageService(host, documentRegistry);
    	
    	function enablePlugins() {    		
          // Check if tsconfig.json declared plugins like this:
          // {
          //   "compilerOptions": {
          //    "plugins": [
          //			{ "name": "tslint-language-service"}, 
          //			{ "name": "@angular/language-service"}
          //		]
          //  }
          //}
          //
          var compilerOptions = host.getCompilationSettings();
          if (compilerOptions) {
            var project = host.project;
            // get array of compilerOptions/plugins
            var plugins = compilerOptions.plugins;          
            // TypeScript 2.0.0 defines psLogger and TypeScript 2.1.0 defines logger.
            if (!project.projectService.logger && project.projectService.psLogger) project.projectService.logger = project.projectService.psLogger;
            if (!(plugins && plugins.length)) {                
              project.projectService.logger.info("No plugins exist");
              // No plugins
              return;
            }

            if (!/* host. */require) {
                project.projectService.logger.info("Plugins were requested but not running in environment that supports 'require'. Nothing will be loaded");
                return;
            }
            
            // Load plugins
            plugins.forEach(function(pluginConfigEntry) {
              if (pluginConfigEntry.name) {
                try {
                  var resolvedModule = require(pluginConfigEntry.name);
                  if (resolvedModule) {
                	  enableProxy(resolvedModule, pluginConfigEntry);
                  }
                }
                catch(e) {
                	console.error(e)
                }
              }
            });
          }
    	}
    	
    	function enableProxy(pluginModuleFactory, configEntry) {
    		var project = host.project;
    		try {
    		    if (typeof pluginModuleFactory !== "function") {
                    project.projectService.logger.info("Skipped loading plugin " + configEntry.name + " because it did expose a proper factory function");
                    return;
                }
    			var info /* PluginCreateInfo*/ = {
                    config: configEntry,
                    project: project,
                    languageService: languageService,
                    languageServiceHost: host,
                    serverHost: project.projectService.host,
                };
    			var pluginModule = pluginModuleFactory({ typescript: ts });
                languageService = pluginModule.create(info);
                // this.plugins.push(pluginModule);
            }
            catch (e) {
            	console.error(e)
                project.projectService.logger.info("Plugin activation failed: " + e);
                if (typeof pluginModule === "object") {
                    project.projectService.logger.info("Plugin top-level keys: " + Object.keys(pluginModule));
                }
                else {
                    project.projectService.logger.info("Plugin load result was: " + pluginModule);
                }
            }
    	}
    	
    	enablePlugins();
    	// Returns Language Service
    	return languageService;
    	
      }
    },
    set: function(v) {
      createLanguageService = v;
    },
    configurable: true,
    enumerable: true
  });
} catch(e) {
	console.error(e)
 }