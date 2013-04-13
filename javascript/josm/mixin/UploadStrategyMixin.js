(function() {
/**
 * <p>This module is auto-loaded by the scripting plugin and mixed into the 
 * native java class {@class org.openstreetmap.josm.gui.io.UploadStrategy}.</p>
 * 
 * @module josm/mixin/UploadStrategyMixin
 */
var util = require("josm/util");
var UploadStrategy = org.openstreetmap.josm.gui.io.UploadStrategy;

/**
 * <p>This mixin 
 * provides additional properties and methods which you can invoke on an instance of
 * {@class org.openstreetmap.josm.gui.io.UploadStrategy}.</p>
 *   
 * @mixin UploadStrategyMixin
 * @forClass org.openstreetmap.josm.gui.io.UploadStrategy
 * @memberof josm/mixin/UploadStrategyMixin
 */
var mixin = {};

/**
 * <p>Checks whether a value can be converted into an  {@class org.openstreetmap.josm.gui.io.UploadStrategy},
 * converts and normalizes it, and returns the converted value. Throws an error, if <code>value</code> can't
 * be converted.</p>
 * 
 * Accepts one of the following values:
 * <dl>
 *   <dt><code class="signature">a {@class org.openstreetmap.josm.gui.io.UploadStrategy}</code></dt>
 *   <dd>just replies value</dd>
 *   <dt><code class="signature">a string</code></dt> 
 *   <dd>One of the following strings: 
 *     <ul>
 *          <li>individualobjects</li>
 *          <li>chunked</li>
 *          <li>singlerequest</li>
 *       </ul>
 *   
 *   Replies the matching enumeration value {@class org.openstreetmap.josm.gui.io.UploadStrategy}</dd>
 * </dl>
 * 
 * Throws an error, if value is null or undefined.
 * 
 * @example
 * var UploadStrategy = org.openstreetmap.josm.gui.io.UploadStrategy;
 * var strategy = UploadStrategy.from("individualobjects");
 * 
 * @param {org.openstreetmap.josm.gui.io.UploadStrategy|string} value  the value to normalize
 * @memberOf UploadStrategyMixin
 * @static
 * @function
 * @name from
 * @type org.openstreetmap.josm.gui.io.UploadStrategy
 * @summary Convert value to  {@class org.openstreetmap.josm.gui.io.UploadStrategy}
 */
mixin.from = function(value) {
	util.assertSomething(value, "value: must not be null or undefined");
	if (util.isString(value)) {
		value = util.trim(value).toLowerCase();
		var values = UploadStrategy.values();
		for (var i =0; i< values.length; i++) {
			if (values[i].getPreferenceValue().startsWith(value)) return values[i];
		}
		util.assert(false, "value: can''t convert string ''{0}'' to an UploadStrategy", value);
	} else if (value instanceof UploadStrategy) {
		return value;
	} else {
		util.assert(false, "value: can''t convert value to an UploadStrategy, got {0}", value);
	}
};
mixin.from.static = true;

/**
 * <p>Replies true, if {@class org.openstreetmap.josm.gui.io.UploadStrategy},
 * is equal to a specific enumeration value or to a string value. </p>
 * 
 * Accepts one of the following values:
 * <dl>
 *   <dt><code class="signature">a {@class org.openstreetmap.josm.gui.io.UploadStrategy}</code></dt>
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
 * var UploadStrategy = org.openstreetmap.josm.gui.io.UploadStrategy;
 * var strategy = UploadStrategy.INDIVIDUAL_OBJECTS_STRATEGY;
 * strategy.is("indiv");             // true
 * strategy.is("individualobjects"); // true
 * strategy.is("chunked");           // false 
 * 
 * @param {org.openstreetmap.josm.gui.io.UploadStrategy|string} value  the value to check
 * @memberOf UploadStrategyMixin
 * @function
 * @name is
 * @type boolean
 * @summary Checks equality with string values
 */
mixin.is = function(value) {
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

exports.forClass=org.openstreetmap.josm.gui.io.UploadStrategy;
exports.mixin = mixin;

}());