if (typeof ts === "undefined") {
  ts =  {};
}
var ts;
try {
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
          if (compilerOptions && Object.getOwnPropertyNames(compilerOptions).length > 1) {
            var project = host.project;
            // It exists "compilerOptions", reload tsconfig.json because at this step, compilerOptions/plugins is not available.
            var configFilename = ts.normalizePath(compilerOptions.configFilePath);          
            var configFileContent = host.readFile(configFilename);
            var configJsonObject = ts.parseConfigFileTextToJson(configFilename, configFileContent);
            // get array of compilerOptions/plugins
            var plugins = configJsonObject.config.compilerOptions.plugins;          
            
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
    	
    	function enableProxy(pluginModule, configEntry) {
    		var project = host.project;
    		try {
    			 var info /* PluginCreateInfo*/ = {
                    config: configEntry,
                    project: project,
                    languageService: languageService,
                    languageServiceHost: host,
                    serverHost: project.projectService.host,
                };
    			// Remove that when TypeScript will provide custom codefix registration
    			 // see https://github.com/Microsoft/TypeScript/issues/13435
    			// This code is used by tslint-language-service (codefix.registerCodeFix(action))
    			info.ts = ts;
                if (pluginModule.create === undefined && typeof pluginModule === "function") {
                    // We might get back a top-level factory function instead,
                    // depending on the presence of default exports
                    project.projectService.logger.info("Unwrapping factory function module import");
                    pluginModule = pluginModule(/* { typescript: ts } */);                    
                }
                languageService = pluginModule.create(info);
                // this.plugins.push(pluginModule);
            }
            catch (e) {
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