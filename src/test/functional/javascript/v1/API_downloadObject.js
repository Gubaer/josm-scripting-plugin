/*
 * Functional test for downloading objects
 * 
 * Load in the JOSM scripting console and run.
 */

var Api = require("josm/api").Api;
var util = require("josm/util");
var SimplePrimitiveId = org.openstreetmap.josm.data.osm.SimplePrimitiveId;
var OsmPrimitiveType = org.openstreetmap.josm.data.osm.OsmPrimitiveType;

// a sample way 
var nummeric_id = 167898265;
var type = "way";

function downloadTest_001() {	
	var primitive = Api
		.downloadObject({id: nummeric_id, type: type}, {version: 1})
		.get(nummeric_id, type);
	
	util.assert(primitive.version == 1, "Expected downloaded version 1, got {0}",
			primitive.version)
}

function downloadTest_002() {	
	var id = new SimplePrimitiveId(nummeric_id, OsmPrimitiveType.WAY);
	
	var primitive = Api
		.downloadObject(id, {version: 1})
		.get(nummeric_id, type);
	
	util.assert(primitive.version == 1, "Expected downloaded version 1, got {0}",
			primitive.version)
}

function downloadTest_003() {	
	var id = new SimplePrimitiveId(nummeric_id, OsmPrimitiveType.WAY);
	
	var primitive = Api
		.downloadObject(id)
		.get(nummeric_id, type);
	
	util.assert(primitive.version != 1, "Expected downloaded version != 1, got {0}",
			primitive.version)
}

function downloadTest_004() {
	var primitive = Api
		.downloadObject({id: nummeric_id, type: type})
		.get(nummeric_id, type);
	
	util.assert(primitive.version != 1, "Expected downloaded version != 1, got {0}",
			primitive.version)
}

downloadTest_001();
downloadTest_002();
downloadTest_003();
downloadTest_004();
