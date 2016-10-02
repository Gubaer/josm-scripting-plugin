package org.openstreetmap.josm.plugins.scripting.js.api;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.RelationMember;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.gui.DefaultNameFormatter;
import org.openstreetmap.josm.plugins.scripting.util.Assert;

/**
 * <p>Represents a change to one of the "properties" of an
 * {@link OsmPrimitive}. A change consists of a list of
 * {@link PropertyChange}s. It can be applied to a primitive and it
 * can generate an explanation of the applied change.</p>
 *
 */
public class Change {
    //private static final Logger logger =
    //      Logger.getLogger(Change.class.getName());

    public static abstract class PropertyChange {
        protected Object newValue;
        public  boolean appliesTo(OsmPrimitive primitive) {
            try {
                ensureApplicable(primitive);
                return true;
            } catch(Exception e) {
                return false;
            }
        }
        public abstract void apply(OsmPrimitive primitive);
        public abstract String explain(OsmPrimitive primitive);

        protected abstract void ensureValidValue(Object value);
        protected abstract void ensureApplicable(OsmPrimitive primitive);


        public Object getNewValue() {
            return newValue;
        }
    }

    public static class LatChange extends PropertyChange{
         protected void ensureApplicable(OsmPrimitive primitive) {
             Assert.assertArg(primitive instanceof Node,
                     "Expected a node, got {0}", primitive);
             Assert.assertArg(!primitive.isIncomplete(),
                     "Node must not be incomplete");
        }

        @Override
        public void apply(OsmPrimitive primitive) {
            ensureApplicable(primitive);
            Node node = (Node)primitive;
            double lat = ((Number)newValue).doubleValue();
            LatLon oldpos = node.getCoor();
            LatLon newpos = oldpos == null ?
                    new LatLon(lat,0) : new LatLon(lat, oldpos.lon());
            node.setCoor(newpos);
        }

        @Override
        public String explain(OsmPrimitive primitive) {
            double lat = ((Number)newValue).doubleValue();
            return "lat=" + LatLon.cDdFormatter.format(lat);
        }

        @Override
        protected void ensureValidValue(Object value) {
            Assert.assertArg(value != null
                    ,"lat must not be null");
            Assert.assertArg(value instanceof Number,
                    "lat must be a number, got {0}", value);
            double lat = ((Number)value).doubleValue();
            Assert.assertArg(LatLon.isValidLat(lat),
                    "Expected a valid lat, got {0}", lat);
        }

        public LatChange(Object newValue) {
            ensureValidValue(newValue);
            this.newValue = newValue;
        }
    }

    public static class LonChange extends PropertyChange{
         protected void ensureApplicable(OsmPrimitive primitive) {
             Assert.assertArg(primitive instanceof Node,
                     "Expected a node, got {0}", primitive);
             Assert.assertArg(!primitive.isIncomplete(),
                     "Node must not be incomplete");
        }

        @Override
        public void apply(OsmPrimitive primitive) {
            ensureApplicable(primitive);
            Node node = (Node)primitive;
            double lon = ((Number)newValue).doubleValue();
            LatLon oldpos = node.getCoor();
            LatLon newpos = oldpos == null ?
                    new LatLon(0,lon) : new LatLon(oldpos.lat(), lon);
            node.setCoor(newpos);
        }

        @Override
        public String explain(OsmPrimitive primitive) {
            double lon = ((Number)newValue).doubleValue();
            return "lon=" + LatLon.cDdFormatter.format(lon);
        }

        @Override
        protected void ensureValidValue(Object value) {
            Assert.assertArg(value != null,"lon must not be null");
            Assert.assertArg(value instanceof Number,
                    "lon must be a number, got {0}", value);
            double lon = ((Number)value).doubleValue();
            Assert.assertArg(LatLon.isValidLon(lon),
                    "Expected a valid lon, got {0}", lon);
        }

        public LonChange(Object newValue) {
            ensureValidValue(newValue);
            this.newValue = newValue;
        }
    }

    public static class PosChange extends PropertyChange {
         protected void ensureApplicable(OsmPrimitive primitive) {
             Assert.assertArg(primitive instanceof Node,
                     "Expected a node, got {0}", primitive);
             Assert.assertArg(!primitive.isIncomplete(),
                     "Node must not be incomplete");
        }

        @Override
        public void apply(OsmPrimitive primitive) {
            ensureApplicable(primitive);
            Node node = (Node)primitive;
            LatLon pos = (LatLon)newValue;
            node.setCoor(pos);
        }

