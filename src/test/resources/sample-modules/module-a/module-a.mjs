/*
 * Module 'module-a' exports a single function 'add'.
 *
 * Here's a sample script using the module
 * <pre>
 * import {add} from 'module-a'
 * import * as console from 'josm/scriptingconsole'
 * const b = add(1,1)
 * console.println(`result: ${b}`)
 * </pre>
 */
export function add(a, b) {
    return a + b
}