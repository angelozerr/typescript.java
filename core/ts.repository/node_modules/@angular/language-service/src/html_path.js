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
import { visitAll } from '@angular/compiler/src/ml_parser/ast';
import { AstPath } from './ast_path';
import { inSpan, spanOf } from './utils';
export var HtmlAstPath = (function (_super) {
    __extends(HtmlAstPath, _super);
    function HtmlAstPath(ast, position) {
        _super.call(this, buildPath(ast, position));
        this.position = position;
    }
    return HtmlAstPath;
}(AstPath));
function buildPath(ast, position) {
    var visitor = new HtmlAstPathBuilder(position);
    visitAll(visitor, ast);
    return visitor.getPath();
}
export var ChildVisitor = (function () {
    function ChildVisitor(visitor) {
        this.visitor = visitor;
    }
    ChildVisitor.prototype.visitElement = function (ast, context) {
        this.visitChildren(context, function (visit) {
            visit(ast.attrs);
            visit(ast.children);
        });
    };
    ChildVisitor.prototype.visitAttribute = function (ast, context) { };
    ChildVisitor.prototype.visitText = function (ast, context) { };
    ChildVisitor.prototype.visitComment = function (ast, context) { };
    ChildVisitor.prototype.visitExpansion = function (ast, context) {
        return this.visitChildren(context, function (visit) { visit(ast.cases); });
    };
    ChildVisitor.prototype.visitExpansionCase = function (ast, context) { };
    ChildVisitor.prototype.visitChildren = function (context, cb) {
        var visitor = this.visitor || this;
        var results = [];
        function visit(children) {
            if (children)
                results.push(visitAll(visitor, children, context));
        }
        cb(visit);
        return [].concat.apply([], results);
    };
    return ChildVisitor;
}());
var HtmlAstPathBuilder = (function (_super) {
    __extends(HtmlAstPathBuilder, _super);
    function HtmlAstPathBuilder(position) {
        _super.call(this);
        this.position = position;
        this.path = [];
    }
    HtmlAstPathBuilder.prototype.visit = function (ast, context) {
        var span = spanOf(ast);
        if (inSpan(this.position, span)) {
            this.path.push(ast);
        }
        else {
            // Returning a value here will result in the children being skipped.
            return true;
        }
    };
    HtmlAstPathBuilder.prototype.getPath = function () { return this.path; };
    return HtmlAstPathBuilder;
}(ChildVisitor));
//# sourceMappingURL=html_path.js.map