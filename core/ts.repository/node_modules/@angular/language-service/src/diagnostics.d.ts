import { NgAnalyzedModules } from '@angular/compiler/src/aot/compiler';
import { AstResult } from './common';
import { Declarations, Diagnostics, TemplateSource } from './types';
export interface AstProvider {
    getTemplateAst(template: TemplateSource, fileName: string): AstResult;
}
export declare function getTemplateDiagnostics(fileName: string, astProvider: AstProvider, templates: TemplateSource[]): Diagnostics;
export declare function getDeclarationDiagnostics(declarations: Declarations, modules: NgAnalyzedModules): Diagnostics;