        @Override
        public String explain(OsmPrimitive primitive) {
            LatLon pos = (LatLon)newValue;
            return MessageFormat.format("pos=[{0},{1}]",
                    LatLon.cDdFormatter.format(pos.lat()),
                    LatLon.cDdFormatter.format(pos.lon()));
        }

        @Override
        protected void ensureValidValue(Object value) {
            Assert.assertArgNotNull(value, "pos");
            Assert.assertArg(value instanceof LatLon,
                    "Expected a LatLon, got {0}", value);
        }

        public PosChange(Object newValue) {
            ensureValidValue(newValue);
            this.newValue = newValue;
        }
    }

    public static class TagsChange extends PropertyChange {
        protected void ensureApplicable(OsmPrimitive primitive) {
             Assert.assertArg(!primitive.isIncomplete(),
                     "Primitive must not be incomplete, got {0}", primitive);
        }

        @Override
        public void apply(OsmPrimitive primitive) {
            ensureApplicable(primitive);
            if (newValue == null){
                primitive.removeAll();
            } else {
                Map<String, String> tags = (Map<String,String>)newValue;
                for(String key: tags.keySet()) {
                    if (key == null) continue;
                    key = key.trim();
                    String value = tags.get(key);
                    // this will remove the tag, if value is null
                    primitive.put(key, value);
                }
            }
        }

        @Override
        public String explain(OsmPrimitive primitive) {
            if (newValue == null){
                return "tags=[]";
            } else {
                Map<String,String> tags = (Map<String, String>)newValue;
                StringBuilder sb = new StringBuilder();
                for (String key: tags.keySet()) {
                    if (sb.length() > 0) sb.append(", ");
                    String value = tags.get(key);
                    if (value == null){
                        sb.append("[x]").append(key);
                    } else {
                        sb.append(key).append("=").append(value);
                    }
                }
                return "tags=[" + sb.toString() + "]";
            }
        }

        @Override
        protected void ensureValidValue(Object value) {
            if (value != null) {
                Assert.assertArg(value instanceof Map<?,?>,
                        "Expected a Map of tags, got {0}", value);
            }
        }

        public TagsChange(Object newValue) {
            ensureValidValue(newValue);
            this.newValue = newValue;
        }
    }

    public static class NodesChange extends PropertyChange {
         protected void ensureApplicable(OsmPrimitive primitive) {
             Assert.assertArg(primitive instanceof Way,
                     "Expected a way, got {0}", primitive);
             Assert.assertArg(!primitive.isIncomplete(),
                     "Primitive must not be incomplete, got {0}", primitive);
        }

        @Override
        public void apply(OsmPrimitive primitive) {
            ensureApplicable(primitive);
            Way way = (Way)primitive;
            if (newValue == null) {
                way.setNodes(null);
            } else {
                @SuppressWarnings("unchecked") List<Node> nodes =
                        (List<Node>)newValue;
                way.setNodes(nodes);
            }
        }

        @Override
        public String explain(OsmPrimitive primitive) {
            StringBuffer sb = new StringBuffer();
            sb.append("nodes=[");
            if (newValue != null) {
                List<Node> nodes = (List<Node>)newValue;
                sb.append(nodes.stream()
                   .map(n -> n.getDisplayName(DefaultNameFormatter.getInstance()))
                   .collect(Collectors.joining(", "))
                );
            }
            sb.append("]");
            return sb.toString();
        }

        @Override
        protected void ensureValidValue(Object value) {
            if (value == null){
                return;
            } else {
                Assert.assertArg(value instanceof List<?>,
                        "Expected a list of nodes, got {0}", value);
                try {
                    List<?> nodes = (List<?>)value;
                    for(Object o: nodes) {
                        if (o == null) continue;
                        Node node = (Node)o; // just try to convert to a node
                    }
                } catch(ClassCastException e) {
                    Assert.assertArg(false,
                            "Got an illegal node list or an illegal element in "
                            + "node list, exception is {0}",e);
                }
            }
        }

        public NodesChange(Object newValue) {
            ensureValidValue(newValue);
            if (newValue == null){
                this.newValue = null;
            } else {
                @SuppressWarnings("unchecked") List<Node> nodes =
                        new ArrayList<>((List<Node>)newValue);
                nodes.remove(null);
                this.newValue = nodes;
            }
        }
    }

    public static class MemberChange extends PropertyChange {

