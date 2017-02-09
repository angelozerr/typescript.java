/**
 * @license
 * Copyright Google Inc. All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license that can be
 * found in the LICENSE file at https://angular.io/license
 */
import { locateSymbol } from './locate_symbol';
export function getHover(info) {
    var result = locateSymbol(info);
    if (result) {
        return { text: hoverTextOf(result.symbol), span: result.span };
    }
}
function hoverTextOf(symbol) {
    var result = [{ text: symbol.kind }, { text: ' ' }, { text: symbol.name, language: symbol.language }];
    var container = symbol.container;
    if (container) {
        result.push({ text: ' of ' }, { text: container.name, language: container.language });
    }
    return result;
}
//# sourceMappingURL=hover.js.map