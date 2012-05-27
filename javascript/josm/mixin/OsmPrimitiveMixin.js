
var User = org.openstreetmap.josm.data.osm.User;

exports.forClass = org.openstreetmap.josm.data.osm.OsmPrimitive;

exports.mixin = {
	id: {
		get: function() {
			return this.getUniqueId();
		}
	},
	
	isLocal: {
		get: function() {
			return this.isNew();
		}
	},
	
	isGlobal: {
		get: function() {
			return ! this.isNews();
		}
	},
	
	dataSet: {
		get: function() {
			var ds = this.getDataSet();
			return ds == null? undefined: ds;
		}
	},
	
	
	user: {
		get: function() {
			var user = this.getUser();
			return user == null ? undefined : user;
		},
		set: function(val) {
			util.assert(util.isSomething(user), "null or undefined not allowed");
			if (val instanceof User){
				this.setUser(val);
			} else if (util.isString(val)) {
				var user = User.getByName(val);
				util.assert(user != null, "Can''t set user with name ''{0}''. User doesn''t exist.", val);
				this.setUser(user);
			} else if (util.isNumber(val)) {
				var user = User.getById(val);
				util.assert(user != null, "Can''t set user with id {0}'. User doesn''t exist.", val);
				this.setUser(user);
			} else {
				util.assert(false, "Unexpected type of value, got {0}", val);
			}
		}
	},
	
	changesetId: {
		get: function() {
			var cid = this.getChangesetId();
			return cid == 0 ? undefined: cid;
		},
		
		set: function(val) {
			util.assert(util.isSomething(val), "null or undefined not allowed");
			util.assert(util.isNumber(val), "Expected a number, got {0}", val);
			util.assert(val > 0, "Expected a number > 0, got {0}", val);
			this.setChangesetId(id);
		}		
	},
	
	timestamp: {
		get: function() {
			if (this.isTimestampEmpty()) return undefined;
			return this.getTimestamp();
		}
	}
};

exports.mixin.isNew = exports.mixin.isLocal;