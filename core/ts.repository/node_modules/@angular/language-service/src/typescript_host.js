/**
 * @license
 * Copyright Google Inc. All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license that can be
 * found in the LICENSE file at https://angular.io/license
 */
var __extends = (this && this.__extends) || function (d, b) {
    for (var p in b) if (b.hasOwnProperty(p)) d[p] = b[p];
    function __() { this.constructor = d; }
    d.prototype = b === null ? Object.create(b) : (__.prototype = b.prototype, new __());
};
import { AotSummaryResolver, CompilerConfig, StaticReflector, StaticSymbolCache, StaticSymbolResolver, componentModuleUrl, createOfflineCompileUrlResolver } from '@angular/compiler';
import { analyzeNgModules, extractProgramSymbols } from '@angular/compiler/src/aot/compiler';
import { DirectiveNormalizer } from '@angular/compiler/src/directive_normalizer';
import { DirectiveResolver } from '@angular/compiler/src/directive_resolver';
import { CompileMetadataResolver } from '@angular/compiler/src/metadata_resolver';
import { HtmlParser } from '@angular/compiler/src/ml_parser/html_parser';
import { DEFAULT_INTERPOLATION_CONFIG } from '@angular/compiler/src/ml_parser/interpolation_config';
import { ParseTreeResult } from '@angular/compiler/src/ml_parser/parser';
import { NgModuleResolver } from '@angular/compiler/src/ng_module_resolver';
import { PipeResolver } from '@angular/compiler/src/pipe_resolver';
import { ResourceLoader } from '@angular/compiler/src/resource_loader';
import { DomElementSchemaRegistry } from '@angular/compiler/src/schema/dom_element_schema_registry';
import { SummaryResolver } from '@angular/compiler/src/summary_resolver';
import { ViewEncapsulation } from '@angular/core';
import * as fs from 'fs';
import * as path from 'path';
import * as ts from 'typescript';
import { createLanguageService } from './language_service';
import { ReflectorHost } from './reflector_host';
import { BuiltinType } from './types';
// In TypeScript 2.1 these flags moved
// These helpers work for both 2.0 and 2.1.
var isPrivate = ts.ModifierFlags ?
    (function (node) {
        return !!(ts.getCombinedModifierFlags(node) & ts.ModifierFlags.Private);
    }) :
    (function (node) { return !!(node.flags & ts.NodeFlags.Private); });
var isReferenceType = ts.ObjectFlags ?
    (function (type) {
        return !!(type.flags & ts.TypeFlags.Object &&
            type.objectFlags & ts.ObjectFlags.Reference);
    }) :
    (function (type) { return !!(type.flags & ts.TypeFlags.Reference); });
/**
 * Create a `LanguageServiceHost`
 */
export function createLanguageServiceFromTypescript(host, service) {
    var ngHost = new TypeScriptServiceHost(host, service);
    var ngServer = createLanguageService(ngHost);
    ngHost.setSite(ngServer);
    return ngServer;
}
/**
 * The language service never needs the normalized versions of the metadata. To avoid parsing
 * the content and resolving references, return an empty file. This also allows normalizing
 * template that are syntatically incorrect which is required to provide completions in
 * syntatically incorrect templates.
 */
export var DummyHtmlParser = (function (_super) {
    __extends(DummyHtmlParser, _super);
    function DummyHtmlParser() {
        _super.call(this);
    }
    DummyHtmlParser.prototype.parse = function (source, url, parseExpansionForms, interpolationConfig) {
        if (parseExpansionForms === void 0) { parseExpansionForms = false; }
        if (interpolationConfig === void 0) { interpolationConfig = DEFAULT_INTERPOLATION_CONFIG; }
        return new ParseTreeResult([], []);
    };
    return DummyHtmlParser;
}(HtmlParser));
/**
 * Avoid loading resources in the language servcie by using a dummy loader.
 */
export var DummyResourceLoader = (function (_super) {
    __extends(DummyResourceLoader, _super);
    function DummyResourceLoader() {
        _super.apply(this, arguments);
    }
    DummyResourceLoader.prototype.get = function (url) { return Promise.resolve(''); };
    return DummyResourceLoader;
}(ResourceLoader));
/**
 * An implemntation of a `LanguageSerivceHost` for a TypeScript project.
 *
 * The `TypeScriptServiceHost` implements the Angular `LanguageServiceHost` using
 * the TypeScript language services.
 *
 * @expermental
 */
