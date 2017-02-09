import { AST } from '@angular/compiler/src/expression_parser/ast';
import { TemplateInfo } from './common';
import { TemplateAstPath } from './template_path';
import { DiagnosticKind, Span, Symbol, SymbolQuery, SymbolTable } from './types';
export interface ExpressionDiagnosticsContext {
    event?: boolean;
}
export declare function getExpressionDiagnostics(scope: SymbolTable, ast: AST, query: SymbolQuery, context?: ExpressionDiagnosticsContext): TypeDiagnostic[];
export declare function getExpressionCompletions(scope: SymbolTable, ast: AST, position: number, query: SymbolQuery): Symbol[];
export declare function getExpressionSymbol(scope: SymbolTable, ast: AST, position: number, query: SymbolQuery): {
    symbol: Symbol;
    span: Span;
};
export declare class TypeDiagnostic {
    kind: DiagnosticKind;
    message: string;
    ast: AST;
    constructor(kind: DiagnosticKind, message: string, ast: AST);
}
export declare function getExpressionScope(info: TemplateInfo, path: TemplateAstPath, includeEvent: boolean): SymbolTable;
