
var fail = function(message) {
	var e = new Error();
	e.name = "TestFailure";
	e.message = message;
	throw e;
};

var assertDefined = function(val, msg) {
	if (val === void(0)) {
		fail(msg);
	}
};

var assertUndefined = function(val, msg) {
	if (val !== void(0)) {
		fail(msg);
	}
};


var msg = function() {    	
	switch(arguments.length) {
	case 0: return "";
	case 1: return arguments[0] + "";
	default: return MessageFormat.format(arguments[0] + "", Array.prototype.slice.call(arguments,1));
	}
};


var assert = function(cond) {
	! cond && fail(msg(Array.prototype.splice(arguments,1)));
};

var run = function(testcase) {
	try {
		testcase.call();
		print("PASS - " + testcase.testname);
	} catch(e) {		
		print("FAIL - " + testcase.testname + "' - " + e);
	}
};

var tests = [];

var test = function(name, test) {
	test.testname = name;
	tests.push(test);
};

var runtests = function() {
	for(var i =0; i< tests.length; i++) {
		run(tests[i]);
	}
};