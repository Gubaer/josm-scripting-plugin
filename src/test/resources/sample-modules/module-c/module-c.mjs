import {add} from 'module-a/module-a.mjs'
import {mult} from 'module-b/module-b.mjs'

export function computeResult() {
    const r1 = add(1,1)
    const r2 = mult(2,2)
    const result = [r1, r2]
    return result
}