export var TypeScriptServiceHost = (function () {
    function TypeScriptServiceHost(host, tsService) {
        this.host = host;
        this.tsService = tsService;
        this._staticSymbolCache = new StaticSymbolCache();
        this._typeCache = [];
        this.modulesOutOfDate = true;
    }
    TypeScriptServiceHost.prototype.setSite = function (service) { this.service = service; };
    Object.defineProperty(TypeScriptServiceHost.prototype, "resolver", {
        /**
         * Angular LanguageServiceHost implementation
         */
        get: function () {
            var _this = this;
            this.validate();
            var result = this._resolver;
            if (!result) {
                var moduleResolver = new NgModuleResolver(this.reflector);
                var directiveResolver = new DirectiveResolver(this.reflector);
                var pipeResolver = new PipeResolver(this.reflector);
                var elementSchemaRegistry = new DomElementSchemaRegistry();
                var resourceLoader = new DummyResourceLoader();
                var urlResolver = createOfflineCompileUrlResolver();
                var htmlParser = new DummyHtmlParser();
                // This tracks the CompileConfig in codegen.ts. Currently these options
                // are hard-coded except for genDebugInfo which is not applicable as we
                // never generate code.
                var config = new CompilerConfig({
                    genDebugInfo: false,
                    defaultEncapsulation: ViewEncapsulation.Emulated,
                    logBindingUpdate: false,
                    useJit: false
                });
                var directiveNormalizer = new DirectiveNormalizer(resourceLoader, urlResolver, htmlParser, config);
                result = this._resolver = new CompileMetadataResolver(moduleResolver, directiveResolver, pipeResolver, new SummaryResolver(), elementSchemaRegistry, directiveNormalizer, this._staticSymbolCache, this.reflector, function (error, type) { return _this.collectError(error, type && type.filePath); });
            }
            return result;
        },
        enumerable: true,
        configurable: true
    });
    TypeScriptServiceHost.prototype.getTemplateReferences = function () {
        this.ensureTemplateMap();
        return this.templateReferences;
    };
    TypeScriptServiceHost.prototype.getTemplateAt = function (fileName, position) {
        var sourceFile = this.getSourceFile(fileName);
        if (sourceFile) {
            this.context = sourceFile.fileName;
            var node = this.findNode(sourceFile, position);
            if (node) {
                return this.getSourceFromNode(fileName, this.host.getScriptVersion(sourceFile.fileName), node);
            }
        }
        else {
            this.ensureTemplateMap();
            // TODO: Cannocalize the file?
            var componentType = this.fileToComponent.get(fileName);
            if (componentType) {
                return this.getSourceFromType(fileName, this.host.getScriptVersion(fileName), componentType);
            }
        }
    };
    TypeScriptServiceHost.prototype.getAnalyzedModules = function () {
        this.validate();
        return this.ensureAnalyzedModules();
    };
    TypeScriptServiceHost.prototype.ensureAnalyzedModules = function () {
        var analyzedModules = this.analyzedModules;
        if (!analyzedModules) {
            var analyzeHost = { isSourceFile: function (filePath) { return true; } };
            var programSymbols = extractProgramSymbols(this.staticSymbolResolver, this.program.getSourceFiles().map(function (sf) { return sf.fileName; }), analyzeHost);
            analyzedModules = this.analyzedModules =
                analyzeNgModules(programSymbols, analyzeHost, this.resolver);
        }
        return analyzedModules;
    };
    TypeScriptServiceHost.prototype.getTemplates = function (fileName) {
        var _this = this;
        this.ensureTemplateMap();
        var componentType = this.fileToComponent.get(fileName);
        if (componentType) {
            var templateSource = this.getTemplateAt(fileName, 0);
            if (templateSource) {
                return [templateSource];
            }
        }
        else {
            var version_1 = this.host.getScriptVersion(fileName);
            var result_1 = [];
            // Find each template string in the file
            var visit_1 = function (child) {
                var templateSource = _this.getSourceFromNode(fileName, version_1, child);
                if (templateSource) {
                    result_1.push(templateSource);
                }
                else {
                    ts.forEachChild(child, visit_1);
                }
            };
            var sourceFile = this.getSourceFile(fileName);
            if (sourceFile) {
                this.context = sourceFile.path;
                ts.forEachChild(sourceFile, visit_1);
            }
            return result_1.length ? result_1 : undefined;
        }
    };
    TypeScriptServiceHost.prototype.getDeclarations = function (fileName) {
        var _this = this;
        var result = [];
        var sourceFile = this.getSourceFile(fileName);
        if (sourceFile) {
            var visit_2 = function (child) {
                var declaration = _this.getDeclarationFromNode(sourceFile, child);
                if (declaration) {
                    result.push(declaration);
                }
                else {
                    ts.forEachChild(child, visit_2);
                }
            };
            ts.forEachChild(sourceFile, visit_2);
        }
        return result;
    };
    TypeScriptServiceHost.prototype.getSourceFile = function (fileName) {
        return this.tsService.getProgram().getSourceFile(fileName);
    };
    TypeScriptServiceHost.prototype.updateAnalyzedModules = function () {
        this.validate();
        if (this.modulesOutOfDate) {
            this.analyzedModules = null;
            this._reflector = null;
            this._staticSymbolResolver = null;
            this.templateReferences = null;
            this.fileToComponent = null;
            this.ensureAnalyzedModules();
            this.modulesOutOfDate = false;
        }
    };
    Object.defineProperty(TypeScriptServiceHost.prototype, "program", {
        get: function () { return this.tsService.getProgram(); },
        enumerable: true,
        configurable: true
    });
    Object.defineProperty(TypeScriptServiceHost.prototype, "checker", {
        get: function () {
            var checker = this._checker;
            if (!checker) {
                checker = this._checker = this.program.getTypeChecker();
            }
            return checker;
        },
        enumerable: true,
        configurable: true
    });
    TypeScriptServiceHost.prototype.validate = function () {
        var program = this.program;
        if (this.lastProgram != program) {
            this.clearCaches();
            this.lastProgram = program;
        }
    };
    TypeScriptServiceHost.prototype.clearCaches = function () {
        this._checker = null;
        this._typeCache = [];
        this._resolver = null;
        this.collectedErrors = null;
        this.modulesOutOfDate = true;
    };
    TypeScriptServiceHost.prototype.ensureTemplateMap = function () {
        if (!this.fileToComponent || !this.templateReferences) {
            var fileToComponent = new Map();
            var templateReference = [];
            var ngModuleSummary = this.getAnalyzedModules();
            var urlResolver = createOfflineCompileUrlResolver();
            for (var _i = 0, _a = ngModuleSummary.ngModules; _i < _a.length; _i++) {
                var module_1 = _a[_i];
                for (var _b = 0, _c = module_1.declaredDirectives; _b < _c.length; _b++) {
                    var directive = _c[_b];
                    var _d = this.resolver.getNonNormalizedDirectiveMetadata(directive.reference), metadata = _d.metadata, annotation = _d.annotation;
                    if (metadata.isComponent && metadata.template && metadata.template.templateUrl) {
                        var templateName = urlResolver.resolve(componentModuleUrl(this.reflector, directive.reference, annotation), metadata.template.templateUrl);
                        fileToComponent.set(templateName, directive.reference);
                        templateReference.push(templateName);
                    }
                }
            }
            this.fileToComponent = fileToComponent;
            this.templateReferences = templateReference;
        }
    };
    TypeScriptServiceHost.prototype.getSourceFromDeclaration = function (fileName, version, source, span, type, declaration, node, sourceFile) {
        var queryCache = undefined;
        var t = this;
        if (declaration) {
            return {
                version: version,
                source: source,
                span: span,
                type: type,
                get members() {
                    var checker = t.checker;
                    var program = t.program;
                    var type = checker.getTypeAtLocation(declaration);
                    return new TypeWrapper(type, { node: node, program: program, checker: checker }).members();
                },
                get query() {
                    if (!queryCache) {
                        queryCache = new TypeScriptSymbolQuery(t.program, t.checker, sourceFile, function () {
                            var pipes = t.service.getPipesAt(fileName, node.getStart());
                            var checker = t.checker;
                            var program = t.program;
                            return new PipesTable(pipes, { node: node, program: program, checker: checker });
                        });
                    }
                    return queryCache;
                }
            };
        }
    };
    TypeScriptServiceHost.prototype.getSourceFromNode = function (fileName, version, node) {
        var result = undefined;
        var t = this;
        switch (node.kind) {
            case ts.SyntaxKind.NoSubstitutionTemplateLiteral:
            case ts.SyntaxKind.StringLiteral:
                var _a = this.getTemplateClassDeclFromNode(node), declaration = _a[0], decorator = _a[1];
                var queryCache = undefined;
                if (declaration && declaration.name) {
                    var sourceFile = this.getSourceFile(fileName);
                    return this.getSourceFromDeclaration(fileName, version, this.stringOf(node), shrink(spanOf(node)), this.reflector.getStaticSymbol(sourceFile.fileName, declaration.name.text), declaration, node, sourceFile);
                }
                break;
        }
        return result;
    };
    TypeScriptServiceHost.prototype.getSourceFromType = function (fileName, version, type) {
        var result = undefined;
        var declaration = this.getTemplateClassFromStaticSymbol(type);
        if (declaration) {
            var snapshot = this.host.getScriptSnapshot(fileName);
            var source = snapshot.getText(0, snapshot.getLength());
            result = this.getSourceFromDeclaration(fileName, version, source, { start: 0, end: source.length }, type, declaration, declaration, declaration.getSourceFile());
        }
        return result;
    };
    Object.defineProperty(TypeScriptServiceHost.prototype, "reflectorHost", {
        get: function () {
            var _this = this;
            var result = this._reflectorHost;
            if (!result) {
                if (!this.context) {
                    // Make up a context by finding the first script and using that as the base dir.
                    this.context = this.host.getScriptFileNames()[0];
                }
                // Use the file context's directory as the base directory.
                // The host's getCurrentDirectory() is not reliable as it is always "" in
                // tsserver. We don't need the exact base directory, just one that contains
                // a source file.
                var source = this.tsService.getProgram().getSourceFile(this.context);
                if (!source) {
                    throw new Error('Internal error: no context could be determined');
                }
                var tsConfigPath = findTsConfig(source.fileName);
                var basePath = path.dirname(tsConfigPath || this.context);
                result = this._reflectorHost = new ReflectorHost(function () { return _this.tsService.getProgram(); }, this.host, { basePath: basePath, genDir: basePath });
            }
            return result;
        },
        enumerable: true,
        configurable: true
    });
    TypeScriptServiceHost.prototype.collectError = function (error, filePath) {
        var errorMap = this.collectedErrors;
        if (!errorMap) {
            errorMap = this.collectedErrors = new Map();
        }
        var errors = errorMap.get(filePath);
        if (!errors) {
            errors = [];
            this.collectedErrors.set(filePath, errors);
        }
        errors.push(error);
    };
    Object.defineProperty(TypeScriptServiceHost.prototype, "staticSymbolResolver", {
        get: function () {
            var _this = this;
            var result = this._staticSymbolResolver;
            if (!result) {
                var summaryResolver = new AotSummaryResolver({
                    loadSummary: function (filePath) { return null; },
                    isSourceFile: function (sourceFilePath) { return true; },
                    getOutputFileName: function (sourceFilePath) { return null; }
                }, this._staticSymbolCache);
                result = this._staticSymbolResolver = new StaticSymbolResolver(this.reflectorHost, this._staticSymbolCache, summaryResolver, function (e, filePath) { return _this.collectError(e, filePath); });
            }
            return result;
        },
        enumerable: true,
        configurable: true
    });
    Object.defineProperty(TypeScriptServiceHost.prototype, "reflector", {
        get: function () {
            var _this = this;
            var result = this._reflector;
            if (!result) {
                result = this._reflector = new StaticReflector(this.staticSymbolResolver, [], [], function (e, filePath) { return _this.collectError(e, filePath); });
            }
            return result;
        },
        enumerable: true,
        configurable: true
    });
    TypeScriptServiceHost.prototype.getTemplateClassFromStaticSymbol = function (type) {
        var source = this.getSourceFile(type.filePath);
        if (source) {
            var declarationNode = ts.forEachChild(source, function (child) {
                if (child.kind === ts.SyntaxKind.ClassDeclaration) {
                    var classDeclaration = child;
                    if (classDeclaration.name.text === type.name) {
                        return classDeclaration;
                    }
                }
            });
            return declarationNode;
        }
        return undefined;
    };
    /**
     * Given a template string node, see if it is an Angular template string, and if so return the
     * containing class.
     */
    TypeScriptServiceHost.prototype.getTemplateClassDeclFromNode = function (currentToken) {
        // Verify we are in a 'template' property assignment, in an object literal, which is an call
        // arg, in a decorator
        var parentNode = currentToken.parent; // PropertyAssignment
        if (!parentNode) {
            return TypeScriptServiceHost.missingTemplate;
        }
        if (parentNode.kind !== ts.SyntaxKind.PropertyAssignment) {
            return TypeScriptServiceHost.missingTemplate;
        }
        else {
            // TODO: Is this different for a literal, i.e. a quoted property name like "template"?
            if (parentNode.name.text !== 'template') {
                return TypeScriptServiceHost.missingTemplate;
            }
        }
        parentNode = parentNode.parent; // ObjectLiteralExpression
        if (!parentNode || parentNode.kind !== ts.SyntaxKind.ObjectLiteralExpression) {
            return TypeScriptServiceHost.missingTemplate;
        }
        parentNode = parentNode.parent; // CallExpression
        if (!parentNode || parentNode.kind !== ts.SyntaxKind.CallExpression) {
            return TypeScriptServiceHost.missingTemplate;
        }
        var callTarget = parentNode.expression;
        var decorator = parentNode.parent; // Decorator
        if (!decorator || decorator.kind !== ts.SyntaxKind.Decorator) {
            return TypeScriptServiceHost.missingTemplate;
        }
        var declaration = decorator.parent; // ClassDeclaration
        if (!declaration || declaration.kind !== ts.SyntaxKind.ClassDeclaration) {
            return TypeScriptServiceHost.missingTemplate;
        }
        return [declaration, callTarget];
    };
    TypeScriptServiceHost.prototype.getCollectedErrors = function (defaultSpan, sourceFile) {
        var errors = (this.collectedErrors && this.collectedErrors.get(sourceFile.fileName));
        return (errors && errors.map(function (e) {
            return { message: e.message, span: spanAt(sourceFile, e.line, e.column) || defaultSpan };
        })) ||
            [];
    };
    TypeScriptServiceHost.prototype.getDeclarationFromNode = function (sourceFile, node) {
        if (node.kind == ts.SyntaxKind.ClassDeclaration && node.decorators &&
            node.name) {
            for (var _i = 0, _a = node.decorators; _i < _a.length; _i++) {
                var decorator = _a[_i];
                if (decorator.expression && decorator.expression.kind == ts.SyntaxKind.CallExpression) {
                    var classDeclaration = node;
                    if (classDeclaration.name) {
                        var call = decorator.expression;
                        var target = call.expression;
                        var type = this.checker.getTypeAtLocation(target);
                        if (type) {
                            var staticSymbol = this._reflector.getStaticSymbol(sourceFile.fileName, classDeclaration.name.text);
                            try {
                                if (this.resolver.isDirective(staticSymbol)) {
                                    var metadata = this.resolver.getNonNormalizedDirectiveMetadata(staticSymbol).metadata;
                                    var declarationSpan = spanOf(target);
                                    return {
                                        type: staticSymbol,
                                        declarationSpan: declarationSpan,
                                        metadata: metadata,
                                        errors: this.getCollectedErrors(declarationSpan, sourceFile)
                                    };
                                }
                            }
                            catch (e) {
                                if (e.message) {
                                    this.collectError(e, sourceFile.fileName);
                                    var declarationSpan = spanOf(target);
                                    return {
                                        type: staticSymbol,
                                        declarationSpan: declarationSpan,
                                        errors: this.getCollectedErrors(declarationSpan, sourceFile)
                                    };
                                }
                            }
                        }
                    }
                }
            }
        }
    };
    TypeScriptServiceHost.prototype.stringOf = function (node) {
        switch (node.kind) {
            case ts.SyntaxKind.NoSubstitutionTemplateLiteral:
                return node.text;
            case ts.SyntaxKind.StringLiteral:
                return node.text;
        }
    };
    TypeScriptServiceHost.prototype.findNode = function (sourceFile, position) {
        var _this = this;
        function find(node) {
            if (position >= node.getStart() && position < node.getEnd()) {
                return ts.forEachChild(node, find) || node;
            }
        }
        return find(sourceFile);
    };
    TypeScriptServiceHost.prototype.findLiteralType = function (kind, context) {
        var checker = this.checker;
        var type;
        switch (kind) {
            case BuiltinType.Any:
                type = checker.getTypeAtLocation({
                    kind: ts.SyntaxKind.AsExpression,
                    expression: { kind: ts.SyntaxKind.TrueKeyword },
                    type: { kind: ts.SyntaxKind.AnyKeyword }
                });
                break;
            case BuiltinType.Boolean:
                type = checker.getTypeAtLocation({ kind: ts.SyntaxKind.TrueKeyword });
                break;
            case BuiltinType.Null:
                type = checker.getTypeAtLocation({ kind: ts.SyntaxKind.NullKeyword });
                break;
            case BuiltinType.Number:
                type = checker.getTypeAtLocation({ kind: ts.SyntaxKind.NumericLiteral });
                break;
            case BuiltinType.String:
                type =
                    checker.getTypeAtLocation({ kind: ts.SyntaxKind.NoSubstitutionTemplateLiteral });
                break;
            case BuiltinType.Undefined:
                type = checker.getTypeAtLocation({ kind: ts.SyntaxKind.VoidExpression });
                break;
            default:
                throw new Error("Internal error, unhandled literal kind " + kind + ":" + BuiltinType[kind]);
        }
        return new TypeWrapper(type, context);
    };
    TypeScriptServiceHost.missingTemplate = [];
    return TypeScriptServiceHost;
}());
var TypeScriptSymbolQuery = (function () {
    function TypeScriptSymbolQuery(program, checker, source, fetchPipes) {
        this.program = program;
        this.checker = checker;
        this.source = source;
        this.fetchPipes = fetchPipes;
        this.typeCache = new Map();
    }
    TypeScriptSymbolQuery.prototype.getTypeKind = function (symbol) { return typeKindOf(this.getTsTypeOf(symbol)); };
    TypeScriptSymbolQuery.prototype.getBuiltinType = function (kind) {
        // TODO: Replace with typeChecker API when available.
        var result = this.typeCache.get(kind);
        if (!result) {
            var type = getBuiltinTypeFromTs(kind, { checker: this.checker, node: this.source, program: this.program });
            result =
                new TypeWrapper(type, { program: this.program, checker: this.checker, node: this.source });
            this.typeCache.set(kind, result);
        }
        return result;
    };
    TypeScriptSymbolQuery.prototype.getTypeUnion = function () {
        var types = [];
        for (var _i = 0; _i < arguments.length; _i++) {
            types[_i - 0] = arguments[_i];
        }
        // TODO: Replace with typeChecker API when available
        var checker = this.checker;
        // No API exists so the cheat is to just return the last type any if no types are given.
        return types.length ? types[types.length - 1] : this.getBuiltinType(BuiltinType.Any);
    };
    TypeScriptSymbolQuery.prototype.getArrayType = function (type) {
        // TODO: Replace with typeChecker API when available
        return this.getBuiltinType(BuiltinType.Any);
    };
    TypeScriptSymbolQuery.prototype.getElementType = function (type) {
        if (type instanceof TypeWrapper) {
            var elementType = getTypeParameterOf(type.tsType, 'Array');
            if (elementType) {
                return new TypeWrapper(elementType, type.context);
            }
        }
    };
    TypeScriptSymbolQuery.prototype.getNonNullableType = function (symbol) {
        // TODO: Replace with typeChecker API when available;
        return symbol;
    };
    TypeScriptSymbolQuery.prototype.getPipes = function () {
        var result = this.pipesCache;
        if (!result) {
            result = this.pipesCache = this.fetchPipes();
        }
        return result;
    };
    TypeScriptSymbolQuery.prototype.getTemplateContext = function (type) {
        var context = { node: this.source, program: this.program, checker: this.checker };
        var typeSymbol = findClassSymbolInContext(type, context);
        if (typeSymbol) {
            var contextType = this.getTemplateRefContextType(typeSymbol);
            if (contextType)
                return new SymbolWrapper(contextType, context).members();
        }
    };
    TypeScriptSymbolQuery.prototype.getTypeSymbol = function (type) {
        var context = { node: this.source, program: this.program, checker: this.checker };
        var typeSymbol = findClassSymbolInContext(type, context);
        return new SymbolWrapper(typeSymbol, context);
    };
    TypeScriptSymbolQuery.prototype.createSymbolTable = function (symbols) {
        var result = new MapSymbolTable();
        result.addAll(symbols.map(function (s) { return new DeclaredSymbol(s); }));
        return result;
    };
    TypeScriptSymbolQuery.prototype.mergeSymbolTable = function (symbolTables) {
        var result = new MapSymbolTable();
        for (var _i = 0, symbolTables_1 = symbolTables; _i < symbolTables_1.length; _i++) {
            var symbolTable = symbolTables_1[_i];
            result.addAll(symbolTable.values());
        }
        return result;
    };
    TypeScriptSymbolQuery.prototype.getSpanAt = function (line, column) { return spanAt(this.source, line, column); };
    TypeScriptSymbolQuery.prototype.getTemplateRefContextType = function (type) {
        var constructor = type.members['__constructor'];
        if (constructor) {
            var constructorDeclaration = constructor.declarations[0];
            for (var _i = 0, _a = constructorDeclaration.parameters; _i < _a.length; _i++) {
                var parameter = _a[_i];
                var type_1 = this.checker.getTypeAtLocation(parameter.type);
                if (type_1.symbol.name == 'TemplateRef' && isReferenceType(type_1)) {
                    var typeReference = type_1;
                    if (typeReference.typeArguments.length === 1) {
                        return typeReference.typeArguments[0].symbol;
                    }
                }
            }
            ;
        }
    };
    TypeScriptSymbolQuery.prototype.getTsTypeOf = function (symbol) {
        var type = this.getTypeWrapper(symbol);
        return type && type.tsType;
    };
    TypeScriptSymbolQuery.prototype.getTypeWrapper = function (symbol) {
        var type = undefined;
        if (symbol instanceof TypeWrapper) {
            type = symbol;
        }
        else if (symbol.type instanceof TypeWrapper) {
            type = symbol.type;
        }
        return type;
    };
    return TypeScriptSymbolQuery;
}());
function typeCallable(type) {
    var signatures = type.getCallSignatures();
    return signatures && signatures.length != 0;
}
function signaturesOf(type, context) {
    return type.getCallSignatures().map(function (s) { return new SignatureWrapper(s, context); });
}
function selectSignature(type, context, types) {
    // TODO: Do a better job of selecting the right signature.
    var signatures = type.getCallSignatures();
    return signatures.length ? new SignatureWrapper(signatures[0], context) : undefined;
}
function toSymbolTable(symbols) {
    var result = {};
    for (var _i = 0, symbols_1 = symbols; _i < symbols_1.length; _i++) {
        var symbol = symbols_1[_i];
        result[symbol.name] = symbol;
    }
    return result;
}
function toSymbols(symbolTable, filter) {
    var result = [];
    var own = typeof symbolTable.hasOwnProperty === 'function' ?
        function (name) { return symbolTable.hasOwnProperty(name); } :
        function (name) { return !!symbolTable[name]; };
    for (var name_1 in symbolTable) {
        if (own(name_1) && (!filter || filter(symbolTable[name_1]))) {
            result.push(symbolTable[name_1]);
        }
    }
    return result;
}
var TypeWrapper = (function () {
    function TypeWrapper(tsType, context) {
        this.tsType = tsType;
        this.context = context;
        if (!tsType) {
            throw Error('Internal: null type');
        }
    }
    Object.defineProperty(TypeWrapper.prototype, "name", {
        get: function () {
            var symbol = this.tsType.symbol;
            return (symbol && symbol.name) || '<anonymous>';
        },
        enumerable: true,
        configurable: true
    });
    Object.defineProperty(TypeWrapper.prototype, "kind", {
        get: function () { return 'type'; },
        enumerable: true,
        configurable: true
    });
    Object.defineProperty(TypeWrapper.prototype, "language", {
        get: function () { return 'typescript'; },
        enumerable: true,
        configurable: true
    });
    Object.defineProperty(TypeWrapper.prototype, "type", {
        get: function () { return undefined; },
        enumerable: true,
        configurable: true
    });
    Object.defineProperty(TypeWrapper.prototype, "container", {
        get: function () { return undefined; },
        enumerable: true,
        configurable: true
    });
    Object.defineProperty(TypeWrapper.prototype, "public", {
        get: function () { return true; },
        enumerable: true,
        configurable: true
    });
    Object.defineProperty(TypeWrapper.prototype, "callable", {
        get: function () { return typeCallable(this.tsType); },
        enumerable: true,
        configurable: true
    });
    Object.defineProperty(TypeWrapper.prototype, "definition", {
        get: function () { return definitionFromTsSymbol(this.tsType.getSymbol()); },
        enumerable: true,
        configurable: true
    });
    TypeWrapper.prototype.members = function () {
        return new SymbolTableWrapper(this.tsType.getProperties(), this.context);
    };
    TypeWrapper.prototype.signatures = function () { return signaturesOf(this.tsType, this.context); };
    TypeWrapper.prototype.selectSignature = function (types) {
        return selectSignature(this.tsType, this.context, types);
    };
    TypeWrapper.prototype.indexed = function (argument) { return undefined; };
    return TypeWrapper;
}());
var SymbolWrapper = (function () {
    function SymbolWrapper(symbol, context) {
        this.symbol = symbol;
        this.context = context;
    }
    Object.defineProperty(SymbolWrapper.prototype, "name", {
        get: function () { return this.symbol.name; },
        enumerable: true,
        configurable: true
    });
    Object.defineProperty(SymbolWrapper.prototype, "kind", {
        get: function () { return this.callable ? 'method' : 'property'; },
        enumerable: true,
        configurable: true
    });
    Object.defineProperty(SymbolWrapper.prototype, "language", {
        get: function () { return 'typescript'; },
        enumerable: true,
        configurable: true
    });
    Object.defineProperty(SymbolWrapper.prototype, "type", {
        get: function () { return new TypeWrapper(this.tsType, this.context); },
        enumerable: true,
        configurable: true
    });
    Object.defineProperty(SymbolWrapper.prototype, "container", {
        get: function () { return getContainerOf(this.symbol, this.context); },
        enumerable: true,
        configurable: true
    });
    Object.defineProperty(SymbolWrapper.prototype, "public", {
        get: function () {
            // Symbols that are not explicitly made private are public.
            return !isSymbolPrivate(this.symbol);
        },
        enumerable: true,
        configurable: true
    });
    Object.defineProperty(SymbolWrapper.prototype, "callable", {
        get: function () { return typeCallable(this.tsType); },
        enumerable: true,
        configurable: true
    });
    Object.defineProperty(SymbolWrapper.prototype, "definition", {
        get: function () { return definitionFromTsSymbol(this.symbol); },
        enumerable: true,
        configurable: true
    });
    SymbolWrapper.prototype.members = function () { return new SymbolTableWrapper(this.symbol.members, this.context); };
    SymbolWrapper.prototype.signatures = function () { return signaturesOf(this.tsType, this.context); };
    SymbolWrapper.prototype.selectSignature = function (types) {
        return selectSignature(this.tsType, this.context, types);
    };
    SymbolWrapper.prototype.indexed = function (argument) { return undefined; };
    Object.defineProperty(SymbolWrapper.prototype, "tsType", {
        get: function () {
            var type = this._tsType;
            if (!type) {
                type = this._tsType =
                    this.context.checker.getTypeOfSymbolAtLocation(this.symbol, this.context.node);
            }
            return type;
        },
        enumerable: true,
        configurable: true
    });
    return SymbolWrapper;
}());
var DeclaredSymbol = (function () {
    function DeclaredSymbol(declaration) {
        this.declaration = declaration;
    }
    Object.defineProperty(DeclaredSymbol.prototype, "name", {
        get: function () { return this.declaration.name; },
        enumerable: true,
        configurable: true
    });
    Object.defineProperty(DeclaredSymbol.prototype, "kind", {
        get: function () { return this.declaration.kind; },
        enumerable: true,
        configurable: true
    });
    Object.defineProperty(DeclaredSymbol.prototype, "language", {
        get: function () { return 'ng-template'; },
        enumerable: true,
        configurable: true
    });
    Object.defineProperty(DeclaredSymbol.prototype, "container", {
        get: function () { return undefined; },
        enumerable: true,
        configurable: true
    });
    Object.defineProperty(DeclaredSymbol.prototype, "type", {
        get: function () { return this.declaration.type; },
        enumerable: true,
        configurable: true
    });
    Object.defineProperty(DeclaredSymbol.prototype, "callable", {
        get: function () { return this.declaration.type.callable; },
        enumerable: true,
        configurable: true
    });
    Object.defineProperty(DeclaredSymbol.prototype, "public", {
        get: function () { return true; },
        enumerable: true,
        configurable: true
    });
    Object.defineProperty(DeclaredSymbol.prototype, "definition", {
        get: function () { return this.declaration.definition; },
        enumerable: true,
        configurable: true
    });
    DeclaredSymbol.prototype.members = function () { return this.declaration.type.members(); };
    DeclaredSymbol.prototype.signatures = function () { return this.declaration.type.signatures(); };
    DeclaredSymbol.prototype.selectSignature = function (types) {
        return this.declaration.type.selectSignature(types);
    };
    DeclaredSymbol.prototype.indexed = function (argument) { return undefined; };
    return DeclaredSymbol;
}());
var SignatureWrapper = (function () {
    function SignatureWrapper(signature, context) {
        this.signature = signature;
        this.context = context;
    }
    Object.defineProperty(SignatureWrapper.prototype, "arguments", {
        get: function () {
            return new SymbolTableWrapper(this.signature.getParameters(), this.context);
        },
        enumerable: true,
        configurable: true
    });
    Object.defineProperty(SignatureWrapper.prototype, "result", {
        get: function () { return new TypeWrapper(this.signature.getReturnType(), this.context); },
        enumerable: true,
        configurable: true
    });
    return SignatureWrapper;
}());
var SignatureResultOverride = (function () {
    function SignatureResultOverride(signature, resultType) {
        this.signature = signature;
        this.resultType = resultType;
    }
    Object.defineProperty(SignatureResultOverride.prototype, "arguments", {
        get: function () { return this.signature.arguments; },
        enumerable: true,
        configurable: true
    });
    Object.defineProperty(SignatureResultOverride.prototype, "result", {
        get: function () { return this.resultType; },
        enumerable: true,
        configurable: true
    });
    return SignatureResultOverride;
}());
var SymbolTableWrapper = (function () {
    function SymbolTableWrapper(symbols, context, filter) {
        this.context = context;
        if (Array.isArray(symbols)) {
            this.symbols = filter ? symbols.filter(filter) : symbols;
            this.symbolTable = toSymbolTable(symbols);
        }
        else {
            this.symbols = toSymbols(symbols, filter);
            this.symbolTable = filter ? toSymbolTable(this.symbols) : symbols;
        }
    }
    Object.defineProperty(SymbolTableWrapper.prototype, "size", {
        get: function () { return this.symbols.length; },
        enumerable: true,
        configurable: true
    });
    SymbolTableWrapper.prototype.get = function (key) {
        var symbol = this.symbolTable[key];
        return symbol ? new SymbolWrapper(symbol, this.context) : undefined;
    };
    SymbolTableWrapper.prototype.has = function (key) { return this.symbolTable[key] != null; };
    SymbolTableWrapper.prototype.values = function () {
        var _this = this;
        return this.symbols.map(function (s) { return new SymbolWrapper(s, _this.context); });
    };
    return SymbolTableWrapper;
}());
var MapSymbolTable = (function () {
    function MapSymbolTable() {
        this.map = new Map();
        this._values = [];
    }
    Object.defineProperty(MapSymbolTable.prototype, "size", {
        get: function () { return this.map.size; },
        enumerable: true,
        configurable: true
    });
    MapSymbolTable.prototype.get = function (key) { return this.map.get(key); };
    MapSymbolTable.prototype.add = function (symbol) {
        if (this.map.has(symbol.name)) {
            var previous = this.map.get(symbol.name);
            this._values[this._values.indexOf(previous)] = symbol;
        }
        this.map.set(symbol.name, symbol);
        this._values.push(symbol);
    };
    MapSymbolTable.prototype.addAll = function (symbols) {
        for (var _i = 0, symbols_2 = symbols; _i < symbols_2.length; _i++) {
            var symbol = symbols_2[_i];
            this.add(symbol);
        }
    };
    MapSymbolTable.prototype.has = function (key) { return this.map.has(key); };
    MapSymbolTable.prototype.values = function () {
        // Switch to this.map.values once iterables are supported by the target language.
        return this._values;
    };
    return MapSymbolTable;
}());
var PipesTable = (function () {
    function PipesTable(pipes, context) {
        this.pipes = pipes;
        this.context = context;
    }
    Object.defineProperty(PipesTable.prototype, "size", {
        get: function () { return this.pipes.length; },
        enumerable: true,
        configurable: true
    });
    PipesTable.prototype.get = function (key) {
        var pipe = this.pipes.find(function (pipe) { return pipe.name == key; });
        if (pipe) {
            return new PipeSymbol(pipe, this.context);
        }
    };
    PipesTable.prototype.has = function (key) { return this.pipes.find(function (pipe) { return pipe.name == key; }) != null; };
    PipesTable.prototype.values = function () {
        var _this = this;
        return this.pipes.map(function (pipe) { return new PipeSymbol(pipe, _this.context); });
    };
    return PipesTable;
}());
var PipeSymbol = (function () {
    function PipeSymbol(pipe, context) {
        this.pipe = pipe;
        this.context = context;
    }
    Object.defineProperty(PipeSymbol.prototype, "name", {
        get: function () { return this.pipe.name; },
        enumerable: true,
        configurable: true
    });
    Object.defineProperty(PipeSymbol.prototype, "kind", {
        get: function () { return 'pipe'; },
        enumerable: true,
        configurable: true
    });
    Object.defineProperty(PipeSymbol.prototype, "language", {
        get: function () { return 'typescript'; },
        enumerable: true,
        configurable: true
    });
    Object.defineProperty(PipeSymbol.prototype, "type", {
        get: function () { return new TypeWrapper(this.tsType, this.context); },
        enumerable: true,
        configurable: true
    });
    Object.defineProperty(PipeSymbol.prototype, "container", {
        get: function () { return undefined; },
        enumerable: true,
        configurable: true
    });
    Object.defineProperty(PipeSymbol.prototype, "callable", {
        get: function () { return true; },
        enumerable: true,
        configurable: true
    });
    Object.defineProperty(PipeSymbol.prototype, "public", {
        get: function () { return true; },
        enumerable: true,
        configurable: true
    });
    Object.defineProperty(PipeSymbol.prototype, "definition", {
        get: function () { return definitionFromTsSymbol(this.tsType.getSymbol()); },
        enumerable: true,
        configurable: true
    });
    PipeSymbol.prototype.members = function () { return EmptyTable.instance; };
    PipeSymbol.prototype.signatures = function () { return signaturesOf(this.tsType, this.context); };
    PipeSymbol.prototype.selectSignature = function (types) {
        var signature = selectSignature(this.tsType, this.context, types);
        if (types.length == 1) {
            var parameterType = types[0];
            if (parameterType instanceof TypeWrapper) {
                var resultType = undefined;
                switch (this.name) {
                    case 'async':
                        switch (parameterType.name) {
                            case 'Observable':
                            case 'Promise':
                            case 'EventEmitter':
                                resultType = getTypeParameterOf(parameterType.tsType, parameterType.name);
                                break;
                        }
                        break;
                    case 'slice':
                        resultType = getTypeParameterOf(parameterType.tsType, 'Array');
                        break;
                }
                if (resultType) {
                    signature = new SignatureResultOverride(signature, new TypeWrapper(resultType, parameterType.context));
                }
            }
        }
        return signature;
    };
    PipeSymbol.prototype.indexed = function (argument) { return undefined; };
    Object.defineProperty(PipeSymbol.prototype, "tsType", {
        get: function () {
            var type = this._tsType;
            if (!type) {
                var classSymbol = this.findClassSymbol(this.pipe.symbol);
                if (classSymbol) {
                    type = this._tsType = this.findTransformMethodType(classSymbol);
                }
                if (!type) {
                    type = this._tsType = getBuiltinTypeFromTs(BuiltinType.Any, this.context);
                }
            }
            return type;
        },
        enumerable: true,
        configurable: true
    });
    PipeSymbol.prototype.findClassSymbol = function (type) {
        return findClassSymbolInContext(type, this.context);
    };
    PipeSymbol.prototype.findTransformMethodType = function (classSymbol) {
        var transform = classSymbol.members['transform'];
        if (transform) {
            return this.context.checker.getTypeOfSymbolAtLocation(transform, this.context.node);
        }
    };
    return PipeSymbol;
}());
function findClassSymbolInContext(type, context) {
    var sourceFile = context.program.getSourceFile(type.filePath);
    if (sourceFile) {
        var moduleSymbol = sourceFile.module || sourceFile.symbol;
        var exports_1 = context.checker.getExportsOfModule(moduleSymbol);
        return (exports_1 || []).find(function (symbol) { return symbol.name == type.name; });
    }
}
var EmptyTable = (function () {
    function EmptyTable() {
    }
    Object.defineProperty(EmptyTable.prototype, "size", {
        get: function () { return 0; },
        enumerable: true,
        configurable: true
    });
    EmptyTable.prototype.get = function (key) { return undefined; };
    EmptyTable.prototype.has = function (key) { return false; };
    EmptyTable.prototype.values = function () { return []; };
    EmptyTable.instance = new EmptyTable();
    return EmptyTable;
}());
function findTsConfig(fileName) {
    var dir = path.dirname(fileName);
    while (fs.existsSync(dir)) {
        var candidate = path.join(dir, 'tsconfig.json');
        if (fs.existsSync(candidate))
            return candidate;
        dir = path.dirname(dir);
    }
}
function isBindingPattern(node) {
    return !!node && (node.kind === ts.SyntaxKind.ArrayBindingPattern ||
        node.kind === ts.SyntaxKind.ObjectBindingPattern);
}
function walkUpBindingElementsAndPatterns(node) {
    while (node && (node.kind === ts.SyntaxKind.BindingElement || isBindingPattern(node))) {
        node = node.parent;
    }
    return node;
}
function getCombinedNodeFlags(node) {
    node = walkUpBindingElementsAndPatterns(node);
    var flags = node.flags;
    if (node.kind === ts.SyntaxKind.VariableDeclaration) {
        node = node.parent;
    }
    if (node && node.kind === ts.SyntaxKind.VariableDeclarationList) {
        flags |= node.flags;
        node = node.parent;
    }
    if (node && node.kind === ts.SyntaxKind.VariableStatement) {
        flags |= node.flags;
    }
    return flags;
}
function isSymbolPrivate(s) {
    return s.valueDeclaration && isPrivate(s.valueDeclaration);
}
function getBuiltinTypeFromTs(kind, context) {
    var type;
    var checker = context.checker;
    var node = context.node;
    switch (kind) {
        case BuiltinType.Any:
            type = checker.getTypeAtLocation(setParents({
                kind: ts.SyntaxKind.AsExpression,
                expression: { kind: ts.SyntaxKind.TrueKeyword },
                type: { kind: ts.SyntaxKind.AnyKeyword }
            }, node));
            break;
        case BuiltinType.Boolean:
            type =
                checker.getTypeAtLocation(setParents({ kind: ts.SyntaxKind.TrueKeyword }, node));
            break;
        case BuiltinType.Null:
            type =
                checker.getTypeAtLocation(setParents({ kind: ts.SyntaxKind.NullKeyword }, node));
            break;
        case BuiltinType.Number:
            var numeric = { kind: ts.SyntaxKind.NumericLiteral };
            setParents({ kind: ts.SyntaxKind.ExpressionStatement, expression: numeric }, node);
            type = checker.getTypeAtLocation(numeric);
            break;
        case BuiltinType.String:
            type = checker.getTypeAtLocation(setParents({ kind: ts.SyntaxKind.NoSubstitutionTemplateLiteral }, node));
            break;
        case BuiltinType.Undefined:
            type = checker.getTypeAtLocation(setParents({
                kind: ts.SyntaxKind.VoidExpression,
                expression: { kind: ts.SyntaxKind.NumericLiteral }
            }, node));
            break;
        default:
            throw new Error("Internal error, unhandled literal kind " + kind + ":" + BuiltinType[kind]);
    }
    return type;
}
function setParents(node, parent) {
    node.parent = parent;
    ts.forEachChild(node, function (child) { return setParents(child, node); });
    return node;
}
function spanOf(node) {
    return { start: node.getStart(), end: node.getEnd() };
}
function shrink(span, offset) {
    if (offset == null)
        offset = 1;
    return { start: span.start + offset, end: span.end - offset };
}
function spanAt(sourceFile, line, column) {
    if (line != null && column != null) {
        var position_1 = ts.getPositionOfLineAndCharacter(sourceFile, line, column);
        var findChild = function findChild(node) {
            if (node.kind > ts.SyntaxKind.LastToken && node.pos <= position_1 && node.end > position_1) {
                var betterNode = ts.forEachChild(node, findChild);
                return betterNode || node;
            }
        };
        var node = ts.forEachChild(sourceFile, findChild);
        if (node) {
            return { start: node.getStart(), end: node.getEnd() };
        }
    }
}
function definitionFromTsSymbol(symbol) {
    var declarations = symbol.declarations;
    if (declarations) {
        return declarations.map(function (declaration) {
            var sourceFile = declaration.getSourceFile();
            return {
                fileName: sourceFile.fileName,
                span: { start: declaration.getStart(), end: declaration.getEnd() }
            };
        });
    }
}
function parentDeclarationOf(node) {
    while (node) {
        switch (node.kind) {
            case ts.SyntaxKind.ClassDeclaration:
            case ts.SyntaxKind.InterfaceDeclaration:
                return node;
            case ts.SyntaxKind.SourceFile:
                return null;
        }
        node = node.parent;
    }
}
function getContainerOf(symbol, context) {
    if (symbol.getFlags() & ts.SymbolFlags.ClassMember && symbol.declarations) {
        for (var _i = 0, _a = symbol.declarations; _i < _a.length; _i++) {
            var declaration = _a[_i];
            var parent_1 = parentDeclarationOf(declaration);
            if (parent_1) {
                var type = context.checker.getTypeAtLocation(parent_1);
                if (type) {
                    return new TypeWrapper(type, context);
                }
            }
        }
    }
}
function getTypeParameterOf(type, name) {
    if (type && type.symbol && type.symbol.name == name) {
        var typeArguments = type.typeArguments;
        if (typeArguments && typeArguments.length <= 1) {
            return typeArguments[0];
        }
    }
}
function typeKindOf(type) {
    if (type) {
        if (type.flags & ts.TypeFlags.Any) {
            return BuiltinType.Any;
        }
        else if (type.flags & (ts.TypeFlags.String | ts.TypeFlags.StringLike | ts.TypeFlags.StringLiteral)) {
            return BuiltinType.String;
        }
        else if (type.flags & (ts.TypeFlags.Number | ts.TypeFlags.NumberLike)) {
            return BuiltinType.Number;
        }
        else if (type.flags & (ts.TypeFlags.Undefined)) {
            return BuiltinType.Undefined;
        }
        else if (type.flags & (ts.TypeFlags.Null)) {
            return BuiltinType.Null;
        }
        else if (type.flags & ts.TypeFlags.Union) {
            // If all the constituent types of a union are the same kind, it is also that kind.
            var candidate = void 0;
            var unionType = type;
            if (unionType.types.length > 0) {
                candidate = typeKindOf(unionType.types[0]);
                for (var _i = 0, _a = unionType.types; _i < _a.length; _i++) {
                    var subType = _a[_i];
                    if (candidate != typeKindOf(subType)) {
                        return BuiltinType.Other;
                    }
                }
            }
            return candidate;
        }
    }
    return BuiltinType.Other;
}
//# sourceMappingURL=typescript_host.js.map