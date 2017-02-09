/**
 * @license
 * Copyright Google Inc. All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license that can be
 * found in the LICENSE file at https://angular.io/license
 */
export declare class AstPath<T> {
    private path;
    constructor(path: T[]);
    empty: boolean;
    head: T | undefined;
    tail: T | undefined;
    parentOf(node: T): T | undefined;
    childOf(node: T): T | undefined;
    first<N extends T>(ctor: {
        new (...args: any[]): N;
    }): N | undefined;
    push(node: T): void;
    pop(): T;
}
