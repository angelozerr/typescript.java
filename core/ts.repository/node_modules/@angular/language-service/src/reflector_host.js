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
import { CompilerHost, ModuleResolutionHostAdapter } from '@angular/compiler-cli';
var ReflectorModuleModuleResolutionHost = (function () {
    function ReflectorModuleModuleResolutionHost(host) {
        var _this = this;
        this.host = host;
        if (host.directoryExists)
            this.directoryExists = function (directoryName) { return _this.host.directoryExists(directoryName); };
    }
    ReflectorModuleModuleResolutionHost.prototype.fileExists = function (fileName) { return !!this.host.getScriptSnapshot(fileName); };
    ReflectorModuleModuleResolutionHost.prototype.readFile = function (fileName) {
        var snapshot = this.host.getScriptSnapshot(fileName);
        if (snapshot) {
            return snapshot.getText(0, snapshot.getLength());
        }
    };
    return ReflectorModuleModuleResolutionHost;
}());
export var ReflectorHost = (function (_super) {
    __extends(ReflectorHost, _super);
    function ReflectorHost(getProgram, serviceHost, options) {
        _super.call(this, null, options, new ModuleResolutionHostAdapter(new ReflectorModuleModuleResolutionHost(serviceHost)));
        this.getProgram = getProgram;
    }
    Object.defineProperty(ReflectorHost.prototype, "program", {
        get: function () { return this.getProgram(); },
        set: function (value) {
            // Discard the result set by ancestor constructor
        },
        enumerable: true,
        configurable: true
    });
    return ReflectorHost;
}(CompilerHost));
//# sourceMappingURL=reflector_host.js.map