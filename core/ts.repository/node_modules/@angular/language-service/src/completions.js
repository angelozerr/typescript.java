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
import { ImplicitReceiver, ParseSpan, PropertyRead } from '@angular/compiler/src/expression_parser/ast';
import { Element, Text } from '@angular/compiler/src/ml_parser/ast';
import { getHtmlTagDefinition } from '@angular/compiler/src/ml_parser/html_tags';
import { NAMED_ENTITIES, TagContentType, splitNsName } from '@angular/compiler/src/ml_parser/tags';
import { CssSelector, SelectorMatcher } from '@angular/compiler/src/selector';
import { getExpressionCompletions, getExpressionScope } from './expressions';
import { attributeNames, elementNames, eventNames, propertyNames } from './html_info';
import { HtmlAstPath } from './html_path';
import { NullTemplateVisitor, TemplateAstPath } from './template_path';
import { flatten, getSelectors, hasTemplateReference, inSpan, removeSuffix, spanOf, uniqueByName } from './utils';
var TEMPLATE_ATTR_PREFIX = '*';
var hiddenHtmlElements = {
    html: true,
    script: true,
    noscript: true,
    base: true,
    body: true,
    title: true,
    head: true,
    link: true,
};
export function getTemplateCompletions(templateInfo) {
    var result = undefined;
    var htmlAst = templateInfo.htmlAst, templateAst = templateInfo.templateAst, template = templateInfo.template;
    // The templateNode starts at the delimiter character so we add 1 to skip it.
    var templatePosition = templateInfo.position - template.span.start;
    var path = new HtmlAstPath(htmlAst, templatePosition);
    var mostSpecific = path.tail;
    if (path.empty) {
        result = elementCompletions(templateInfo, path);
    }
    else {
        var astPosition_1 = templatePosition - mostSpecific.sourceSpan.start.offset;
        mostSpecific.visit({
            visitElement: function (ast) {
                var startTagSpan = spanOf(ast.sourceSpan);
                var tagLen = ast.name.length;
                if (templatePosition <=
                    startTagSpan.start + tagLen + 1 /* 1 for the opening angle bracked */) {
                    // If we are in the tag then return the element completions.
                    result = elementCompletions(templateInfo, path);
                }
                else if (templatePosition < startTagSpan.end) {
                    // We are in the attribute section of the element (but not in an attribute).
                    // Return the attribute completions.
                    result = attributeCompletions(templateInfo, path);
                }
            },
            visitAttribute: function (ast) {
                if (!ast.valueSpan || !inSpan(templatePosition, spanOf(ast.valueSpan))) {
                    // We are in the name of an attribute. Show attribute completions.
                    result = attributeCompletions(templateInfo, path);
                }
                else if (ast.valueSpan && inSpan(templatePosition, spanOf(ast.valueSpan))) {
                    result = attributeValueCompletions(templateInfo, templatePosition, ast);
                }
            },
            visitText: function (ast) {
                // Check if we are in a entity.
                result = entityCompletions(getSourceText(template, spanOf(ast)), astPosition_1);
                if (result)
                    return result;
                result = interpolationCompletions(templateInfo, templatePosition);
                if (result)
                    return result;
                var element = path.first(Element);
                if (element) {
                    var definition = getHtmlTagDefinition(element.name);
                    if (definition.contentType === TagContentType.PARSABLE_DATA) {
                        result = voidElementAttributeCompletions(templateInfo, path);
                        if (!result) {
                            // If the element can hold content Show element completions.
                            result = elementCompletions(templateInfo, path);
                        }
                    }
                }
                else {
                    // If no element container, implies parsable data so show elements.
                    result = voidElementAttributeCompletions(templateInfo, path);
                    if (!result) {
                        result = elementCompletions(templateInfo, path);
                    }
                }
            },
            visitComment: function (ast) { },
            visitExpansion: function (ast) { },
            visitExpansionCase: function (ast) { }
        }, null);
    }
    return result;
}
function attributeCompletions(info, path) {
    var item = path.tail instanceof Element ? path.tail : path.parentOf(path.tail);
    if (item instanceof Element) {
        return attributeCompletionsForElement(info, item.name, item);
    }
    return undefined;
}
function attributeCompletionsForElement(info, elementName, element) {
    var attributes = getAttributeInfosForElement(info, elementName, element);
    // Map all the attributes to a completion
    return attributes.map(function (attr) { return ({
        kind: attr.fromHtml ? 'html attribute' : 'attribute',
        name: nameOfAttr(attr),
        sort: attr.name
    }); });
}
function getAttributeInfosForElement(info, elementName, element) {
    var attributes = [];
    // Add html attributes
    var htmlAttributes = attributeNames(elementName) || [];
    if (htmlAttributes) {
        attributes.push.apply(attributes, htmlAttributes.map(function (name) { return ({ name: name, fromHtml: true }); }));
    }
    // Add html properties
    var htmlProperties = propertyNames(elementName);
    if (htmlProperties) {
        attributes.push.apply(attributes, htmlProperties.map(function (name) { return ({ name: name, input: true }); }));
    }
    // Add html events
    var htmlEvents = eventNames(elementName);
    if (htmlEvents) {
        attributes.push.apply(attributes, htmlEvents.map(function (name) { return ({ name: name, output: true }); }));
    }
    var _a = getSelectors(info), selectors = _a.selectors, selectorMap = _a.map;
    if (selectors && selectors.length) {
        // All the attributes that are selectable should be shown.
        var applicableSelectors = selectors.filter(function (selector) { return !selector.element || selector.element == elementName; });
        var selectorAndAttributeNames = applicableSelectors.map(function (selector) { return ({ selector: selector, attrs: selector.attrs.filter(function (a) { return !!a; }) }); });
        var attrs_1 = flatten(selectorAndAttributeNames.map(function (selectorAndAttr) {
            var directive = selectorMap.get(selectorAndAttr.selector);
            var result = selectorAndAttr.attrs.map(function (name) { return ({ name: name, input: name in directive.inputs, output: name in directive.outputs }); });
            return result;
        }));
        // Add template attribute if a directive contains a template reference
        selectorAndAttributeNames.forEach(function (selectorAndAttr) {
            var selector = selectorAndAttr.selector;
            var directive = selectorMap.get(selector);
            if (directive && hasTemplateReference(directive.type) && selector.attrs.length &&
                selector.attrs[0]) {
                attrs_1.push({ name: selector.attrs[0], template: true });
            }
        });
        // All input and output properties of the matching directives should be added.
        var elementSelector = element ?
            createElementCssSelector(element) :
            createElementCssSelector(new Element(elementName, [], [], undefined, undefined, undefined));
        var matcher = new SelectorMatcher();
        matcher.addSelectables(selectors);
        matcher.match(elementSelector, function (selector) {
            var directive = selectorMap.get(selector);
            if (directive) {
                attrs_1.push.apply(attrs_1, Object.keys(directive.inputs).map(function (name) { return ({ name: name, input: true }); }));
                attrs_1.push.apply(attrs_1, Object.keys(directive.outputs).map(function (name) { return ({ name: name, output: true }); }));
            }
        });
        // If a name shows up twice, fold it into a single value.
        attrs_1 = foldAttrs(attrs_1);
        // Now expand them back out to ensure that input/output shows up as well as input and
        // output.
        attributes.push.apply(attributes, flatten(attrs_1.map(expandedAttr)));
    }
    return attributes;
}
function attributeValueCompletions(info, position, attr) {
    var path = new TemplateAstPath(info.templateAst, position);
    var mostSpecific = path.tail;
    if (mostSpecific) {
        var visitor = new ExpressionVisitor(info, position, attr, function () { return getExpressionScope(info, path, false); });
        mostSpecific.visit(visitor, null);
        if (!visitor.result || !visitor.result.length) {
            // Try allwoing widening the path
            var widerPath_1 = new TemplateAstPath(info.templateAst, position, /* allowWidening */ true);
            if (widerPath_1.tail) {
                var widerVisitor = new ExpressionVisitor(info, position, attr, function () { return getExpressionScope(info, widerPath_1, false); });
                widerPath_1.tail.visit(widerVisitor, null);
                return widerVisitor.result;
            }
        }
        return visitor.result;
    }
}
function elementCompletions(info, path) {
    var htmlNames = elementNames().filter(function (name) { return !(name in hiddenHtmlElements); });
    // Collect the elements referenced by the selectors
    var directiveElements = getSelectors(info).selectors.map(function (selector) { return selector.element; }).filter(function (name) { return !!name; });
    var components = directiveElements.map(function (name) { return ({ kind: 'component', name: name, sort: name }); });
    var htmlElements = htmlNames.map(function (name) { return ({ kind: 'element', name: name, sort: name }); });
    // Return components and html elements
    return uniqueByName(htmlElements.concat(components));
}
function entityCompletions(value, position) {
    // Look for entity completions
    var re = /&[A-Za-z]*;?(?!\d)/g;
    var found;
    var result;
    while (found = re.exec(value)) {
        var len = found[0].length;
        if (position >= found.index && position < (found.index + len)) {
            result = Object.keys(NAMED_ENTITIES)
                .map(function (name) { return ({ kind: 'entity', name: "&" + name + ";", sort: name }); });
            break;
        }
    }
    return result;
}
function interpolationCompletions(info, position) {
    // Look for an interpolation in at the position.
    var templatePath = new TemplateAstPath(info.templateAst, position);
    var mostSpecific = templatePath.tail;
    if (mostSpecific) {
        var visitor = new ExpressionVisitor(info, position, undefined, function () { return getExpressionScope(info, templatePath, false); });
        mostSpecific.visit(visitor, null);
        return uniqueByName(visitor.result);
    }
}
// There is a special case of HTML where text that contains a unclosed tag is treated as
// text. For exaple '<h1> Some <a text </h1>' produces a text nodes inside of the H1
// element "Some <a text". We, however, want to treat this as if the user was requesting
// the attributes of an "a" element, not requesting completion in the a text element. This
// code checks for this case and returns element completions if it is detected or undefined
// if it is not.
function voidElementAttributeCompletions(info, path) {
    var tail = path.tail;
    if (tail instanceof Text) {
        var match = tail.value.match(/<(\w(\w|\d|-)*:)?(\w(\w|\d|-)*)\s/);
        // The position must be after the match, otherwise we are still in a place where elements
        // are expected (such as `<|a` or `<a|`; we only want attributes for `<a |` or after).
        if (match && path.position >= match.index + match[0].length + tail.sourceSpan.start.offset) {
            return attributeCompletionsForElement(info, match[3]);
        }
    }
}
var ExpressionVisitor = (function (_super) {
    __extends(ExpressionVisitor, _super);
    function ExpressionVisitor(info, position, attr, getExpressionScope) {
        _super.call(this);
        this.info = info;
        this.position = position;
        this.attr = attr;
        this.getExpressionScope = getExpressionScope;
        if (!getExpressionScope) {
            this.getExpressionScope = function () { return info.template.members; };
        }
    }
    ExpressionVisitor.prototype.visitDirectiveProperty = function (ast) {
        this.attributeValueCompletions(ast.value);
    };
    ExpressionVisitor.prototype.visitElementProperty = function (ast) {
        this.attributeValueCompletions(ast.value);
    };
    ExpressionVisitor.prototype.visitEvent = function (ast) { this.attributeValueCompletions(ast.handler); };
    ExpressionVisitor.prototype.visitElement = function (ast) {
        var _this = this;
        if (this.attr && getSelectors(this.info) && this.attr.name.startsWith(TEMPLATE_ATTR_PREFIX)) {
            // The value is a template expression but the expression AST was not produced when the
            // TemplateAst was produce so
            // do that now.
            var key_1 = this.attr.name.substr(TEMPLATE_ATTR_PREFIX.length);
            // Find the selector
            var selectorInfo = getSelectors(this.info);
            var selectors = selectorInfo.selectors;
            var selector_1 = selectors.filter(function (s) { return s.attrs.some(function (attr, i) { return i % 2 == 0 && attr == key_1; }); })[0];
            var templateBindingResult = this.info.expressionParser.parseTemplateBindings(key_1, this.attr.value, null);
            // find the template binding that contains the position
            var valueRelativePosition_1 = this.position - this.attr.valueSpan.start.offset - 1;
            var bindings = templateBindingResult.templateBindings;
            var binding = bindings.find(function (binding) { return inSpan(valueRelativePosition_1, binding.span, /* exclusive */ true); }) ||
                bindings.find(function (binding) { return inSpan(valueRelativePosition_1, binding.span); });
            var keyCompletions = function () {
                var keys = [];
                if (selector_1) {
                    var attrNames = selector_1.attrs.filter(function (_, i) { return i % 2 == 0; });
                    keys = attrNames.filter(function (name) { return name.startsWith(key_1) && name != key_1; })
                        .map(function (name) { return lowerName(name.substr(key_1.length)); });
                }
                keys.push('let');
                _this.result = keys.map(function (key) { return { kind: 'key', name: key, sort: key }; });
            };
            if (!binding || (binding.key == key_1 && !binding.expression)) {
                // We are in the root binding. We should return `let` and keys that are left in the
                // selector.
                keyCompletions();
            }
            else if (binding.keyIsVar) {
                var equalLocation = this.attr.value.indexOf('=');
                this.result = [];
                if (equalLocation >= 0 && valueRelativePosition_1 >= equalLocation) {
                    // We are after the '=' in a let clause. The valid values here are the members of the
                    // template reference's type parameter.
                    var directiveMetadata = selectorInfo.map.get(selector_1);
                    var contextTable = this.info.template.query.getTemplateContext(directiveMetadata.type.reference);
                    if (contextTable) {
                        this.result = this.symbolsToCompletions(contextTable.values());
                    }
                }
                else if (binding.key && valueRelativePosition_1 <= (binding.key.length - key_1.length)) {
                    keyCompletions();
                }
            }
            else {
                // If the position is in the expression or after the key or there is no key, return the
                // expression completions
                if ((binding.expression && inSpan(valueRelativePosition_1, binding.expression.ast.span)) ||
                    (binding.key &&
                        valueRelativePosition_1 > binding.span.start + (binding.key.length - key_1.length)) ||
                    !binding.key) {
                    var span = new ParseSpan(0, this.attr.value.length);
                    this.attributeValueCompletions(binding.expression ? binding.expression.ast :
                        new PropertyRead(span, new ImplicitReceiver(span), ''), valueRelativePosition_1);
                }
                else {
                    keyCompletions();
                }
            }
        }
    };
    ExpressionVisitor.prototype.visitBoundText = function (ast) {
        var expressionPosition = this.position - ast.sourceSpan.start.offset;
        if (inSpan(expressionPosition, ast.value.span)) {
            var completions = getExpressionCompletions(this.getExpressionScope(), ast.value, expressionPosition, this.info.template.query);
            if (completions) {
                this.result = this.symbolsToCompletions(completions);
            }
        }
    };
    ExpressionVisitor.prototype.attributeValueCompletions = function (value, position) {
        var symbols = getExpressionCompletions(this.getExpressionScope(), value, position == null ? this.attributeValuePosition : position, this.info.template.query);
        if (symbols) {
            this.result = this.symbolsToCompletions(symbols);
        }
    };
    ExpressionVisitor.prototype.symbolsToCompletions = function (symbols) {
        return symbols.filter(function (s) { return !s.name.startsWith('__') && s.public; })
            .map(function (symbol) { return { kind: symbol.kind, name: symbol.name, sort: symbol.name }; });
    };
    Object.defineProperty(ExpressionVisitor.prototype, "attributeValuePosition", {
        get: function () {
            return this.position - this.attr.valueSpan.start.offset - 1;
        },
        enumerable: true,
        configurable: true
    });
    return ExpressionVisitor;
}(NullTemplateVisitor));
function getSourceText(template, span) {
    return template.source.substring(span.start, span.end);
}
function nameOfAttr(attr) {
    var name = attr.name;
    if (attr.output) {
        name = removeSuffix(name, 'Events');
        name = removeSuffix(name, 'Changed');
    }
    var result = [name];
    if (attr.input) {
        result.unshift('[');
        result.push(']');
    }
    if (attr.output) {
        result.unshift('(');
        result.push(')');
    }
    if (attr.template) {
        result.unshift('*');
    }
    return result.join('');
}
var templateAttr = /^(\w+:)?(template$|^\*)/;
function createElementCssSelector(element) {
    var cssSelector = new CssSelector();
    var elNameNoNs = splitNsName(element.name)[1];
    cssSelector.setElement(elNameNoNs);
    for (var _i = 0, _a = element.attrs; _i < _a.length; _i++) {
        var attr = _a[_i];
        if (!attr.name.match(templateAttr)) {
            var _b = splitNsName(attr.name), _ = _b[0], attrNameNoNs = _b[1];
            cssSelector.addAttribute(attrNameNoNs, attr.value);
            if (attr.name.toLowerCase() == 'class') {
                var classes = attr.value.split(/s+/g);
                classes.forEach(function (className) { return cssSelector.addClassName(className); });
            }
        }
    }
    return cssSelector;
}
function foldAttrs(attrs) {
    var inputOutput = new Map();
    var templates = new Map();
    var result = [];
    attrs.forEach(function (attr) {
        if (attr.fromHtml) {
            return attr;
        }
        if (attr.template) {
            var duplicate = templates.get(attr.name);
            if (!duplicate) {
                result.push({ name: attr.name, template: true });
                templates.set(attr.name, attr);
            }
        }
        if (attr.input || attr.output) {
            var duplicate = inputOutput.get(attr.name);
            if (duplicate) {
                duplicate.input = duplicate.input || attr.input;
                duplicate.output = duplicate.output || attr.output;
            }
            else {
                var cloneAttr = { name: attr.name };
                if (attr.input)
                    cloneAttr.input = true;
                if (attr.output)
                    cloneAttr.output = true;
                result.push(cloneAttr);
                inputOutput.set(attr.name, cloneAttr);
            }
        }
    });
    return result;
}
function expandedAttr(attr) {
    if (attr.input && attr.output) {
        return [
            attr, { name: attr.name, input: true, output: false },
            { name: attr.name, input: false, output: true }
        ];
    }
    return [attr];
}
function lowerName(name) {
    return name && (name[0].toLowerCase() + name.substr(1));
}
//# sourceMappingURL=completions.js.map