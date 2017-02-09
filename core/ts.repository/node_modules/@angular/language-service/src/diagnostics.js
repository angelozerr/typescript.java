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
import { Attribute } from '@angular/compiler/src/ml_parser/ast';
import { templateVisitAll } from '@angular/compiler/src/template_parser/template_ast';
import { getExpressionDiagnostics, getExpressionScope } from './expressions';
import { HtmlAstPath } from './html_path';
import { TemplateAstChildVisitor, TemplateAstPath } from './template_path';
import { DiagnosticKind } from './types';
import { getSelectors, hasTemplateReference, offsetSpan, spanOf } from './utils';
export function getTemplateDiagnostics(fileName, astProvider, templates) {
    var results = [];
    var _loop_1 = function(template) {
        var ast = astProvider.getTemplateAst(template, fileName);
        if (ast) {
            if (ast.parseErrors && ast.parseErrors.length) {
                results.push.apply(results, ast.parseErrors.map(function (e) { return ({
                    kind: DiagnosticKind.Error,
                    span: offsetSpan(spanOf(e.span), template.span.start),
                    message: e.msg
                }); }));
            }
            else if (ast.templateAst) {
                var expressionDiagnostics = getTemplateExpressionDiagnostics(template, ast);
                results.push.apply(results, expressionDiagnostics);
            }
            if (ast.errors) {
                results.push.apply(results, ast.errors.map(function (e) { return ({ kind: e.kind, span: e.span || template.span, message: e.message }); }));
            }
        }
    };
    for (var _i = 0, templates_1 = templates; _i < templates_1.length; _i++) {
        var template = templates_1[_i];
        _loop_1(template);
    }
    return results;
}
export function getDeclarationDiagnostics(declarations, modules) {
    var results = [];
    var directives = undefined;
    var _loop_2 = function(declaration) {
        var report = function (message, span) {
            results.push({
                kind: DiagnosticKind.Error,
                span: span || declaration.declarationSpan, message: message
            });
        };
        for (var _i = 0, _a = declaration.errors; _i < _a.length; _i++) {
            var error = _a[_i];
            report(error.message, error.span);
        }
        if (declaration.metadata) {
            if (declaration.metadata.isComponent) {
                if (!modules.ngModuleByPipeOrDirective.has(declaration.type)) {
                    report("Component '" + declaration.type.name + "' is not included in a module and will not be available inside a template. Consider adding it to a NgModule declaration");
                }
                if (declaration.metadata.template.template == null &&
                    !declaration.metadata.template.templateUrl) {
                    report("Component " + declaration.type.name + " must have a template or templateUrl");
                }
            }
            else {
                if (!directives) {
                    directives = new Set();
                    modules.ngModules.forEach(function (module) {
                        module.declaredDirectives.forEach(function (directive) { directives.add(directive.reference); });
                    });
                }
                if (!directives.has(declaration.type)) {
                    report("Directive '" + declaration.type.name + "' is not included in a module and will not be available inside a template. Consider adding it to a NgModule declaration");
                }
            }
        }
    };
    for (var _b = 0, declarations_1 = declarations; _b < declarations_1.length; _b++) {
        var declaration = declarations_1[_b];
        _loop_2(declaration);
    }
    return results;
}
function getTemplateExpressionDiagnostics(template, astResult) {
    var info = {
        template: template,
        htmlAst: astResult.htmlAst,
        directive: astResult.directive,
        directives: astResult.directives,
        pipes: astResult.pipes,
        templateAst: astResult.templateAst,
        expressionParser: astResult.expressionParser
    };
    var visitor = new ExpressionDiagnosticsVisitor(info, function (path, includeEvent) {
        return getExpressionScope(info, path, includeEvent);
    });
    templateVisitAll(visitor, astResult.templateAst);
    return visitor.diagnostics;
}
var ExpressionDiagnosticsVisitor = (function (_super) {
    __extends(ExpressionDiagnosticsVisitor, _super);
    function ExpressionDiagnosticsVisitor(info, getExpressionScope) {
        _super.call(this);
        this.info = info;
        this.getExpressionScope = getExpressionScope;
        this.diagnostics = [];
        this.path = new TemplateAstPath([], 0);
    }
    ExpressionDiagnosticsVisitor.prototype.visitDirective = function (ast, context) {
        // Override the default child visitor to ignore the host properties of a directive.
        if (ast.inputs && ast.inputs.length) {
            templateVisitAll(this, ast.inputs, context);
        }
    };
    ExpressionDiagnosticsVisitor.prototype.visitBoundText = function (ast) {
        this.push(ast);
        this.diagnoseExpression(ast.value, ast.sourceSpan.start.offset, false);
        this.pop();
    };
    ExpressionDiagnosticsVisitor.prototype.visitDirectiveProperty = function (ast) {
        this.push(ast);
        this.diagnoseExpression(ast.value, this.attributeValueLocation(ast), false);
        this.pop();
    };
    ExpressionDiagnosticsVisitor.prototype.visitElementProperty = function (ast) {
        this.push(ast);
        this.diagnoseExpression(ast.value, this.attributeValueLocation(ast), false);
        this.pop();
    };
    ExpressionDiagnosticsVisitor.prototype.visitEvent = function (ast) {
        this.push(ast);
        this.diagnoseExpression(ast.handler, this.attributeValueLocation(ast), true);
        this.pop();
    };
    ExpressionDiagnosticsVisitor.prototype.visitVariable = function (ast) {
        var directive = this.directiveSummary;
        if (directive && ast.value) {
            var context = this.info.template.query.getTemplateContext(directive.type.reference);
            if (!context.has(ast.value)) {
                if (ast.value === '$implicit') {
                    this.reportError('The template context does not have an implicit value', spanOf(ast.sourceSpan));
                }
                else {
                    this.reportError("The template context does not defined a member called '" + ast.value + "'", spanOf(ast.sourceSpan));
                }
            }
        }
    };
    ExpressionDiagnosticsVisitor.prototype.visitElement = function (ast, context) {
        this.push(ast);
        _super.prototype.visitElement.call(this, ast, context);
        this.pop();
    };
    ExpressionDiagnosticsVisitor.prototype.visitEmbeddedTemplate = function (ast, context) {
        var previousDirectiveSummary = this.directiveSummary;
        this.push(ast);
        // Find directive that refernces this template
        this.directiveSummary =
            ast.directives.map(function (d) { return d.directive; }).find(function (d) { return hasTemplateReference(d.type); });
        // Process children
        _super.prototype.visitEmbeddedTemplate.call(this, ast, context);
        this.pop();
        this.directiveSummary = previousDirectiveSummary;
    };
    ExpressionDiagnosticsVisitor.prototype.attributeValueLocation = function (ast) {
        var path = new HtmlAstPath(this.info.htmlAst, ast.sourceSpan.start.offset);
        var last = path.tail;
        if (last instanceof Attribute && last.valueSpan) {
            // Add 1 for the quote.
            return last.valueSpan.start.offset + 1;
        }
        return ast.sourceSpan.start.offset;
    };
    ExpressionDiagnosticsVisitor.prototype.diagnoseExpression = function (ast, offset, includeEvent) {
        var _this = this;
        var scope = this.getExpressionScope(this.path, includeEvent);
        (_a = this.diagnostics).push.apply(_a, getExpressionDiagnostics(scope, ast, this.info.template.query, {
            event: includeEvent
        }).map(function (d) { return ({
            span: offsetSpan(d.ast.span, offset + _this.info.template.span.start),
            kind: d.kind,
            message: d.message
        }); }));
        var _a;
    };
    ExpressionDiagnosticsVisitor.prototype.push = function (ast) { this.path.push(ast); };
    ExpressionDiagnosticsVisitor.prototype.pop = function () { this.path.pop(); };
    ExpressionDiagnosticsVisitor.prototype.selectors = function () {
        var result = this._selectors;
        if (!result) {
            this._selectors = result = getSelectors(this.info);
        }
        return result;
    };
    ExpressionDiagnosticsVisitor.prototype.findElement = function (position) {
        var htmlPath = new HtmlAstPath(this.info.htmlAst, position);
        if (htmlPath.tail instanceof Element) {
            return htmlPath.tail;
        }
    };
    ExpressionDiagnosticsVisitor.prototype.reportError = function (message, span) {
        this.diagnostics.push({
            span: offsetSpan(span, this.info.template.span.start),
            kind: DiagnosticKind.Error, message: message
        });
    };
    ExpressionDiagnosticsVisitor.prototype.reportWarning = function (message, span) {
        this.diagnostics.push({
            span: offsetSpan(span, this.info.template.span.start),
            kind: DiagnosticKind.Warning, message: message
        });
    };
    return ExpressionDiagnosticsVisitor;
}(TemplateAstChildVisitor));
//# sourceMappingURL=diagnostics.js.map