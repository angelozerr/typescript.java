/**
 * @license
 * Copyright Google Inc. All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license that can be
 * found in the LICENSE file at https://angular.io/license
 */
export var AstPath = (function () {
    function AstPath(path) {
        this.path = path;
    }
    Object.defineProperty(AstPath.prototype, "empty", {
        get: function () { return !this.path || !this.path.length; },
        enumerable: true,
        configurable: true
    });
    Object.defineProperty(AstPath.prototype, "head", {
        get: function () { return this.path[0]; },
        enumerable: true,
        configurable: true
    });
    Object.defineProperty(AstPath.prototype, "tail", {
        get: function () { return this.path[this.path.length - 1]; },
        enumerable: true,
        configurable: true
    });
    AstPath.prototype.parentOf = function (node) { return this.path[this.path.indexOf(node) - 1]; };
    AstPath.prototype.childOf = function (node) { return this.path[this.path.indexOf(node) + 1]; };
    AstPath.prototype.first = function (ctor) {
        for (var i = this.path.length - 1; i >= 0; i--) {
            var item = this.path[i];
            if (item instanceof ctor)
                return item;
        }
    };
    AstPath.prototype.push = function (node) { this.path.push(node); };
    AstPath.prototype.pop = function () { return this.path.pop(); };
    return AstPath;
}());
//# sourceMappingURL=ast_path.js.map