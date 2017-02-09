/**
 * @license
 * Copyright Google Inc. All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license that can be
 * found in the LICENSE file at https://angular.io/license
 */
import { tokenReference } from '@angular/compiler';
import { Attribute } from '@angular/compiler/src/ml_parser/ast';
import { ElementAst } from '@angular/compiler/src/template_parser/template_ast';
import { getExpressionScope, getExpressionSymbol } from './expressions';
import { HtmlAstPath } from './html_path';
import { TemplateAstPath } from './template_path';
import { inSpan, offsetSpan, spanOf } from './utils';
export function locateSymbol(info) {
    var templatePosition = info.position - info.template.span.start;
    var path = new TemplateAstPath(info.templateAst, templatePosition);
    if (path.tail) {
        var symbol_1 = undefined;
        var span_1 = undefined;
        var attributeValueSymbol_1 = function (ast, inEvent) {
            if (inEvent === void 0) { inEvent = false; }
            var attribute = findAttribute(info);
            if (attribute) {
                if (inSpan(templatePosition, spanOf(attribute.valueSpan))) {
                    var scope = getExpressionScope(info, path, inEvent);
                    var expressionOffset = attribute.valueSpan.start.offset + 1;
                    var result = getExpressionSymbol(scope, ast, templatePosition - expressionOffset, info.template.query);
                    if (result) {
                        symbol_1 = result.symbol;
                        span_1 = offsetSpan(result.span, expressionOffset);
                    }
                    return true;
                }
            }
            return false;
        };
        path.tail.visit({
            visitNgContent: function (ast) { },
            visitEmbeddedTemplate: function (ast) { },
            visitElement: function (ast) {
                var component = ast.directives.find(function (d) { return d.directive.isComponent; });
                if (component) {
                    symbol_1 = info.template.query.getTypeSymbol(component.directive.type.reference);
                    symbol_1 = symbol_1 && new OverrideKindSymbol(symbol_1, 'component');
                    span_1 = spanOf(ast);
                }
                else {
                    // Find a directive that matches the element name
                    var directive = ast.directives.find(function (d) { return d.directive.selector.indexOf(ast.name) >= 0; });
                    if (directive) {
                        symbol_1 = info.template.query.getTypeSymbol(directive.directive.type.reference);
                        symbol_1 = symbol_1 && new OverrideKindSymbol(symbol_1, 'directive');
                        span_1 = spanOf(ast);
                    }
                }
            },
            visitReference: function (ast) {
                symbol_1 = info.template.query.getTypeSymbol(tokenReference(ast.value));
                span_1 = spanOf(ast);
            },
            visitVariable: function (ast) { },
            visitEvent: function (ast) {
                if (!attributeValueSymbol_1(ast.handler, /* inEvent */ true)) {
                    symbol_1 = findOutputBinding(info, path, ast);
                    symbol_1 = symbol_1 && new OverrideKindSymbol(symbol_1, 'event');
                    span_1 = spanOf(ast);
                }
            },
            visitElementProperty: function (ast) { attributeValueSymbol_1(ast.value); },
            visitAttr: function (ast) { },
            visitBoundText: function (ast) {
                var expressionPosition = templatePosition - ast.sourceSpan.start.offset;
                if (inSpan(expressionPosition, ast.value.span)) {
                    var scope = getExpressionScope(info, path, /* includeEvent */ false);
                    var result = getExpressionSymbol(scope, ast.value, expressionPosition, info.template.query);
                    if (result) {
                        symbol_1 = result.symbol;
                        span_1 = offsetSpan(result.span, ast.sourceSpan.start.offset);
                    }
                }
            },
            visitText: function (ast) { },
            visitDirective: function (ast) {
                symbol_1 = info.template.query.getTypeSymbol(ast.directive.type.reference);
                span_1 = spanOf(ast);
            },
            visitDirectiveProperty: function (ast) {
                if (!attributeValueSymbol_1(ast.value)) {
                    symbol_1 = findInputBinding(info, path, ast);
                    span_1 = spanOf(ast);
                }
            }
        }, null);
        if (symbol_1 && span_1) {
            return { symbol: symbol_1, span: offsetSpan(span_1, info.template.span.start) };
        }
    }
}
function findAttribute(info) {
    var templatePosition = info.position - info.template.span.start;
    var path = new HtmlAstPath(info.htmlAst, templatePosition);
    return path.first(Attribute);
}
function findInputBinding(info, path, binding) {
    var element = path.first(ElementAst);
    if (element) {
        for (var _i = 0, _a = element.directives; _i < _a.length; _i++) {
            var directive = _a[_i];
            var invertedInput = invertMap(directive.directive.inputs);
            var fieldName = invertedInput[binding.templateName];
            if (fieldName) {
                var classSymbol = info.template.query.getTypeSymbol(directive.directive.type.reference);
                if (classSymbol) {
                    return classSymbol.members().get(fieldName);
                }
            }
        }
    }
}
function findOutputBinding(info, path, binding) {
    var element = path.first(ElementAst);
    if (element) {
        for (var _i = 0, _a = element.directives; _i < _a.length; _i++) {
            var directive = _a[_i];
            var invertedOutputs = invertMap(directive.directive.outputs);
            var fieldName = invertedOutputs[binding.name];
            if (fieldName) {
                var classSymbol = info.template.query.getTypeSymbol(directive.directive.type.reference);
                if (classSymbol) {
                    return classSymbol.members().get(fieldName);
                }
            }
        }
    }
}
function invertMap(obj) {
    var result = {};
    for (var _i = 0, _a = Object.keys(obj); _i < _a.length; _i++) {
        var name_1 = _a[_i];
        var v = obj[name_1];
        result[v] = name_1;
    }
    return result;
}
/**
 * Wrap a symbol and change its kind to component.
 */