         protected void ensureApplicable(OsmPrimitive primitive) {
             Assert.assertArg(primitive instanceof Relation,
                     "Expected a relation, got {0}", primitive);
             Assert.assertArg(!primitive.isIncomplete(),
                     "Primitive must not be incomplete, got {0}", primitive);
        }

        @Override
        public void apply(OsmPrimitive primitive) {
            ensureApplicable(primitive);
            Relation relation = (Relation)primitive;
            if (newValue == null) {
                relation.setMembers(null);
            } else {
                @SuppressWarnings("unchecked") List<RelationMember> members =
                        (List<RelationMember>)newValue;
                relation.setMembers(members);
            }
        }

        @Override
        public String explain(OsmPrimitive primitive) {
            StringBuffer sb = new StringBuffer();
            sb.append("members=[");
            if (newValue != null) {
                @SuppressWarnings("unchecked")
                List<RelationMember> members = (List<RelationMember>)newValue;
                sb.append(members.stream()
                    .map(m -> m.getRole() + "/" +
                         m.getMember().getDisplayName(
                                 DefaultNameFormatter.getInstance()
                         )
                    )
                    .collect(Collectors.joining(", "))
                );
            }
            sb.append("]");
            return sb.toString();
        }

        @Override
        protected void ensureValidValue(Object value) {
            if (value == null) return;
            Assert.assertArg(value instanceof List<?>,
                    "Expected a list of members, got {0}", value);
            try {
                List<?> members = (List<?>)value;
                for(Object o: members) {
                    if (o == null) continue;
                    // just try to convert to a member
                    RelationMember member = (RelationMember)o;
                }
            } catch(ClassCastException e) {
                Assert.assertArg(false,
                        "Got an illegal member list or an illegal member, "
                        + "exception is {0}",e);
            }
        }

        public MemberChange(Object newValue) {
            ensureValidValue(newValue);
            if (newValue != null) {
                @SuppressWarnings("unchecked") List<RelationMember> members =
                        new ArrayList<>((List<RelationMember>)newValue);
                members.remove(null);
                this.newValue = members;
            }
        }
    }

    private final List<PropertyChange> changes = new ArrayList<>();

    /**
     * Schedules a property change for the latitude of a {@link Node}
     *
     * @param lat the new latitude
     * @return the change (for method chaining)
     */
    public Change withLatChange(double lat) {
        changes.add(new LatChange(lat));
        return this;
    }

    /**
     * Schedules a change for the longitude of a {@link Node}.
     *
     * @param lon the new longitude
     * @return the change (for method chaining)
     */
    public Change withLonChange(double lon) {
        changes.add(new LonChange(lon));
        return this;
    }

    /**
     * Schedules a change for the position of a {@link Node}.
     *
     * @param pos the new position
     * @return the change (for method chaining)
     */
    public Change withPosChange(LatLon pos) {
        changes.add(new PosChange(pos));
        return this;
    }

    /**
     * Schedules a change for the tags of a primitive.
     *
     * @param tags the new tags. Can be null to delete all tags.
     * @return the change (for method chaining)
     */
    public Change withTagsChange(Map<String, String> tags){
        changes.add(new TagsChange(tags));
        return this;
    }

    /**
     * Schedules a change for the node list of a {@link Way}
     *
     * @param nodes the new list of nodes. Can be null to remove all nodes.
     */
    public void withNodeChange(List<Node> nodes) {
        changes.add(new NodesChange(nodes));
    }

    /**
     * Schedules a change for the member list of {@link Relation}
     *
     * @param members the list of new members. Can be null to remove all
     * members.
     *
     * @return the change (for method chaining)
     */
    public Change withMemberChange(List<RelationMember> members) {
        changes.add(new MemberChange(members));
        return this;
    }

    /**
     * Applies this change to the primitive.
     *
     * @param primitive the primitive.
     */
    public void apply(OsmPrimitive primitive) {
        for (PropertyChange change: changes) {
            if (change.appliesTo(primitive)) {
                change.apply(primitive);
            }
        }
    }

    /**
     * Replies an explanation of this change, when applied to the primitive
     * {@code primitive}.
     *
     * @param primitive the primitive
     * @return the explanation
     */
    public String explain(OsmPrimitive primitive) {
        StringBuilder sb = new StringBuilder();
        sb.append(primitive.getDisplayName(DefaultNameFormatter.getInstance()));
        sb.append(": ");
        sb.append(
            changes.stream()
                .filter(change -> change.appliesTo(primitive))
                .map(change -> change.explain(primitive))
                .collect(Collectors.joining(", "))
        );
        return sb.toString();
    }
}
