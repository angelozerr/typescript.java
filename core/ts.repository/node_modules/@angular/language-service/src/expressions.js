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
import { identifierName, tokenReference } from '@angular/compiler';
import { ASTWithSource } from '@angular/compiler/src/expression_parser/ast';
import { EmbeddedTemplateAst, templateVisitAll } from '@angular/compiler/src/template_parser/template_ast';
import { AstPath as AstPathBase } from './ast_path';
import { TemplateAstChildVisitor } from './template_path';
import { BuiltinType, DiagnosticKind } from './types';
import { inSpan } from './utils';
export function getExpressionDiagnostics(scope, ast, query, context) {
    if (context === void 0) { context = {}; }
    var analyzer = new AstType(scope, query, context);
    analyzer.getDiagnostics(ast);
    return analyzer.diagnostics;
}
export function getExpressionCompletions(scope, ast, position, query) {
    var path = new AstPath(ast, position);
    if (path.empty)
        return undefined;
    var tail = path.tail;
    var result = scope;
    function getType(ast) { return new AstType(scope, query, {}).getType(ast); }
    // If the completion request is in a not in a pipe or property access then the global scope
    // (that is the scope of the implicit receiver) is the right scope as the user is typing the
    // beginning of an expression.
    tail.visit({
        visitBinary: function (ast) { },
        visitChain: function (ast) { },
        visitConditional: function (ast) { },
        visitFunctionCall: function (ast) { },
        visitImplicitReceiver: function (ast) { },
        visitInterpolation: function (ast) { result = undefined; },
        visitKeyedRead: function (ast) { },
        visitKeyedWrite: function (ast) { },
        visitLiteralArray: function (ast) { },
        visitLiteralMap: function (ast) { },
        visitLiteralPrimitive: function (ast) { },
        visitMethodCall: function (ast) { },
        visitPipe: function (ast) {
            if (position >= ast.exp.span.end &&
                (!ast.args || !ast.args.length || position < ast.args[0].span.start)) {
                // We are in a position a pipe name is expected.
                result = query.getPipes();
            }
        },
        visitPrefixNot: function (ast) { },
        visitPropertyRead: function (ast) {
            var receiverType = getType(ast.receiver);
            result = receiverType ? receiverType.members() : scope;
        },
        visitPropertyWrite: function (ast) {
            var receiverType = getType(ast.receiver);
            result = receiverType ? receiverType.members() : scope;
        },
        visitQuote: function (ast) {
            // For a quote, return the members of any (if there are any).
            result = query.getBuiltinType(BuiltinType.Any).members();
        },
        visitSafeMethodCall: function (ast) {
            var receiverType = getType(ast.receiver);
            result = receiverType ? receiverType.members() : scope;
        },
        visitSafePropertyRead: function (ast) {
            var receiverType = getType(ast.receiver);
            result = receiverType ? receiverType.members() : scope;
        },
    });
    return result && result.values();
}
export function getExpressionSymbol(scope, ast, position, query) {
    var path = new AstPath(ast, position, /* excludeEmpty */ true);
    if (path.empty)
        return undefined;
    var tail = path.tail;
    function getType(ast) { return new AstType(scope, query, {}).getType(ast); }
    var symbol = undefined;
    var span = undefined;
    // If the completion request is in a not in a pipe or property access then the global scope
    // (that is the scope of the implicit receiver) is the right scope as the user is typing the
    // beginning of an expression.
    tail.visit({
        visitBinary: function (ast) { },
        visitChain: function (ast) { },
        visitConditional: function (ast) { },
        visitFunctionCall: function (ast) { },
        visitImplicitReceiver: function (ast) { },
        visitInterpolation: function (ast) { },
        visitKeyedRead: function (ast) { },
        visitKeyedWrite: function (ast) { },
        visitLiteralArray: function (ast) { },
        visitLiteralMap: function (ast) { },
        visitLiteralPrimitive: function (ast) { },
        visitMethodCall: function (ast) {
            var receiverType = getType(ast.receiver);
            symbol = receiverType && receiverType.members().get(ast.name);
            span = ast.span;
        },
        visitPipe: function (ast) {
            if (position >= ast.exp.span.end &&
                (!ast.args || !ast.args.length || position < ast.args[0].span.start)) {
                // We are in a position a pipe name is expected.
                var pipes = query.getPipes();
                if (pipes) {
                    symbol = pipes.get(ast.name);
                    span = ast.span;
                }
            }
        },
        visitPrefixNot: function (ast) { },
        visitPropertyRead: function (ast) {
            var receiverType = getType(ast.receiver);
            symbol = receiverType && receiverType.members().get(ast.name);
            span = ast.span;
        },
        visitPropertyWrite: function (ast) {
            var receiverType = getType(ast.receiver);
            symbol = receiverType && receiverType.members().get(ast.name);
            span = ast.span;
        },
        visitQuote: function (ast) { },
        visitSafeMethodCall: function (ast) {
            var receiverType = getType(ast.receiver);
            symbol = receiverType && receiverType.members().get(ast.name);
            span = ast.span;
        },
        visitSafePropertyRead: function (ast) {
            var receiverType = getType(ast.receiver);
            symbol = receiverType && receiverType.members().get(ast.name);
            span = ast.span;
        },
    });
    if (symbol && span) {
        return { symbol: symbol, span: span };
    }
}
// Consider moving to expression_parser/ast
var NullVisitor = (function () {
    function NullVisitor() {
    }
    NullVisitor.prototype.visitBinary = function (ast) { };
    NullVisitor.prototype.visitChain = function (ast) { };
    NullVisitor.prototype.visitConditional = function (ast) { };
    NullVisitor.prototype.visitFunctionCall = function (ast) { };
    NullVisitor.prototype.visitImplicitReceiver = function (ast) { };
    NullVisitor.prototype.visitInterpolation = function (ast) { };
    NullVisitor.prototype.visitKeyedRead = function (ast) { };
    NullVisitor.prototype.visitKeyedWrite = function (ast) { };
    NullVisitor.prototype.visitLiteralArray = function (ast) { };
    NullVisitor.prototype.visitLiteralMap = function (ast) { };
    NullVisitor.prototype.visitLiteralPrimitive = function (ast) { };
    NullVisitor.prototype.visitMethodCall = function (ast) { };
    NullVisitor.prototype.visitPipe = function (ast) { };
    NullVisitor.prototype.visitPrefixNot = function (ast) { };
    NullVisitor.prototype.visitPropertyRead = function (ast) { };
    NullVisitor.prototype.visitPropertyWrite = function (ast) { };
    NullVisitor.prototype.visitQuote = function (ast) { };
    NullVisitor.prototype.visitSafeMethodCall = function (ast) { };
    NullVisitor.prototype.visitSafePropertyRead = function (ast) { };
    return NullVisitor;
}());
export var TypeDiagnostic = (function () {
    function TypeDiagnostic(kind, message, ast) {
        this.kind = kind;
        this.message = message;
        this.ast = ast;
    }
    return TypeDiagnostic;
}());
// AstType calculatetype of the ast given AST element.
var AstType = (function () {
    function AstType(scope, query, context) {
        this.scope = scope;
        this.query = query;
        this.context = context;
    }
    AstType.prototype.getType = function (ast) { return ast.visit(this); };
    AstType.prototype.getDiagnostics = function (ast) {
        this.diagnostics = [];
        var type = ast.visit(this);
        if (this.context.event && type.callable) {
            this.reportWarning('Unexpected callable expression. Expected a method call', ast);
        }
        return this.diagnostics;
    };
    AstType.prototype.visitBinary = function (ast) {
        // Treat undefined and null as other.
        function normalize(kind, other) {
            switch (kind) {
                case BuiltinType.Undefined:
                case BuiltinType.Null:
                    return normalize(other, BuiltinType.Other);
            }
            return kind;
        }
        var leftType = this.getType(ast.left);
        var rightType = this.getType(ast.right);
        var leftRawKind = this.query.getTypeKind(leftType);
        var rightRawKind = this.query.getTypeKind(rightType);
        var leftKind = normalize(leftRawKind, rightRawKind);
        var rightKind = normalize(rightRawKind, leftRawKind);
        // The following swtich implements operator typing similar to the
        // type production tables in the TypeScript specification.
        // https://github.com/Microsoft/TypeScript/blob/v1.8.10/doc/spec.md#4.19
        var operKind = leftKind << 8 | rightKind;
        switch (ast.operation) {
            case '*':
            case '/':
            case '%':
            case '-':
            case '<<':
            case '>>':
            case '>>>':
            case '&':
            case '^':
            case '|':
                switch (operKind) {
                    case BuiltinType.Any << 8 | BuiltinType.Any:
                    case BuiltinType.Number << 8 | BuiltinType.Any:
                    case BuiltinType.Any << 8 | BuiltinType.Number:
                    case BuiltinType.Number << 8 | BuiltinType.Number:
                        return this.query.getBuiltinType(BuiltinType.Number);
                    default:
                        var errorAst = ast.left;
                        switch (leftKind) {
                            case BuiltinType.Any:
                            case BuiltinType.Number:
                                errorAst = ast.right;
                                break;
                        }
                        return this.reportError('Expected a numeric type', errorAst);
                }
            case '+':
                switch (operKind) {
                    case BuiltinType.Any << 8 | BuiltinType.Any:
                    case BuiltinType.Any << 8 | BuiltinType.Boolean:
                    case BuiltinType.Any << 8 | BuiltinType.Number:
                    case BuiltinType.Any << 8 | BuiltinType.Other:
                    case BuiltinType.Boolean << 8 | BuiltinType.Any:
                    case BuiltinType.Number << 8 | BuiltinType.Any:
                    case BuiltinType.Other << 8 | BuiltinType.Any:
                        return this.anyType;
                    case BuiltinType.Any << 8 | BuiltinType.String:
                    case BuiltinType.Boolean << 8 | BuiltinType.String:
                    case BuiltinType.Number << 8 | BuiltinType.String:
                    case BuiltinType.String << 8 | BuiltinType.Any:
                    case BuiltinType.String << 8 | BuiltinType.Boolean:
                    case BuiltinType.String << 8 | BuiltinType.Number:
                    case BuiltinType.String << 8 | BuiltinType.String:
                    case BuiltinType.String << 8 | BuiltinType.Other:
                    case BuiltinType.Other << 8 | BuiltinType.String:
                        return this.query.getBuiltinType(BuiltinType.String);
                    case BuiltinType.Number << 8 | BuiltinType.Number:
                        return this.query.getBuiltinType(BuiltinType.Number);
                    case BuiltinType.Boolean << 8 | BuiltinType.Number:
                    case BuiltinType.Other << 8 | BuiltinType.Number:
                        return this.reportError('Expected a number type', ast.left);
                    case BuiltinType.Number << 8 | BuiltinType.Boolean:
                    case BuiltinType.Number << 8 | BuiltinType.Other:
                        return this.reportError('Expected a number type', ast.right);
                    default:
                        return this.reportError('Expected operands to be a string or number type', ast);
                }
            case '>':
            case '<':
            case '<=':
            case '>=':
            case '==':
            case '!=':
            case '===':
            case '!==':
                switch (operKind) {
                    case BuiltinType.Any << 8 | BuiltinType.Any:
                    case BuiltinType.Any << 8 | BuiltinType.Boolean:
                    case BuiltinType.Any << 8 | BuiltinType.Number:
                    case BuiltinType.Any << 8 | BuiltinType.String:
                    case BuiltinType.Any << 8 | BuiltinType.Other:
                    case BuiltinType.Boolean << 8 | BuiltinType.Any:
                    case BuiltinType.Boolean << 8 | BuiltinType.Boolean:
                    case BuiltinType.Number << 8 | BuiltinType.Any:
                    case BuiltinType.Number << 8 | BuiltinType.Number:
                    case BuiltinType.String << 8 | BuiltinType.Any:
                    case BuiltinType.String << 8 | BuiltinType.String:
                    case BuiltinType.Other << 8 | BuiltinType.Any:
                    case BuiltinType.Other << 8 | BuiltinType.Other:
                        return this.query.getBuiltinType(BuiltinType.Boolean);
                    default:
                        return this.reportError('Expected the operants to be of similar type or any', ast);
                }
            case '&&':
                return rightType;
            case '||':
                return this.query.getTypeUnion(leftType, rightType);
        }
        return this.reportError("Unrecognized operator " + ast.operation, ast);
    };
    AstType.prototype.visitChain = function (ast) {
        if (this.diagnostics) {
            // If we are producing diagnostics, visit the children
            visitChildren(ast, this);
        }
        // The type of a chain is always undefined.
        return this.query.getBuiltinType(BuiltinType.Undefined);
    };
    AstType.prototype.visitConditional = function (ast) {
        // The type of a conditional is the union of the true and false conditions.
        return this.query.getTypeUnion(this.getType(ast.trueExp), this.getType(ast.falseExp));
    };
    AstType.prototype.visitFunctionCall = function (ast) {
        var _this = this;
        // The type of a function call is the return type of the selected signature.
        // The signature is selected based on the types of the arguments. Angular doesn't
        // support contextual typing of arguments so this is simpler than TypeScript's
        // version.
        var args = ast.args.map(function (arg) { return _this.getType(arg); });
        var target = this.getType(ast.target);
        if (!target || !target.callable)
            return this.reportError('Call target is not callable', ast);
        var signature = target.selectSignature(args);
        if (signature)
            return signature.result;
        // TODO: Consider a better error message here.
        return this.reportError('Unable no compatible signature found for call', ast);
    };
    AstType.prototype.visitImplicitReceiver = function (ast) {
        var _this = this;
        // Return a pseudo-symbol for the implicit receiver.
        // The members of the implicit receiver are what is defined by the
        // scope passed into this class.
        return {
            name: '$implict',
            kind: 'component',
            language: 'ng-template',
            type: undefined,
            container: undefined,
            callable: false,
            public: true,
            definition: undefined,
            members: function () { return _this.scope; },
            signatures: function () { return []; },
            selectSignature: function (types) { return undefined; },
            indexed: function (argument) { return undefined; }
        };
    };
    AstType.prototype.visitInterpolation = function (ast) {
        // If we are producing diagnostics, visit the children.
        if (this.diagnostics) {
            visitChildren(ast, this);
        }
        return this.undefinedType;
    };
    AstType.prototype.visitKeyedRead = function (ast) {
        var targetType = this.getType(ast.obj);
        var keyType = this.getType(ast.key);
        var result = targetType.indexed(keyType);
        return result || this.anyType;
    };
    AstType.prototype.visitKeyedWrite = function (ast) {
        // The write of a type is the type of the value being written.
        return this.getType(ast.value);
    };
    AstType.prototype.visitLiteralArray = function (ast) {
        var _this = this;
        // A type literal is an array type of the union of the elements
        return this.query.getArrayType((_a = this.query).getTypeUnion.apply(_a, ast.expressions.map(function (element) { return _this.getType(element); })));
        var _a;
    };
    AstType.prototype.visitLiteralMap = function (ast) {
        // If we are producing diagnostics, visit the children
        if (this.diagnostics) {
            visitChildren(ast, this);
        }
        // TODO: Return a composite type.
        return this.anyType;
    };
    AstType.prototype.visitLiteralPrimitive = function (ast) {
        // The type of a literal primitive depends on the value of the literal.
        switch (ast.value) {
            case true:
            case false:
                return this.query.getBuiltinType(BuiltinType.Boolean);
            case null:
                return this.query.getBuiltinType(BuiltinType.Null);
            case undefined:
                return this.query.getBuiltinType(BuiltinType.Undefined);
            default:
                switch (typeof ast.value) {
                    case 'string':
                        return this.query.getBuiltinType(BuiltinType.String);
                    case 'number':
                        return this.query.getBuiltinType(BuiltinType.Number);
                    default:
                        return this.reportError('Unrecognized primitive', ast);
                }
        }
    };
    AstType.prototype.visitMethodCall = function (ast) {
        return this.resolveMethodCall(this.getType(ast.receiver), ast);
    };
    AstType.prototype.visitPipe = function (ast) {
        var _this = this;
        // The type of a pipe node is the return type of the pipe's transform method. The table returned
        // by getPipes() is expected to contain symbols with the corresponding transform method type.
        var pipe = this.query.getPipes().get(ast.name);
        if (!pipe)
            return this.reportError("No pipe by the name " + pipe.name + " found", ast);
        var expType = this.getType(ast.exp);
        var signature = pipe.selectSignature([expType].concat(ast.args.map(function (arg) { return _this.getType(arg); })));
        if (!signature)
            return this.reportError('Unable to resolve signature for pipe invocation', ast);
        return signature.result;
    };
    AstType.prototype.visitPrefixNot = function (ast) {
        // The type of a prefix ! is always boolean.
        return this.query.getBuiltinType(BuiltinType.Boolean);
    };
    AstType.prototype.visitPropertyRead = function (ast) {
        return this.resolvePropertyRead(this.getType(ast.receiver), ast);
    };
    AstType.prototype.visitPropertyWrite = function (ast) {
        // The type of a write is the type of the value being written.
        return this.getType(ast.value);
    };
    AstType.prototype.visitQuote = function (ast) {
        // The type of a quoted expression is any.
        return this.query.getBuiltinType(BuiltinType.Any);
    };
    AstType.prototype.visitSafeMethodCall = function (ast) {
        return this.resolveMethodCall(this.query.getNonNullableType(this.getType(ast.receiver)), ast);
    };
    AstType.prototype.visitSafePropertyRead = function (ast) {
        return this.resolvePropertyRead(this.query.getNonNullableType(this.getType(ast.receiver)), ast);
    };
    Object.defineProperty(AstType.prototype, "anyType", {
        get: function () {
            var result = this._anyType;
            if (!result) {
                result = this._anyType = this.query.getBuiltinType(BuiltinType.Any);
            }
            return result;
        },
        enumerable: true,
        configurable: true
    });
    Object.defineProperty(AstType.prototype, "undefinedType", {
        get: function () {
            var result = this._undefinedType;
            if (!result) {
                result = this._undefinedType = this.query.getBuiltinType(BuiltinType.Undefined);
            }
            return result;
        },
        enumerable: true,
        configurable: true
    });
    AstType.prototype.resolveMethodCall = function (receiverType, ast) {
        var _this = this;
        if (this.isAny(receiverType)) {
            return this.anyType;
        }
        // The type of a method is the selected methods result type.
        var method = receiverType.members().get(ast.name);
        if (!method)
            return this.reportError("Unknown method " + ast.name, ast);
        if (!method.type.callable)
            return this.reportError("Member " + ast.name + " is not callable", ast);
        var signature = method.type.selectSignature(ast.args.map(function (arg) { return _this.getType(arg); }));
        if (!signature)
            return this.reportError("Unable to resolve signature for call of method " + ast.name, ast);
        return signature.result;
    };
    AstType.prototype.resolvePropertyRead = function (receiverType, ast) {
        if (this.isAny(receiverType)) {
            return this.anyType;
        }
        // The type of a property read is the seelcted member's type.
        var member = receiverType.members().get(ast.name);
        if (!member) {
            var receiverInfo = receiverType.name;
            if (receiverInfo == '$implict') {
                receiverInfo =
                    'The component declaration, template variable declarations, and element references do';
            }
            else {
                receiverInfo = "'" + receiverInfo + "' does";
            }
            return this.reportError("Identifier '" + ast.name + "' is not defined. " + receiverInfo + " not contain such a member", ast);
        }
        if (!member.public) {
            var receiverInfo = receiverType.name;
            if (receiverInfo == '$implict') {
                receiverInfo = 'the component';
            }
            else {
                receiverInfo = "'" + receiverInfo + "'";
            }
            this.reportWarning("Identifier '" + ast.name + "' refers to a private member of " + receiverInfo, ast);
        }
        return member.type;
    };
    AstType.prototype.reportError = function (message, ast) {
        if (this.diagnostics) {
            this.diagnostics.push(new TypeDiagnostic(DiagnosticKind.Error, message, ast));
        }
        return this.anyType;
    };
    AstType.prototype.reportWarning = function (message, ast) {
        if (this.diagnostics) {
            this.diagnostics.push(new TypeDiagnostic(DiagnosticKind.Warning, message, ast));
        }
        return this.anyType;
    };
    AstType.prototype.isAny = function (symbol) {
        return !symbol || this.query.getTypeKind(symbol) == BuiltinType.Any ||
            (symbol.type && this.isAny(symbol.type));
    };
    return AstType;
}());
var AstPath = (function (_super) {
    __extends(AstPath, _super);
    function AstPath(ast, position, excludeEmpty) {
        if (excludeEmpty === void 0) { excludeEmpty = false; }
        _super.call(this, new AstPathVisitor(position, excludeEmpty).buildPath(ast).path);
        this.position = position;
    }
    return AstPath;
}(AstPathBase));
var AstPathVisitor = (function (_super) {
    __extends(AstPathVisitor, _super);
    function AstPathVisitor(position, excludeEmpty) {
        _super.call(this);
        this.position = position;
        this.excludeEmpty = excludeEmpty;
        this.path = [];
    }
    AstPathVisitor.prototype.visit = function (ast) {
        if ((!this.excludeEmpty || ast.span.start < ast.span.end) && inSpan(this.position, ast.span)) {
            this.path.push(ast);
            visitChildren(ast, this);
        }
    };
    AstPathVisitor.prototype.buildPath = function (ast) {
        // We never care about the ASTWithSource node and its visit() method calls its ast's visit so
        // the visit() method above would never see it.
        if (ast instanceof ASTWithSource) {
            ast = ast.ast;
        }
        this.visit(ast);
        return this;
    };
    return AstPathVisitor;
}(NullVisitor));
// TODO: Consider moving to expression_parser/ast
function visitChildren(ast, visitor) {
    function visit(ast) { visitor.visit && visitor.visit(ast) || ast.visit(visitor); }
    function visitAll(asts) { asts.forEach(visit); }
    ast.visit({
        visitBinary: function (ast) {
            visit(ast.left);
            visit(ast.right);
        },
        visitChain: function (ast) { visitAll(ast.expressions); },
        visitConditional: function (ast) {
            visit(ast.condition);
            visit(ast.trueExp);
            visit(ast.falseExp);
        },
        visitFunctionCall: function (ast) {
            visit(ast.target);
            visitAll(ast.args);
        },
        visitImplicitReceiver: function (ast) { },
        visitInterpolation: function (ast) { visitAll(ast.expressions); },
        visitKeyedRead: function (ast) {
            visit(ast.obj);
            visit(ast.key);
        },
        visitKeyedWrite: function (ast) {
            visit(ast.obj);
            visit(ast.key);
            visit(ast.obj);
        },
        visitLiteralArray: function (ast) { visitAll(ast.expressions); },
        visitLiteralMap: function (ast) { },
        visitLiteralPrimitive: function (ast) { },
        visitMethodCall: function (ast) {
            visit(ast.receiver);
            visitAll(ast.args);
        },
        visitPipe: function (ast) {
            visit(ast.exp);
            visitAll(ast.args);
        },
        visitPrefixNot: function (ast) { visit(ast.expression); },
        visitPropertyRead: function (ast) { visit(ast.receiver); },
        visitPropertyWrite: function (ast) {
            visit(ast.receiver);
            visit(ast.value);
        },
        visitQuote: function (ast) { },
        visitSafeMethodCall: function (ast) {
            visit(ast.receiver);
            visitAll(ast.args);
        },
        visitSafePropertyRead: function (ast) { visit(ast.receiver); },
    });
}
export function getExpressionScope(info, path, includeEvent) {
    var result = info.template.members;
    var references = getReferences(info);
    var variables = getVarDeclarations(info, path);
    var events = getEventDeclaration(info, path, includeEvent);
    if (references.length || variables.length || events.length) {
        var referenceTable = info.template.query.createSymbolTable(references);
        var variableTable = info.template.query.createSymbolTable(variables);
        var eventsTable = info.template.query.createSymbolTable(events);
        result =
            info.template.query.mergeSymbolTable([result, referenceTable, variableTable, eventsTable]);
    }
    return result;
}
function getEventDeclaration(info, path, includeEvent) {
    var result = [];
    if (includeEvent) {
        // TODO: Determine the type of the event parameter based on the Observable<T> or EventEmitter<T>
        // of the event.
        result = [{
                name: '$event',
                kind: 'variable',
                type: info.template.query.getBuiltinType(BuiltinType.Any)
            }];
    }
    return result;
}
function getReferences(info) {
    var result = [];
    function processReferences(references) {
        var _loop_1 = function(reference) {
            var type = void 0;
            if (reference.value) {
                type = info.template.query.getTypeSymbol(tokenReference(reference.value));
            }
            result.push({
                name: reference.name,
                kind: 'reference',
                type: type || info.template.query.getBuiltinType(BuiltinType.Any),
                get definition() { return getDefintionOf(info, reference); }
            });
        };
        for (var _i = 0, references_1 = references; _i < references_1.length; _i++) {
            var reference = references_1[_i];
            _loop_1(reference);
        }
    }
    var visitor = new (function (_super) {
        __extends(class_1, _super);
        function class_1() {
            _super.apply(this, arguments);
        }
        class_1.prototype.visitEmbeddedTemplate = function (ast, context) {
            _super.prototype.visitEmbeddedTemplate.call(this, ast, context);
            processReferences(ast.references);
        };
        class_1.prototype.visitElement = function (ast, context) {
            _super.prototype.visitElement.call(this, ast, context);
            processReferences(ast.references);
        };
        return class_1;
    }(TemplateAstChildVisitor));
    templateVisitAll(visitor, info.templateAst);
    return result;
}
function getVarDeclarations(info, path) {
    var result = [];
    var current = path.tail;
    while (current) {
        if (current instanceof EmbeddedTemplateAst) {
            var _loop_2 = function(variable) {
                var name_1 = variable.name;
                // Find the first directive with a context.
                var context = current.directives
                    .map(function (d) { return info.template.query.getTemplateContext(d.directive.type.reference); })
                    .find(function (c) { return !!c; });
                // Determine the type of the context field referenced by variable.value.
                var type = void 0;
                if (context) {
                    var value = context.get(variable.value);
                    if (value) {
                        type = value.type;
                        if (info.template.query.getTypeKind(type) === BuiltinType.Any) {
                            // The any type is not very useful here. For special cases, such as ngFor, we can do
                            // better.
                            type = refinedVariableType(type, info, current);
                        }
                    }
                }
                if (!type) {
                    type = info.template.query.getBuiltinType(BuiltinType.Any);
                }
                result.push({
                    name: name_1,
                    kind: 'variable', type: type, get definition() { return getDefintionOf(info, variable); }
                });
            };
            for (var _i = 0, _a = current.variables; _i < _a.length; _i++) {
                var variable = _a[_i];
                _loop_2(variable);
            }
        }
        current = path.parentOf(current);
    }
    return result;
}
function refinedVariableType(type, info, templateElement) {
    // Special case the ngFor directive
    var ngForDirective = templateElement.directives.find(function (d) { return identifierName(d.directive.type) == 'NgFor'; });
    if (ngForDirective) {
        var ngForOfBinding = ngForDirective.inputs.find(function (i) { return i.directiveName == 'ngForOf'; });
        if (ngForOfBinding) {
            var bindingType = new AstType(info.template.members, info.template.query, {}).getType(ngForOfBinding.value);
            if (bindingType) {
                return info.template.query.getElementType(bindingType);
            }
        }
    }
    // We can't do better, just return the original type.
    return type;
}
function getDefintionOf(info, ast) {
    if (info.fileName) {
        var templateOffset = info.template.span.start;
        return [{
                fileName: info.fileName,
                span: {
                    start: ast.sourceSpan.start.offset + templateOffset,
                    end: ast.sourceSpan.end.offset + templateOffset
                }
            }];
    }
}
//# sourceMappingURL=expressions.js.map