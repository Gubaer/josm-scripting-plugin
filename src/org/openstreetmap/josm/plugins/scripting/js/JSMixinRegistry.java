package org.openstreetmap.josm.plugins.scripting.js;

import java.util.HashMap;
import java.util.Map;

import org.mozilla.javascript.RhinoException;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Wrapper;

/**
* <p>Mixins are implemented as CommonJS modules. A mixin has to export two properties:</p>
* <ul>
*   <li><strong>forClass</strong> -  the value must be java class for which the mixin is defined</li>
*   <li><strong>mixin</strong> - an object with object descriptors. The value for each property is
*   either a function (this defines a mixed in method) or a property descriptor with eithe a value or
*   a getter and an (optional) setter function (this defines a mixed in property).</li>
* </ul>
* <p>Mixins can include instance and class-level ("static") extensions, see the example below.</p>
*
* <strong>Example</strong>
* <pre>
* // the class which we extend
* exports.forClass = org.openstreetmap.josm.data.osm.Node;
*
* // a mixin defining two new properties and an additional method
* //
* exports.mixin = {
*     // a read-only property
*     id: {
*       get: function() {return this.getUniqueId();}
*     },
*     // a read-write property
*     lat: {
*       get: function() {
*          return this.getCoor().lat();
*       },
*       set: function(lat) {
*         var coor = this.getCoor();
*         coor=new LatLon(lat, coor.lon());
*         this.setCoor(coor);
*       }
*     },
*     // a function
*     echo: function() {
*         java.lang.System.out.println("node: " + this.toString());
*     },
*
*     // a static read-only property
*     instance: {
*        static: true;
*        get: function() {...}
*     },
*
*     // a static function
*     staticFunction: function() {...};
*     staticFunction.static = true;  // mark it as static function
* };
* </pre>
*
*/
public class JSMixinRegistry {

    static private Map<Class<?>, Scriptable> MIXINS = new HashMap<Class<?>, Scriptable>();

    static private Object unwrap(Object o) {
        if (o instanceof Wrapper) {
            o = ((Wrapper)o).unwrap();
        }
        return o;
    }

    /**
     * <p>Register a mixin for a java class. The mixin is given by the properties in the
     * scriptable object <code>mixin</code></p>.
     *
     * @param parentScope the parent scope to be used when loading the module
     * @param mixin the mixin
     */
    static public void registerMixin(Class<?> clazz, Scriptable mixin) {
        MIXINS.put(clazz, mixin);
    }

    /**
     * Replies the mixin for the class <code>cls</code>, or null, if no mixin for
     * this class is available.
     *
     * @param cls the class
     * @return the mixin
     */
    static public Scriptable get(Class<?> cls) {
        return MIXINS.get(cls);
    }

    /**
     * <p>Load and register a mixin for a java class. Tries to load the mixin
     * as CommonJS module with name <code>moduleName</code>. This module is expected
     * to export to properties:</p>
     * <ol>
     *   <li><code>forClass</code>  - the value is the java class for which this module provides
     *   a mixin</li>
     *   <li><code>mixin</code> - the mixin implementation</li>
     * </ol>
     *
     * @param parentScope the parent scope to be used when loading the module
     * @param moduleName the module name
     * @throws JSMixinException thrown, if an exception occurs
     */
    static public void loadJSMixin(Scriptable parentScope, String moduleName) throws JSMixinException{
        Scriptable module;
        try {
            module = RhinoEngine.getInstance().require(moduleName);
        } catch(RhinoException e){
            throw JSMixinException.make(e, "Failed to load module ''{0}'' providing a JS mixin.",moduleName);
        }
        Object forClass = module.get("forClass", module);
        if (forClass == Scriptable.NOT_FOUND) {
            throw JSMixinException.make("Property ''{0}'' in module ''{1}'' not found.", "forClass", moduleName);
        }
        forClass = unwrap(forClass);
        if (! (forClass instanceof Class<?>)) {
            throw JSMixinException.make("Unexpected value for property ''{0}'' in module ''{1}''. Expected a java class object, got {2}", "forClass", moduleName, forClass);
        }
        Class<?> clazz = (Class<?>)forClass;

        Object mixin = module.get("mixin", module);
        if (mixin == Scriptable.NOT_FOUND) {
            throw JSMixinException.make("Property ''{0}'' in module ''{1}'' not found.", "mixin", moduleName);
        }
        if (! (mixin instanceof Scriptable)) {
            throw JSMixinException.make("Unexpected value for mixin ''mixin'' in module ''{0}''. Expected a Scriptable, got {1}", "mixin", moduleName, mixin);
        }
        registerMixin(clazz, (Scriptable)mixin);
    }
}
