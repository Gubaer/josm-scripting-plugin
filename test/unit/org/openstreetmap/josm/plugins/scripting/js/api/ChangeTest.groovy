
package org.openstreetmap.josm.plugins.scripting.js.api;

import static org.junit.Assert.*;

import org.openstreetmap.josm.plugins.scripting.js.api.Change.NodesChange;
import org.openstreetmap.josm.plugins.scripting.fixtures.JOSMFixture;
import org.openstreetmap.josm.plugins.scripting.js.api.Change.LatChange;
import org.openstreetmap.josm.plugins.scripting.js.api.Change.LonChange;
import org.openstreetmap.josm.plugins.scripting.js.api.Change.MemberChange
import org.openstreetmap.josm.plugins.scripting.js.api.Change.PosChange
import org.openstreetmap.josm.plugins.scripting.js.api.Change.TagsChange;
import org.openstreetmap.josm.data.coor.LatLon
import org.openstreetmap.josm.data.osm.Node
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.RelationMember
import org.openstreetmap.josm.data.osm.Way
import org.openstreetmap.josm.data.osm.Relation
import org.junit.*

class ChangeTest {

    def shouldFail = new GroovyTestCase().&shouldFail;

    static def JOSMFixture fixture;

    @BeforeClass
    public static void init() {
        fixture = JOSMFixture.createUnitTestFixture();
        fixture.init();
    }

    @Test
    public void testLatChange() {
        def change = new LatChange(12.345);
        assert change

        // can be null
        shouldFail(IllegalArgumentException) {
            change = new LatChange(null);
        }

        // string are not accepted
        shouldFail(IllegalArgumentException) {
            change = new LatChange("123.45");
        }

        // illegal lat
        shouldFail(IllegalArgumentException) {
            change = new LatChange(100.456);
        }

        change = new LatChange(12.345)
        def node = new Node(new LatLon(0,0));
        assert change.appliesTo( new Node(new LatLon(0,0))), "applies to complete nodes"
        assert !change.appliesTo( new Node(1)), "doesn' apply to incomplete nodes";
        assert !change.appliesTo( new Way(1,1)), "doesn't apply to ways"
        assert !change.appliesTo( new Relation(1,1)), "doesn't apply to relations"

        change = new LatChange(12.345)
        node = new Node(new LatLon(1,2))
        change.apply(node);
        assert node.getCoor() == new LatLon(12.345,2), "unexpected position"

        def explanation = change.explain(node)
        println "LatChange: explanation: ${explanation}"
    }

    @Test
    public void testLonChange() {
        def change = new LonChange(12.345);
        assert change

        // can be null
        shouldFail(IllegalArgumentException) {
            change = new LonChange(null);
        }

        // string are not accepted
        shouldFail(IllegalArgumentException) {
            change = new LonChange("123.45");
        }

        // illegal lat
        shouldFail(IllegalArgumentException) {
            change = new LonChange(200.456);
        }

        change = new LonChange(12.345)
        def node = new Node(new LatLon(0,0));
        assert change.appliesTo( new Node(new LatLon(0,0))), "applies to complete nodes"
        assert !change.appliesTo( new Node(1)), "doesn' apply to incomplete nodes";
        assert !change.appliesTo( new Way(1,1)), "doesn't apply to ways"
        assert !change.appliesTo( new Relation(1,1)), "doesn't apply to relations"

        change = new LonChange(12.345)
        node = new Node(new LatLon(1,2))
        change.apply(node);
        assert node.getCoor() == new LatLon(1,12.345), "unexpected position"

        def explanation = change.explain(node)
        println "LonChange: explanation: ${explanation}"
    }

    @Test
    public void testPosChange() {
        def change = new PosChange(new LatLon(1,1));
        assert change

        // can be null
        shouldFail(IllegalArgumentException) {
            change = new PosChange(null);
        }

        // string are not accepted
        shouldFail(IllegalArgumentException) {
            change = new PosChange("123.45,12.22");
        }

        change = new PosChange(new LatLon(3,4));
        def node = new Node(new LatLon(0,0));
        assert change.appliesTo( new Node(new LatLon(0,0))), "applies to complete nodes"
        assert !change.appliesTo( new Node(1)), "doesn' apply to incomplete nodes";
        assert !change.appliesTo( new Way(1,1)), "doesn't apply to ways"
        assert !change.appliesTo( new Relation(1,1)), "doesn't apply to relations"

        change = new PosChange(new LatLon(3,4));
        node = new Node(new LatLon(1,2))
        change.apply(node);
        assert node.getCoor() == new LatLon(3,4), "unexpected position"

        def explanation = change.explain(node)
        println "PosChange: explanation: ${explanation}"
    }


