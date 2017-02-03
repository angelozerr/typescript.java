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
    	// Create Language Service
        var ls = createLanguageService(host, documentRegistry);
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
          } else {
        	// Load plugins
            plugins.forEach(function(plugin) {
              if (plugin.name) {
                try {
                  ls = require(plugin.name)().create({
                    languageServiceHost: host,
                    languageService: ls,
                    project: project,
                    ts: ts    
                  });
                }
                catch(e) {
                  try {
                    ls = require(plugin.name).create({
                        languageServiceHost: host,
                        languageService: ls,
                        project: project,
                        ts: ts    
                    });
                    }
                    catch(e) {
                        console.error(e)
                    }
                }
              }
            });
          }
        }
        return ls;
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