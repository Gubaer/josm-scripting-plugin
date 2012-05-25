exports.forClass = org.openstreetmap.josm.data.osm.OsmPrimitive;

exports.mixin = {
	id: {
		get: function() {
			return this.getUniqueId();
		}
	}
};