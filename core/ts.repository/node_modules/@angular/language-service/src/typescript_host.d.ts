import { NgAnalyzedModules } from '@angular/compiler/src/aot/compiler';
import { CompileMetadataResolver } from '@angular/compiler/src/metadata_resolver';
import { HtmlParser } from '@angular/compiler/src/ml_parser/html_parser';
import { InterpolationConfig } from '@angular/compiler/src/ml_parser/interpolation_config';
import { ParseTreeResult } from '@angular/compiler/src/ml_parser/parser';
import { ResourceLoader } from '@angular/compiler/src/resource_loader';
import * as ts from 'typescript';
import { Declarations, LanguageService, LanguageServiceHost, TemplateSource, TemplateSources } from './types';
/**
 * Create a `LanguageServiceHost`
 */
export declare function createLanguageServiceFromTypescript(host: ts.LanguageServiceHost, service: ts.LanguageService): LanguageService;
/**
 * The language service never needs the normalized versions of the metadata. To avoid parsing
 * the content and resolving references, return an empty file. This also allows normalizing
 * template that are syntatically incorrect which is required to provide completions in
 * syntatically incorrect templates.
 */
export declare class DummyHtmlParser extends HtmlParser {
    constructor();
    parse(source: string, url: string, parseExpansionForms?: boolean, interpolationConfig?: InterpolationConfig): ParseTreeResult;
}
/**
 * Avoid loading resources in the language servcie by using a dummy loader.
 */
export declare class DummyResourceLoader extends ResourceLoader {
    get(url: string): Promise<string>;
}
/**
 * An implemntation of a `LanguageSerivceHost` for a TypeScript project.
 *
 * The `TypeScriptServiceHost` implements the Angular `LanguageServiceHost` using
 * the TypeScript language services.
 *
 * @expermental
 */
export declare class TypeScriptServiceHost implements LanguageServiceHost {
    private host;
    private tsService;
    private _resolver;
    private _staticSymbolCache;
    private _staticSymbolResolver;
    private _reflector;
    private _reflectorHost;
    private _checker;
    private _typeCache;
    private context;
    private lastProgram;
    private modulesOutOfDate;
    private analyzedModules;
    private service;
    private fileToComponent;
    private templateReferences;
    private collectedErrors;
    constructor(host: ts.LanguageServiceHost, tsService: ts.LanguageService);
    setSite(service: LanguageService): void;
    /**
     * Angular LanguageServiceHost implementation
     */
    resolver: CompileMetadataResolver;
    getTemplateReferences(): string[];
    getTemplateAt(fileName: string, position: number): TemplateSource | undefined;
    getAnalyzedModules(): NgAnalyzedModules;
    private ensureAnalyzedModules();
    getTemplates(fileName: string): TemplateSources;
    getDeclarations(fileName: string): Declarations;
    getSourceFile(fileName: string): ts.SourceFile;
    updateAnalyzedModules(): void;
    private program;
    private checker;
    private validate();
    private clearCaches();
    private ensureTemplateMap();
    private getSourceFromDeclaration(fileName, version, source, span, type, declaration, node, sourceFile);
    private getSourceFromNode(fileName, version, node);
    private getSourceFromType(fileName, version, type);
    private reflectorHost;
    private collectError(error, filePath);
    private staticSymbolResolver;
    private reflector;
    private getTemplateClassFromStaticSymbol(type);
    private static missingTemplate;
    /**
     * Given a template string node, see if it is an Angular template string, and if so return the
     * containing class.
     */
    private getTemplateClassDeclFromNode(currentToken);
    private getCollectedErrors(defaultSpan, sourceFile);
    private getDeclarationFromNode(sourceFile, node);
    private stringOf(node);
    private findNode(sourceFile, position);
    private findLiteralType(kind, context);
}
