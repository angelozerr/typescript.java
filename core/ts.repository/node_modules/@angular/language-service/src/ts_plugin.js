/**
 * @license
 * Copyright Google Inc. All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license that can be
 * found in the LICENSE file at https://angular.io/license
 */
import * as ts from 'typescript';
import { createLanguageService } from './language_service';
import { TypeScriptServiceHost } from './typescript_host';
export function create(info /* ts.server.PluginCreateInfo */) {
    // Create the proxy
    var proxy = Object.create(null);
    var oldLS = info.languageService;
    var _loop_1 = function(k) {
        proxy[k] = function () { return oldLS[k].apply(oldLS, arguments); };
    };
    for (var k in oldLS) {
        _loop_1(k);
    }
    function completionToEntry(c) {
        return { kind: c.kind, name: c.name, sortText: c.sort, kindModifiers: '' };
    }
    function diagnosticToDiagnostic(d, file) {
        return {
            file: file,
            start: d.span.start,
            length: d.span.end - d.span.start,
            messageText: d.message,
            category: ts.DiagnosticCategory.Error,
            code: 0
        };
    }
    function tryOperation(attempting, callback) {
        try {
            callback();
        }
        catch (e) {
            info.project.projectService.logger.info("Failed to " + attempting + ": " + e.toString());
            info.project.projectService.logger.info("Stack trace: " + e.stack);
        }
    }
    var serviceHost = new TypeScriptServiceHost(info.languageServiceHost, info.languageService);
    var ls = createLanguageService(serviceHost);
    serviceHost.setSite(ls);
    proxy.getCompletionsAtPosition = function (fileName, position) {
        var base = oldLS.getCompletionsAtPosition(fileName, position);
        tryOperation('get completions', function () {
            var results = ls.getCompletionsAt(fileName, position);
            if (results && results.length) {
                if (base === undefined) {
                    base = { isMemberCompletion: false, isNewIdentifierLocation: false, entries: [] };
                }
                for (var _i = 0, results_1 = results; _i < results_1.length; _i++) {
                    var entry = results_1[_i];
                    base.entries.push(completionToEntry(entry));
                }
            }
        });
        return base;
    };
    proxy.getQuickInfoAtPosition = function (fileName, position) {
        var base = oldLS.getQuickInfoAtPosition(fileName, position);
        tryOperation('get quick info', function () {
            var ours = ls.getHoverAt(fileName, position);
            if (ours) {
                var displayParts = [];
                for (var _i = 0, _a = ours.text; _i < _a.length; _i++) {
                    var part = _a[_i];
                    displayParts.push({ kind: part.language, text: part.text });
                }
                base = {
                    displayParts: displayParts,
                    documentation: [],
                    kind: 'angular',
                    kindModifiers: 'what does this do?',
                    textSpan: { start: ours.span.start, length: ours.span.end - ours.span.start }
                };
            }
        });
        return base;
    };
    proxy.getSemanticDiagnostics = function (fileName) {
        var base = oldLS.getSemanticDiagnostics(fileName);
        if (base === undefined) {
            base = [];
        }
        tryOperation('get diagnostics', function () {
            info.project.projectService.logger.info("Computing Angular semantic diagnostics...");
            var ours = ls.getDiagnostics(fileName);
            if (ours && ours.length) {
                var file_1 = oldLS.getProgram().getSourceFile(fileName);
                base.push.apply(base, ours.map(function (d) { return diagnosticToDiagnostic(d, file_1); }));
            }
        });
        return base;
    };
    proxy.getDefinitionAtPosition = function (fileName, position) {
        var base = oldLS.getDefinitionAtPosition(fileName, position);
        if (base && base.length) {
            return base;
        }
        tryOperation('get definition', function () {
            var ours = ls.getDefinitionAt(fileName, position);
            if (ours && ours.length) {
                base = base || [];
                for (var _i = 0, ours_1 = ours; _i < ours_1.length; _i++) {
                    var loc = ours_1[_i];
                    base.push({
                        fileName: loc.fileName,
                        textSpan: { start: loc.span.start, length: loc.span.end - loc.span.start },
                        name: '',
                        kind: 'definition',
                        containerName: loc.fileName,
                        containerKind: 'file'
                    });
                }
            }
        });
        return base;
    };
    return proxy;
}
//# sourceMappingURL=ts_plugin.js.map