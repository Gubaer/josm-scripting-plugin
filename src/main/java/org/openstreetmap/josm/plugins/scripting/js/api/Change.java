package org.openstreetmap.josm.plugins.scripting.js.api;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.validation.constraints.NotNull;

import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.RelationMember;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.data.osm.DefaultNameFormatter;
import org.openstreetmap.josm.plugins.scripting.util.Assert;

/**
 * Represents a change to one of the "properties" of an
 * {@link OsmPrimitive}. A change consists of a list of
 * {@link PropertyChange}s. It can be applied to a primitive, and it
 * can generate an explanation of the applied change.
 */
public class Change {
    @SuppressWarnings("unused")
    private static final Logger logger =
            Logger.getLogger(Change.class.getName());

    @SuppressWarnings("unused")
    public static abstract class PropertyChange<T> {
        protected T newValue;
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

        protected abstract void ensureApplicable(OsmPrimitive primitive);

        public T getNewValue() {
            return newValue;
        }
    }

    public static class LatChange extends PropertyChange<Number>{
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
            double lat = newValue.doubleValue();
            LatLon oldpos = node.getCoor();
            LatLon newpos = oldpos == null ?
                    new LatLon(lat,0) : new LatLon(lat, oldpos.lon());
            node.setCoor(newpos);
        }

        @Override
        public String explain(OsmPrimitive primitive) {
            double lat = newValue.doubleValue();
            return "lat=" + LatLon.cDdFormatter.format(lat);
        }

        public LatChange(@NotNull Number newValue) {
            Objects.requireNonNull(newValue);
            Assert.assertArg(LatLon.isValidLat(newValue.doubleValue()),
                    "Expected a valid lat, got {0}", newValue);
            this.newValue = newValue;
        }
    }

    public static class LonChange extends PropertyChange<Number>{
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
            double lon = newValue.doubleValue();
            LatLon oldpos = node.getCoor();
            LatLon newpos = oldpos == null ?
                    new LatLon(0,lon) : new LatLon(oldpos.lat(), lon);
            node.setCoor(newpos);
        }

        @Override
        public String explain(OsmPrimitive primitive) {
            double lon = newValue.doubleValue();
            return "lon=" + LatLon.cDdFormatter.format(lon);
        }

        public LonChange(@NotNull Number newValue) {
            Objects.requireNonNull(newValue);
            Assert.assertArg(LatLon.isValidLon(newValue.doubleValue()),
                    "Expected a valid lat, got {0}", newValue);
            this.newValue = newValue;
        }
    }

    public static class PosChange extends PropertyChange<LatLon> {
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
            node.setCoor(newValue);
        }

        @Override
        public String explain(OsmPrimitive primitive) {
            return MessageFormat.format("pos=[{0},{1}]",
                    LatLon.cDdFormatter.format(newValue.lat()),
                    LatLon.cDdFormatter.format(newValue.lon()));
        }

        public PosChange(@NotNull LatLon newValue) {
            Objects.requireNonNull(newValue);
            this.newValue = newValue;
        }
    }

    public static class TagsChange extends PropertyChange<Map<String,String>> {
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
                newValue.entrySet().stream()
                    .filter(entry -> entry.getKey() != null)
                    .forEach(entry ->
                        primitive.put(entry.getKey(), entry.getValue()));
            }
        }

        @Override
        public String explain(OsmPrimitive primitive) {
            if (newValue == null){
                return "tags=[]";
            } else {
                final String formattedTags = newValue.keySet().stream()
                    .map(key -> {
                        String value = newValue.get(key);
                        if (value == null){
                            return "[x]" + key;
                        } else {
                            return key + "=" + "value";
                        }
                    })
                    .collect(Collectors.joining(","));
                return "tags=[" + formattedTags + "]";
            }
        }

        public TagsChange(Map<String,String> newValue) {
            this.newValue = newValue;
        }
    }

    public static class NodesChange extends PropertyChange<List<Node>> {
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
            way.setNodes(newValue);
        }

        @Override
        public String explain(OsmPrimitive primitive) {
            StringBuilder sb = new StringBuilder();
            sb.append("nodes=[");
            if (newValue != null) {
                sb.append(newValue.stream()
                   .map(n -> n.getDisplayName(DefaultNameFormatter.getInstance()))
                   .collect(Collectors.joining(", "))
                );
            }
            sb.append("]");
            return sb.toString();
        }

        public NodesChange(List<Node> newValue) {
            if (newValue == null){
                this.newValue = null;
            } else {
                newValue.remove(null);
                this.newValue = newValue;
            }
        }
    }

    public static class MemberChange extends PropertyChange<List<RelationMember>> {

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
            relation.setMembers(newValue);
        }

        @Override
        public String explain(OsmPrimitive primitive) {
            StringBuilder sb = new StringBuilder();
            sb.append("members=[");
            if (newValue != null) {
                sb.append(newValue.stream()
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

        public MemberChange(List<RelationMember> newValue) {
            if (newValue != null) {
                newValue.remove(null);
                this.newValue = newValue;
            }
        }
    }

    private final List<PropertyChange<?>> changes = new ArrayList<>();

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
        for (PropertyChange<?> change: changes) {
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
        return primitive.getDisplayName(DefaultNameFormatter.getInstance()) +
            ": " +
            changes.stream()
                .filter(change -> change.appliesTo(primitive))
                .map(change -> change.explain(primitive))
                .collect(Collectors.joining(", "));
    }
}