var OverrideKindSymbol = (function () {
    function OverrideKindSymbol(sym, kindOverride) {
        this.sym = sym;
        this.kindOverride = kindOverride;
    }
    Object.defineProperty(OverrideKindSymbol.prototype, "name", {
        get: function () { return this.sym.name; },
        enumerable: true,
        configurable: true
    });
    Object.defineProperty(OverrideKindSymbol.prototype, "kind", {
        get: function () { return this.kindOverride; },
        enumerable: true,
        configurable: true
    });
    Object.defineProperty(OverrideKindSymbol.prototype, "language", {
        get: function () { return this.sym.language; },
        enumerable: true,
        configurable: true
    });
    Object.defineProperty(OverrideKindSymbol.prototype, "type", {
        get: function () { return this.sym.type; },
        enumerable: true,
        configurable: true
    });
    Object.defineProperty(OverrideKindSymbol.prototype, "container", {
        get: function () { return this.sym.container; },
        enumerable: true,
        configurable: true
    });
    Object.defineProperty(OverrideKindSymbol.prototype, "public", {
        get: function () { return this.sym.public; },
        enumerable: true,
        configurable: true
    });
    Object.defineProperty(OverrideKindSymbol.prototype, "callable", {
        get: function () { return this.sym.callable; },
        enumerable: true,
        configurable: true
    });
    Object.defineProperty(OverrideKindSymbol.prototype, "definition", {
        get: function () { return this.sym.definition; },
        enumerable: true,
        configurable: true
    });
    OverrideKindSymbol.prototype.members = function () { return this.sym.members(); };
    OverrideKindSymbol.prototype.signatures = function () { return this.sym.signatures(); };
    OverrideKindSymbol.prototype.selectSignature = function (types) { return this.sym.selectSignature(types); };
    OverrideKindSymbol.prototype.indexed = function (argument) { return this.sym.indexed(argument); };
    return OverrideKindSymbol;
}());
//# sourceMappingURL=locate_symbol.js.map