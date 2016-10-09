var tu = require("josm/unittest");
var util = require("josm/util");
var UploadStrategy = org.openstreetmap.josm.gui.io.UploadStrategy;

var suites = [];

suites.push(tu.suite("from()",
    tu.test("with valid enumeration value", function() {
        var strategy = UploadStrategy.from(
            UploadStrategy.INDIVIDUAL_OBJECTS_STRATEGY);
        util.assert(strategy == UploadStrategy.INDIVIDUAL_OBJECTS_STRATEGY,
            "unexpected result");
    }),

    tu.test("with a valid string", function() {
        var strategy;
        strategy = UploadStrategy.from("individualobjects");
        util.assert(strategy == UploadStrategy.INDIVIDUAL_OBJECTS_STRATEGY,
            "1 - unexpected result");

        strategy = UploadStrategy.from("chunked");
        util.assert(strategy == UploadStrategy.CHUNKED_DATASET_STRATEGY,
            "2 - unexpected result");

        strategy = UploadStrategy.from("singlerequest");
        util.assert(strategy == UploadStrategy.SINGLE_REQUEST_STRATEGY,
            "3 - unexpected result");

        // leading, trailing whitespace - case insensitive
        strategy = UploadStrategy.from("  IndiviDualobjects  ");
        util.assert(strategy == UploadStrategy.INDIVIDUAL_OBJECTS_STRATEGY,
            "4 - unexpected result");

        // prefix
        strategy = UploadStrategy.from("indiv");
        util.assert(strategy == UploadStrategy.INDIVIDUAL_OBJECTS_STRATEGY,
            "5 - unexpected result");

        strategy = UploadStrategy.from("c");
        util.assert(strategy == UploadStrategy.CHUNKED_DATASET_STRATEGY,
            "6 - unexpected result");

        strategy = UploadStrategy.from("single");
        util.assert(strategy == UploadStrategy.SINGLE_REQUEST_STRATEGY,
            "7 - unexpected result");
    }),

    tu.test("with null or undefined", function() {
        var strategy;

        tu.expectAssertionError("null", function() {
            strategy = UploadStrategy.from(null);
        });

        tu.expectAssertionError("undefined", function() {
            strategy = UploadStrategy.from(undefined);
        });
    }),

    tu.test("illegal strings", function() {
        var strategy;

        tu.expectAssertionError("typo", function() {
            strategy = UploadStrategy.from("individualobject");
        });

        tu.expectAssertionError("unrelated string value", function() {
            strategy = UploadStrategy.from("no such value");
        });
    }),
    tu.test("illegal arbitray object", function() {
        var strategy;

        tu.expectAssertionError("arbitray object", function() {
            strategy = UploadStrategy.from({});
        });
    })
));

suites.push(tu.suite("is()",
    tu.test("test  enumeration value", function() {
        var strategy = UploadStrategy.INDIVIDUAL_OBJECTS_STRATEGY;
        util.assert(strategy.is(UploadStrategy.INDIVIDUAL_OBJECTS_STRATEGY),
            "should be true");
        util.assert(!strategy.is(UploadStrategy.CHUNKED_DATASET_STRATEGY),
            "should be false");

    }),

    tu.test("test a string", function() {
        var strategy = UploadStrategy.INDIVIDUAL_OBJECTS_STRATEGY;
        util.assert(strategy.is("indiv"),    "1- should be true");
        util.assert(strategy.is("i"),        "2 -should be true");
        util.assert(!strategy.is("chunked"), "3- should be false");
    }),

    tu.test("illegal value", function() {
        var strategy = UploadStrategy.INDIVIDUAL_OBJECTS_STRATEGY;
        util.assert(!strategy.is(null),     "1 - should be false");
        util.assert(!strategy.is(undefined), "2 - should be false");
        util.assert(!strategy.is({}),        "3 - should be false");
    })
));

exports.run = function() {
    for (var i=0; i<suites.length; i++) {
        suites[i].run();
    }
};
    