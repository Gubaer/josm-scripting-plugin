
var util = require("josm/util");
var LatLon = org.openstreetmap.josm.data.coor.LatLon;

exports.forClass = org.openstreetmap.josm.data.osm.Node;
exports.mixin = {
	id: {
		get: function() {
				return this.getUniqueId();
		}		
	},
	
	lat: {
		get: function() {
			if (this.isIncomplete() || this.getCoor() == null) return undefined;
			return this.getCoor().lat();
		},
		set: function(lat) {
			util.assert(! this.isIncomplete(), "Can''t set lat on an incomplete node");
			util.assert(util.isNumber(lat), "Expected a number, got {0}", lat);
			util.assert(LatLon.isValidLat(lat), "Expected a valid lat in the range [-90,90], got {0}", lat);
			var coor = this.getCoor();
			if (coor == null) coor = new LatLon(0,0);
			coor = new LatLon(lat, coor.lon());
			this.setCoor(coor);
		}	
	},
		
	lon: {
		get: function() {
			if (this.isIncomplete() || this.getCoor() == null) return undefined;
			return n.getCoor().lon;
		},
		set: function(lon) {
			util.assert(! this.isIncomplete(), "Can''t set lon on an incomplete node");
			util.assert(util.isNumber(lon), "Expected a number, got {0}", lat);
			util.assert(LatLon.isValidLon(lon), "Expected a valid lon in the range [-180,180], got {0}", lon);
			var coor = this.getCoor();
			if (coor == null) coor = new LatLon(0,0);
			coor = new LatLon(coor.lat, lon);
			this.setCoor(coor);
		}
	},
	
	east: {
		get: function() {
			if (this.isIncomplete() || this.getEastNorth() == null) return undefined;
			return n.getEastNorth().east;
		}
	},
	
	north: {
		get: function() {
			if (this.isIncomplete() || this.getEastNorth() == null) return undefined;
			return n.getEastNorth().north;
		}
	},
	
	pos: {
		get: function() {
			if (this.isIncomplete() || this.getCoor() == null) return undefined;
			return this.getCoor();
		},
		set: function(coor) {
			util.assert(util.isSomething(coor), "value must not be null or undefined");
			if (coor instanceof LatLon) {
				this.setCoor(coor);
			} else if (typeof coor === "object") {
				util.assert(coor.hasOwnProperty("lat"), "Missing mandatory property 'lat' in {0}", coor);
				util.assert(util.isNumber(coor.lat), "Expected a number in property 'lat' in {0}, got {1}", coor, coor.lat);
				util.assert(LatLon.isValidLat(coor.lat), "Illegal lat value for property 'lat' in {0}, got {1}", coor, coor.lat);
				util.assert(coor.hasOwnProperty("lon"), "Missing mandatory property 'lon' in {0}", coor);
				util.assert(util.isNumber(coor.lon), "Expected a number in property 'lon' in {0}, got {1}", coor, coor.lon);
				util.assert(LatLon.isValidLat(coor.lon), "Illegal lon value for property 'lon' in {0}, got {1}", coor, coor.lon);
				this.setCoor(new LatLon(coor.lat, coor.lon));
			} else {
				util.assert(false, "Unexpected type of value, got {0}", coor);
			}
		}
	}
};

