var tu = require("josm/unittest");
var util = require("josm/util");

tu.suite("josm/util - each",
	tu.test("each - array", function() {
		var a = [1,2,3];
		var sum = 0;
		util.each(a, function(n){
			sum += n;
		});
		util.assert(sum == 6, "should be 6");
	}),
	tu.test("each - List", function() {
		var list = new java.util.ArrayList();
		list.add(1); list.add(2); list.add(3);
		var sum = 0;
		util.each(list, function(n){
			sum += n.intValue();
		});
		util.assert(sum == 6, "should be 6");
	}),
	tu.test("each - arguments", function() {
		var sum = 0;
		function doit() {
			util.each(arguments, function(n) {
				sum += n;
			});
		}
		doit(1,2,3);
		util.assert(sum == 6, "should be 6");
	}),
	tu.test("each - four edge cases with null/undefined", function() {
		util.each(null, function() {});
		util.each(undefined, function() {});
		util.each([], null);
		util.each([], undefined);
	}),
	tu.test("each - unexpected collection", function() {
		tu.expectAssertionError("string as collection", function() {
			util.each("1, 2, 3", function() {});
		});
	})	
).run();