    @Test
    public void testTagsChange() {

        def change = new TagsChange([name: "test"]);
        assert change
        change = new TagsChange(null); // to remove all tags
        assert change

        // string are not accepted
        shouldFail(IllegalArgumentException) {
            change = new TagsChange("name=test");
        }

        change = new TagsChange([name: "test"]);
        assert change.appliesTo( new Node(new LatLon(0,0))), "applies to complete nodes"
        assert !change.appliesTo( new Node(1)), "doesn' apply to incomplete nodes";
        assert change.appliesTo( new Way(1,1)), "aplies to ways"
        assert change.appliesTo( new Relation(1,1)), "applies to relations"

        change = new TagsChange([name: "newname", todelete: null]);
        def node = new Node(new LatLon(1,2))
        node.put("name", "oldname");
        node.put("todelete", "value");
        change.apply(node);
        assert node.get("name") == "newname"
        assert ! node.hasKey("todelete")

        def explanation = change.explain(node)
        println "TagsChange: explanation: ${explanation}"

        change = new TagsChange(null);
        node = new Node(new LatLon(1,2))
        node.put("name", "oldname");
        node.put("todelete", "value");
        change.apply(node);
        assert ! node.hasKey("name")
        assert ! node.hasKey("todelete")
        explanation = change.explain(node)
        println "TagsChange: explanation: ${explanation}"
    }

    @Test
    public void testNodesChange() {

        def n1 = new Node(new LatLon(1,1));
        def n2 = new Node(new LatLon(2,2));

        def change = new NodesChange([n1,n2])
        assert change
        change = new NodesChange(null); // to remove all nodes
        assert change


        // string are not accepted
        shouldFail(IllegalArgumentException) {
            change = new NodesChange("name=test");
        }

        // can't add a set of nodes
        shouldFail(IllegalArgumentException) {
            change = new NodesChange([n1,n2] as Set);
        }

        change = new NodesChange([n1,n2])
        assert !change.appliesTo( new Node(new LatLon(0,0))), "doesn't apply to nodes"
        assert change.appliesTo( new Way(1,1)), "aplies to ways"
        assert !change.appliesTo( new Way(1)), "doesn' apply to incomplete way";
        assert !change.appliesTo( new Relation(1,1)), "doesn't apply to relations"

        change = new NodesChange([n1,n2])
        def n3 = new Node(3,1);
        def n4 = new Node(4,1);
        def way = new Way(1,1);
        way.setNodes([n3,n4]);
        change.apply(way);
        assert way.getNodes() == [n1,n2]

        def explanation = change.explain(way)
        println "NodesChange: explanation: ${explanation}"

        change = new NodesChange(null)
        way.setNodes([n3,n4]);
        change.apply(way);
        assert way.getNodes() == []

        explanation = change.explain(way)
        println "NodesChange: explanation: ${explanation}"
    }

    def member = {role, obj ->
        return new RelationMember(role, obj);
    }

    @Test
    public void testMemberChange() {
        def n1 = new Node(new LatLon(1,1));
        def n2 = new Node(new LatLon(2,2));

        def change = new MemberChange([member("role.1", n1), member("role.2", n2)])
        assert change
        change = new MemberChange(null); // to remove all nodes
        assert change


        // string are not accepted
        shouldFail(IllegalArgumentException) {
            change = new MemberChange("name=test");
        }

        // can't add a set of nodes
        shouldFail(IllegalArgumentException) {
            change = new MemberChange([member("role.1", n1), member("role.2", n2)] as Set);
        }

        change = new MemberChange([member("role.1", n1), member("role.2", n2)])
        assert !change.appliesTo( new Node(new LatLon(0,0))), "doesn't apply to nodes"
        assert ! change.appliesTo( new Way(1,1)), "doesn't apply to ways"
        assert change.appliesTo( new Relation(1,1)), "applies to relations"
        assert !change.appliesTo( new Relation(1)), "doesn' apply to incomplete relations";


        change = new MemberChange([member("role.1", n1), member("role.2", n2)])
        def n3 = new Node(3,1);
        def n4 = new Node(4,1);
        def relation = new Relation(1,1);
        relation.setMembers([member("role.3", n3), member("role.4", n4)])
        change.apply(relation);
        assert relation.getMembers() == [member("role.1", n1), member("role.2", n2)]

        def explanation = change.explain(relation)
        println "NodesChange: explanation: ${explanation}"

        change = new MemberChange(null)
        relation.setMembers([member("role.3", n3), member("role.4", n4)])
        change.apply(relation);
        assert relation.getMembers() == []

        explanation = change.explain(relation)
        println "NodesChange: explanation: ${explanation}"
    }

