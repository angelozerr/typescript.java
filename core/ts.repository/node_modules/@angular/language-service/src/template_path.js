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
import { templateVisitAll } from '@angular/compiler/src/template_parser/template_ast';
import { AstPath } from './ast_path';
import { inSpan, isNarrower, spanOf } from './utils';
export var TemplateAstPath = (function (_super) {
    __extends(TemplateAstPath, _super);
    function TemplateAstPath(ast, position, allowWidening) {
        if (allowWidening === void 0) { allowWidening = false; }
        _super.call(this, buildTemplatePath(ast, position, allowWidening));
        this.position = position;
    }
    return TemplateAstPath;
}(AstPath));
function buildTemplatePath(ast, position, allowWidening) {
    if (allowWidening === void 0) { allowWidening = false; }
    var visitor = new TemplateAstPathBuilder(position, allowWidening);
    templateVisitAll(visitor, ast);
    return visitor.getPath();
}
export var NullTemplateVisitor = (function () {
    function NullTemplateVisitor() {
    }
    NullTemplateVisitor.prototype.visitNgContent = function (ast) { };
    NullTemplateVisitor.prototype.visitEmbeddedTemplate = function (ast) { };
    NullTemplateVisitor.prototype.visitElement = function (ast) { };
    NullTemplateVisitor.prototype.visitReference = function (ast) { };
    NullTemplateVisitor.prototype.visitVariable = function (ast) { };
    NullTemplateVisitor.prototype.visitEvent = function (ast) { };
    NullTemplateVisitor.prototype.visitElementProperty = function (ast) { };
    NullTemplateVisitor.prototype.visitAttr = function (ast) { };
    NullTemplateVisitor.prototype.visitBoundText = function (ast) { };
    NullTemplateVisitor.prototype.visitText = function (ast) { };
    NullTemplateVisitor.prototype.visitDirective = function (ast) { };
    NullTemplateVisitor.prototype.visitDirectiveProperty = function (ast) { };
    return NullTemplateVisitor;
}());
export var TemplateAstChildVisitor = (function () {
    function TemplateAstChildVisitor(visitor) {
        this.visitor = visitor;
    }
    // Nodes with children
    TemplateAstChildVisitor.prototype.visitEmbeddedTemplate = function (ast, context) {
        return this.visitChildren(context, function (visit) {
            visit(ast.attrs);
            visit(ast.references);
            visit(ast.variables);
            visit(ast.directives);
            visit(ast.providers);
            visit(ast.children);
        });
    };
    TemplateAstChildVisitor.prototype.visitElement = function (ast, context) {
        return this.visitChildren(context, function (visit) {
            visit(ast.attrs);
            visit(ast.inputs);
            visit(ast.outputs);
            visit(ast.references);
            visit(ast.directives);
            visit(ast.providers);
            visit(ast.children);
        });
    };
    TemplateAstChildVisitor.prototype.visitDirective = function (ast, context) {
        return this.visitChildren(context, function (visit) {
            visit(ast.inputs);
            visit(ast.hostProperties);
            visit(ast.hostEvents);
        });
    };
    // Terminal nodes
    TemplateAstChildVisitor.prototype.visitNgContent = function (ast, context) { };
    TemplateAstChildVisitor.prototype.visitReference = function (ast, context) { };
    TemplateAstChildVisitor.prototype.visitVariable = function (ast, context) { };
    TemplateAstChildVisitor.prototype.visitEvent = function (ast, context) { };
    TemplateAstChildVisitor.prototype.visitElementProperty = function (ast, context) { };
    TemplateAstChildVisitor.prototype.visitAttr = function (ast, context) { };
    TemplateAstChildVisitor.prototype.visitBoundText = function (ast, context) { };
    TemplateAstChildVisitor.prototype.visitText = function (ast, context) { };
    TemplateAstChildVisitor.prototype.visitDirectiveProperty = function (ast, context) { };
    TemplateAstChildVisitor.prototype.visitChildren = function (context, cb) {
        var visitor = this.visitor || this;
        var results = [];
        function visit(children) {
            if (children && children.length)
                results.push(templateVisitAll(visitor, children, context));
        }
        cb(visit);
        return [].concat.apply([], results);
    };
    return TemplateAstChildVisitor;
}());
var TemplateAstPathBuilder = (function (_super) {
    __extends(TemplateAstPathBuilder, _super);
    function TemplateAstPathBuilder(position, allowWidening) {
        _super.call(this);
        this.position = position;
        this.allowWidening = allowWidening;
        this.path = [];
    }
    TemplateAstPathBuilder.prototype.visit = function (ast, context) {
        var span = spanOf(ast);
        if (inSpan(this.position, span)) {
            var len = this.path.length;
            if (!len || this.allowWidening || isNarrower(span, spanOf(this.path[len - 1]))) {
                this.path.push(ast);
            }
        }
        else {
            // Returning a value here will result in the children being skipped.
            return true;
        }
    };
    TemplateAstPathBuilder.prototype.visitEmbeddedTemplate = function (ast, context) {
        return this.visitChildren(context, function (visit) {
            // Ignore reference, variable and providers
            visit(ast.attrs);
            visit(ast.directives);
            visit(ast.children);
        });
    };
    TemplateAstPathBuilder.prototype.visitElement = function (ast, context) {
        return this.visitChildren(context, function (visit) {
            // Ingnore providers
            visit(ast.attrs);
            visit(ast.inputs);
            visit(ast.outputs);
            visit(ast.references);
            visit(ast.directives);
            visit(ast.children);
        });
    };
    TemplateAstPathBuilder.prototype.visitDirective = function (ast, context) {
        // Ignore the host properties of a directive
        var result = this.visitChildren(context, function (visit) { visit(ast.inputs); });
        // We never care about the diretive itself, just its inputs.
        if (this.path[this.path.length - 1] == ast) {
            this.path.pop();
        }
        return result;
    };
    TemplateAstPathBuilder.prototype.getPath = function () { return this.path; };
    return TemplateAstPathBuilder;
}(TemplateAstChildVisitor));
//# sourceMappingURL=template_path.js.map