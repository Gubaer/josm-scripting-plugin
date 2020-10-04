
var util = require("josm/util");
var OsmPrimitiveType = org.openstreetmap.josm.data.osm.OsmPrimitiveType;

var predicates = {};

predicates.isNode = function(){
	return function(obj) {
		return obj.getType() == OsmPrimitiveType.NODE;
	};
};

predicates.isWay = function(){
	return function(obj) {
		return obj.getType() == OsmPrimitiveType.WAY;
	};
};

predicates.isRelation = function(){
	return function(obj) {
		return obj.getType() == OsmPrimitiveType.RELATION;
	};
};

predicate.type = function() {
	var types = new HashSet();
	function normalizeType(type) {
		if(util.isNothing(type)) return undefined;
		if (util.isString(type)) {
			type = util.trim(type).toLowerCase();
			try {
				return OsmPrimitiveType.fromApiTypeName(type);
			} catch(e) {
				util.assert(false, "type: unsupported OSM primitive type ''{0}''", type);
			}
		} else if (type instanceof OsmPrimitiveType) {
			util.assert(type == OsmPrimitiveType.NODE || type == OsmPrimitiveType.WAY || type == OsmPrimitiveType.RELATION, "type: unsupported OSM primitive type, got {0}", type);
			return type;
		} else {
			util.assert(false, "type: unsupported value, got {0}", type);
		}
	};
	each(arguments, function(arg) {
		var type = normalizeType(arg);
		if (type) types.add(type);
	});
	
	return function(obj) {
		return types.contains(obj.getType()); 
	};
};

predicates.and = function(){
	var args = [];
	function normalize(arg) {
		if (util.isNothing(arg)) return;
		if (util.isFunction(arg)) {
			args.push(arg);
		} else if (isCollection(arg)) {
			each(arg, normalize);
		} else {
			util.assert(false, "Unexpected predicate, got {0}",arg);
		}
	}
	each(arguments, normalize);
	return function(obj) {
		for (var i=0; i < args.length; i++){
			if (!args[i](obj)) return false;
		}
		return true;
	};
};

predicates.or = function(){
	var args = [];
	function normalize(arg) {
		if (util.isNothing(arg)) return;
		if (util.isFunction(arg)) {
			args.push(arg);
		} else if (isCollection(arg)) {
			each(arg, normalize);
		} else {
			util.assert(false, "Unexpected predicate, got {0}",arg);
		}
	}
	each(arguments, normalize);
	return function(obj) {
		for (var i=0; i < args.length; i++){
			if (args[i](obj)) return true;
		}
		return false;
	};
};

predicate.not = function() {
	util.assert(arguments.length == 1, "Expected 1 argument, got {0}", arguments.length);
	var predicate = arguments[0];
	util.assert(util.isFunction(predicate), "predicate: expected a function, got {0}", predicate);	
	return function(obj) {
		return !predicate(obj);
	};
};

predicate.ANY = new Object();

predicate.ALWAYS = function(obj) {return true;};
predicate.NEVER  = function(obj) {return false;};

predicate.tag = function() {
	function tag_1(tag) {
		if (tag == ANY) {
			return ALWAYS;	
		} else {
			return function(obj) {
				return obj.has(tag);
			}
		}	
	};
};