    @Test
    public void scheduleLatChange() {
        def change = new Change();
        change.withLatChange(12.34);

        def n = new Node(new LatLon(1,1));
        change.apply(n);
        assert n.getCoor().lat() == 12.34;

        // change should skip these two
        change.apply(new Way(1,1));
        change.apply(new Relation(1,1));

        def explanation = change.explain(n)
        println "Change: LatChange: explanation: ${explanation}"
    }

    @Test
    public void scheduleLonChange() {
        def change = new Change();
        change.withLonChange(12.34);

        def n = new Node(new LatLon(1,1));
        change.apply(n);
        assert n.getCoor().lon() == 12.34;

        // change should skip these two
        change.apply(new Way(1,1));
        change.apply(new Relation(1,1));

        def explanation = change.explain(n)
        println "Change: LonChange: explanation: ${explanation}"
    }

    @Test
    public void schedulePosChange() {
        def change = new Change();
        change.withPosChange(new LatLon(3,4));

        def n = new Node(new LatLon(1,1));
        change.apply(n);
        assert n.getCoor() == new LatLon(3,4)

        // change should skip these two
        change.apply(new Way(1,1));
        change.apply(new Relation(1,1));

        def explanation = change.explain(n)
        println "Change: PosChange: explanation: ${explanation}"
    }


    @Test
    public void scheduleTagsChange() {
        def change = new Change();
        change.withTagsChange([name: "testname", todelete: null]);

        def n = new Node(new LatLon(1,1));
        n.put("name", "oldname");
        n.put("todelete", "avalue");
        change.apply(n);
        assert n.get("name") == "testname"
        assert !n.hasKey("todelete");

        def explanation = change.explain(n)
        println "Change: TagsChange: explanation: ${explanation}"
    }

    @Test
    public void scheduleNodesChange() {
        def change = new Change();
        def n1 = new Node(new LatLon(1,1));
        def n2 = new Node(new LatLon(2,2));
        change.WithNodeChange([n1,n2,null]);

        def way = new Way(1,1);
        way.setNodes([new Node(1,1), new Node(2,1)]);
        change.apply(way);
        assert way.getNodes() == [n1,n2];
        def explanation = change.explain(way)
        println "Change: NodesChange: explanation: ${explanation}"

        // change should skip these two
        change.apply(new Node(1,1));
        change.apply(new Relation(1,1));
    }

    @Test
    public void scheduleMemberChanges() {
        def change = new Change();
        def n1 = new Node(new LatLon(1,1));
        def n2 = new Node(new LatLon(2,2));

        def members = [member("role.1", n1), member("role.2", n2)];

        change.withMemberChange(members);

        def relation = new Relation(1,1);
        relation.setMembers([member("role.3", new Way(3,1)), member("role.4", new Relation(4,1))])
        change.apply(relation);
        assert relation.getMembers() == members

        def explanation = change.explain(relation)
        println "Change: MemberChange: explanation: ${explanation}"

        // change should skip these two
        change.apply(new Node(1,1));
        change.apply(new Way(1,1));
    }

    @Test
    public void scheduleCombined() {
        def change = new Change()
            .withLatChange(11.11)
            .withLonChange(22.22)
            .withTagsChange([name: "test"])

        def node = new Node(new LatLon(0,0))
        change.apply(node);
        assert node.getCoor().lat() == 11.11;
        assert node.getCoor().lon() == 22.22;
        assert node.get("name") == "test"

        def explanation = change.explain(node)
        println "Change: combined: explanation: ${explanation}"
    }
}
