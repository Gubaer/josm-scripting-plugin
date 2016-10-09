var tu = require("josm/unittest");
var util = require("josm/util");
var test = tu.test;

var nb = require("josm/builder").NodeBuilder;
var DataSet = org.openstreetmap.josm.data.osm.DataSet;

var suites = [];

suites.push(tu.suite("NodeBuilder test cases",
    test("local node - most simple node", function() {
        var node = nb.create();
        util.assert(util.isSomething(node));
    }),
    test("local node - with position", function() {
        var node = nb.withPosition(1,2).create();
        util.assert(util.isSomething(node), "expected node to be something");
        util.assert(node.getCoor().$lat() == 1, "lat should be 1");
        util.assert(node.getCoor().$lon() == 2, "lon should be 2");
        util.assert(node.lat == 1, "lat with property sould be 1");
        util.assert(node.lon == 2, "lon with property should be 2");
    }),
    test("local node - with missing position", function() {
        tu.expectError("missing position", function() {
            var node = nb.withPosition().create();
        });
    }),
    test("local node - with illegal lat", function() {
        tu.expectError("illegal lat", function() {
            var node = nb.withPosition(-91,2).create();
        });
    }),
    test("local node - with illegal lon", function() {
        tu.expectError("with illegal lon", function() {
            var node = nb.withPosition(1,181).create();
        });
    }),
    test("local node - with tags", function() {
        var node = nb.withTags({name: "aName"}).create();
        util.assert(util.isSomething(node));
        util.assert(node.get("name") == "aName");
        util.assert(node.tags.name == "aName");
    }),
    test("local node - with tags - null", function() {
        var node = nb.withTags(null).create();
    }),
    test("local node - with tags - undef", function() {
        var node = nb.withTags(void(0)).create();
    }),
    test("local node - with tags - unsupported value", function() {
        tu.expectError("with tags - unsupported value", function() {
            var node = nb.withTags("string value not allowed").create();
        });
    }),
    test("local node - with a dataset", function() {
        var ds = new DataSet();
        var NodeBuilder = require("josm/builder").NodeBuilder;
        var nb = new NodeBuilder(ds);
        var node = nb.create();
        util.assert(ds.getNodes().size() == 1, "size should be 1");
    }),

    /* ---------------------------------------------------------------------- */
    /* tests for global nodes */
    /* ---------------------------------------------------------------------- */
    test("global node - most simple node", function() {
        var node = nb.create(1);
        util.assert(node.getUniqueId() == 1, "Expected id 1, got {0}",
            node.getUniqueId());
        util.assert(node.getVersion() == 1, "Expected version 1, got {0}",
            node.getVersion());
        util.assert(node.id == 1, "Expected id 1, got {0}", node.id);
        util.assert(node.version == 1, "Expected id 1, got {0}", node.version);
    }),

    test("global node - with id and version", function() {
        var node = nb.create(2,{version: 3});
        util.assert(node.getUniqueId() == 2, "unique id should be 2");
        util.assert(node.getVersion() == 3, "version should be 3");
        util.assert(node.id == 2);
        util.assert(node.version == 3);
    }),

    test("global node - withId(id)", function() {
        var node = nb.withId(2).create();
        util.assert(node.id == 2);
        util.assert(node.version == 1);
    }),

    test("global node - withId(id,version) ", function() {
        var node = nb.withId(2,3).create();
        util.assert(node.id == 2);
        util.assert(node.version == 3);
    }),

    test("global node - withId(id,version) - overriding ", function() {
        var node = nb.withId(2,3).create(5,{version: 6});
        util.assert(node.id == 5);
        util.assert(node.version == 6);
    }),

    test("global node - illegal id - 0", function() {
        tu.expectAssertionError("illegal id- 0", function() {
            var node = nb.create(0);
        });
    }),

    test("global node - illegal id - negative", function() {
        tu.expectAssertionError("illegal id- negative", function() {
            var node = nb.create(-1);
        });
    }),

    test("global node - illegal version - 0", function() {
        tu.expectAssertionError("illegal version - 0", function() {
            var node = nb.create(1,0);
        });
    }),

    test("global node - illegal version - negative", function() {
        tu.expectAssertionError("illegal version - negative", function() {
            var node = nb.create(1,-1);
        });
    }),

    test("global node - illegal version - not a number", function() {
        tu.expectAssertionError("illegal version - not a number", function() {
            var node = nb.create(1,"5");
        });
    }),

    test("global node - illegal version - not a number - null", function() {
        tu.expectAssertionError(
            "illegal vesion not a number - null", function() {
            var node = nb.create(1,null);
        });
    }),
    test("global node - with a dataset", function() {
        var ds = new DataSet();
        var NodeBuilder = require("josm/builder").NodeBuilder;
        var nb = new NodeBuilder(ds);
        var node = nb.create(2);
        util.assert(ds.getNodes().size() == 1);
    }),

    /* ---------------------------------------------------------------------- */
    /* tests for proxy nodes */
    /* ---------------------------------------------------------------------- */
    test("proxy node - simple case", function() {
        var node = nb.createProxy(5);
        util.assert(node.id == 5, "Expected id {0}, got {1}", 5, node.id);
        util.assert(node.version == undefined,
            "Expected undefined version, got {0}", node.version);
        util.assert(node.isIncomplete,
            "Expected node is incomplete, got {0}", node.isIncomplete);
    }),

    test("proxy node - no id", function() {
        tu.expectAssertionError("proxy node - no id",function() {
            var node = nb.createProxy();
        });
    }),

    test("proxy node - negative id", function() {
        tu.expectAssertionError("proxy node - negative id", function() {
            var node = nb.createProxy(-1);
        });
    }),
    test("proxy node - with a dataset", function() {
        var ds = new DataSet();
        var NodeBuilder = require("josm/builder").NodeBuilder;
        var nb = new NodeBuilder(ds);
        var node = nb.createProxy(2);
        util.assert(ds.getNodes().size() == 1);
    }),

    /* ---------------------------------------------------------------------- */
    /* create */
    /* ---------------------------------------------------------------------- */
    test("create - id - OK", function() {
        var n = nb.create({id: 1});
        util.assert(n.id === 1, "unexpected id, got {0}", n.id);
    }),
    test("create - id - 0", function() {
        tu.expectAssertionError("create - id - 0", function() {
            var n = nb.create({id: 0});
        });
    }),
    test("create - id - -1", function() {
        tu.expectAssertionError("create - id - -1", function() {
            var n = nb.create({id: -1});
        });
    }),
    test("create - id - null", function() {
        tu.expectAssertionError("create - id - null", function() {
            var n = nb.create({id: null});
        });
    }),
    test("create - id - undefined", function() {
        tu.expectAssertionError("create - id - undefined", function() {
            var n = nb.create({id: undefined});
        });
    }),
    test("create - id - not a number", function() {
        tu.expectAssertionError("create - id - not a number", function() {
            var n = nb.create({id: "not a number"});
        });
    }),


    test("create - version - OK", function() {
        var n = nb.create({id: 1, version: 2});
        util.assert(n.version == 2, "unexpected version, got {0}", n.version);
    }),
    test("create - version - 0", function() {
        tu.expectAssertionError("create - version - 0", function() {
            var n = nb.create({id: 1, version: 0});
        });
    }),
    test("create - version - -1", function() {
        tu.expectAssertionError("create - version - -1", function() {
            var n = nb.create({id: 1, version: -1});
        });
    }),
    test("create - version - null", function() {
        tu.expectAssertionError("create - version - null", function() {
            var n = nb.create({id: 1, version: null});
        });
    }),
    test("create - version - undefined", function() {
        tu.expectAssertionError("create - version - undefined", function() {
            var n = nb.create({id: 1, version: undefined});
        });
    }),
    test("create - version - not a number", function() {
        tu.expectAssertionError("create - version - not a number", function() {
            var n = nb.create({id: 1, version: "not a number"});
        });
    })
));

suites.push(tu.suite("forDataSet test cases",
    test("create with defined dataset", function() {
        var ds = new org.openstreetmap.josm.data.osm.DataSet();
        var nb = require("josm/builder").NodeBuilder.forDataSet(ds);
        var n = nb.create();
        util.assert(n.getDataSet() === ds,
            "node should belong to the dataset {0}, actually is {1}",
            ds,
            n.getDataSet()
        );
    })
));

exports.run = function() {
    for (var i=0; i<suites.length; i++) {
        suites[i].run();
    }
};

