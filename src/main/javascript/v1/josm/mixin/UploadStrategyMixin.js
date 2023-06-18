/**
 * This module is auto-loaded by the scripting plugin and mixed into the
 * native java class {@class org.openstreetmap.josm.io.UploadStrategy}.
 *
 * @module josm/mixin/UploadStrategyMixin
 */
var util = require("josm/util");
var UploadStrategy = org.openstreetmap.josm.io.UploadStrategy;

/**
 * This mixin provides additional properties and methods which you can 
 * invoke on an instance of {@class org.openstreetmap.josm.io.UploadStrategy}.
 *
 * @mixin UploadStrategyMixin
 * @forClass org.openstreetmap.josm.io.UploadStrategy
 */
exports.mixin = {};
exports.forClass = org.openstreetmap.josm.io.UploadStrategy;


/**
 * Checks whether a value can be converted into an
 * {@class org.openstreetmap.josm.io.UploadStrategy},
 * converts and normalizes it, and returns the converted value.
 * Throws an error, if <code>value</code> can't be converted.
 *
 * Accepts one of the following values:
 * <dl>
 *   <dt><code class="signature">a
 *       {@class org.openstreetmap.josm.io.UploadStrategy}</code></dt>
 *   <dd>just replies value</dd>
 *   <dt><code class="signature">a string</code></dt>
 *   <dd>One of the following strings:
 *     <ul>
 *          <li>individualobjects</li>
 *          <li>chunked</li>
 *          <li>singlerequest</li>
 *       </ul>
 *
 *   Replies the matching enumeration value
 *       {@class org.openstreetmap.josm.io.UploadStrategy}</dd>
 * </dl>
 *
 * Throws an error, if value is null or undefined.
 *
 * @example
 * var UploadStrategy = org.openstreetmap.josm.io.UploadStrategy;
 * var strategy = UploadStrategy.from("individualobjects");
 *
 * @param {org.openstreetmap.josm.io.UploadStrategy|string} value
 *         the value to normalize
 * @memberOf module:josm/mixin/UploadStrategyMixin~UploadStrategyMixin
 * @static
 * @function
 * @name from
 * @returns {org.openstreetmap.josm.io.UploadStrategy}
 * @summary Convert value to {@class org.openstreetmap.josm.io.UploadStrategy}
 */
exports.mixin.from = function(value) {
    util.assertSomething(value, "value: must not be null or undefined");
    if (util.isString(value)) {
        value = util.trim(value).toLowerCase();
        var values = UploadStrategy.values();
        for (var i =0; i< values.length; i++) {
            if (values[i].getPreferenceValue().startsWith(value)) return values[i];
        }
        util.assert(false,
            "value: can''t convert string ''{0}'' to an UploadStrategy",
            value);
    } else if (value instanceof UploadStrategy) {
        return value;
    } else {
        util.assert(false,
            "value: can''t convert value to an UploadStrategy, got {0}",
            value);
    }
};
exports.mixin.from.static = true;

/**
 * Replies true, if {@class org.openstreetmap.josm.io.UploadStrategy},
 * is equal to a specific enumeration value or to a string value.
 *
 * Accepts one of the following values:
 * <dl>
 *   <dt><code class="signature">a
 *   {@class org.openstreetmap.josm.io.UploadStrategy}</code></dt>
 *   <dd>replies true, if </dd>
 *   <dt><code class="signature">a string</code></dt>
 *   <dd>One of the following strings:
 *     <ul>
 *          <li>individualobjects</li>
 *          <li>chunked</li>
 *          <li>singlerequest</li>
 *       </ul>
 *
 *   Replies true, if the value is a prefix of the preference value.
 *   </dd>
 * </dl>
 *
 * @example
 * var UploadStrategy = org.openstreetmap.josm.io.UploadStrategy;
 * var strategy = UploadStrategy.INDIVIDUAL_OBJECTS_STRATEGY;
 * strategy.is("indiv");             // true
 * strategy.is("individualobjects"); // true
 * strategy.is("chunked");           // false
 *
 * @param {org.openstreetmap.josm.io.UploadStrategy | string} value
 *         the value to check
 * @memberOf module:josm/mixin/UploadStrategyMixin~UploadStrategyMixin
 * @function
 * @name is
 * @instance
 * @returns {boolean} true, if string value matches
 * @summary Checks equality with string values
 */
exports.mixin.is = function(value) {
    if (util.isNothing(value)) return false;
    if (util.isString(value)) {
        value = util.trim(value).toLowerCase();
        return this.getPreferenceValue().startsWith(value);
    } else if (value instanceof UploadStrategy) {
        return this.equals(value);
    } else {
        return false;
    }
};
