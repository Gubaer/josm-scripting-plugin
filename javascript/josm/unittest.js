/**
* @module josm/unittest
*/

var util = require("josm/util");

var out = java.lang.System.out;

exports.TestCase = function(name, test) {
	this._name = name;
	this._test = test;
};

exports.test = function() {
	switch(arguments.length) {
	case 1:
		return new exports.TestCase("No Name", arguments[0]);
	case 2:
		return new exports.TestCase(arguments[0], arguments[1]);
	default:
		util.assert(false, "Unsupported arguments");	
	}
};

exports.TestCase.prototype.run = function() {
	try {
		this._test();
		out.println("PASS - " + this._name);
	} catch(e) {
		out.println("FAIL - " + this._name + " - " + e.toSource() + (e.description ? " - " + e.description : ""));
	}
};

exports.Suite = function() {
	this._tests = [];	
};

exports.suite = function() {
	var suite = new exports.Suite();
	for(var i = 0; i< arguments.length; i++) {
		var test = arguments[i];
		if (test instanceof exports.TestCase) {
			suite.add(test);
		} else if (util.isFunction(test)) {
			suite.add(exports.test(test));
		} else {
			util.assert(false, "Unsupported arguments");
		}		
	};
	return suite;
};

exports.Suite.prototype.add = function(test) {
	this._tests.push(test);
};

exports.Suite.prototype.run = function() {
	for (var i = 0; i < this._tests.length; i++) {
		this._tests[i].run();
	}
};


exports.expectError = function(f) {
	try {
		f();
		util.assert(false, "Expected an error, didn't get one.");
	} catch(e) {
		// OK 
	}
};

exports.expectAssertionError = function(f) {
	try {
		f();
		util.assert(false, "Expected an error, didn't get one.");
	} catch(e) {
		if (e.name != "AssertionError") {
			util.assert(false, "Expected AssertionError, got {0}", e.toSource());
		} 
	}
};