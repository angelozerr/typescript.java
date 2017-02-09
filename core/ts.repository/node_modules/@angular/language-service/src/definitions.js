/**
 * @license
 * Copyright Google Inc. All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license that can be
 * found in the LICENSE file at https://angular.io/license
 */
import { locateSymbol } from './locate_symbol';
export function getDefinition(info) {
    var result = locateSymbol(info);
    return result && result.symbol.definition;
}
//# sourceMappingURL=definitions.js.map